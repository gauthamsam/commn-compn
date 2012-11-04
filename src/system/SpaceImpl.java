/*
 * @author gautham
 */
package system;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import shared.Shared;
import utils.Constants;
import api.Result;
import api.Space;
import api.Task;

/**
 * This acts as a channel for passing messages between Client and ComputeServers. It defines mechanisms to hold Tasks that are created by the
 * Client jobs, to assign it to the ComputeServers and then process the Result objects, sub-tasks and successor tasks that are sent by the Computer
 */
public class SpaceImpl extends UnicastRemoteObject implements Space,
		Computer2Space {
	
	/** A blocking dequeue that stores the Tasks that are ready to be executed. */
	private BlockingDeque<Task> readyTasks;	
	
	/**
	 * A blocking dequeue that stores the Results submitted by the ComputeServers.
	 */
	private BlockingDeque<Result> resultQueue;
	
	/** The map that stores the waiting successor tasks. */
	private Map<Object, Task> waitingTasks;

	/** A mapping between the computerId and the actual Computer Object. */
	private Map<Integer, ComputerProxy> computerMap;

	/** The sequence number that is assigned to computers during registration. */
	private int computerId_seqNo;
	
	/** The shared object. */
	private Shared shared;	
	
	/** The total task time. */
	private double totalTaskTime = 0;
	
	/** The num tasks. */
	private int numTasks;
	/**
	 * Instantiates a new space impl.
	 * 
	 * @throws RemoteException the remote exception
	 */
	protected SpaceImpl() throws RemoteException {
		super();
		readyTasks = new LinkedBlockingDeque<Task>();
		resultQueue = new LinkedBlockingDeque<Result>();
		computerMap = Collections.synchronizedMap(new HashMap<Integer, ComputerProxy>());
		waitingTasks = Collections.synchronizedMap(new HashMap<Object, Task>());
	}
	
	

	/**
	 * Registers the Computer and creates a ComputerProxy which runs as a
	 * separate thread to process the submitted Tasks and to return the Results
	 * back to the ComputeSpace. The method is synchronized so that unique computerIds are assigned to the Computers
	 *
	 * @param computer the computer
	 * @return space
	 * @throws RemoteException the remote exception
	 * @see system.Computer2Space#register(system.Computer)
	 */
	@Override
	public synchronized Computer2Space register(Computer computer) throws RemoteException {
		computerId_seqNo++;
		ComputerProxy proxy = new ComputerProxy(computer, computerId_seqNo, this);
		computerMap.put(computerId_seqNo, proxy);
		System.out.println("Registering computer " + computerId_seqNo);
		proxy.start();
		if(PerformanceSettings.isAmeliorateCommunicationLatency()){
			proxy.startWorkers();
		}
		// return a RMI reference to ComputerProxy. 
		return (Computer2Space)UnicastRemoteObject.exportObject(proxy, 0);		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see api.Space#put(api.Task)
	 */
	@Override
	public <T> void put(Task<T> task) throws RemoteException {
		readyTasks.addFirst(task);		
		// As soon as the root task is put in space, propagate the init upperbound to all the registered computers
		Shared<?> initUpperBound = task.getInitUpperBound();
		if(initUpperBound != null){
			System.out.println("Init upper bound: " + initUpperBound.get());
			this.setShared(initUpperBound, -1);
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see api.Space#take()
	 */
	@Override
	public <T> Result<T> take() throws RemoteException, InterruptedException {
		return resultQueue.take();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see api.Space#stop()
	 */
	@Override
	public void stop() throws RemoteException {
		System.out.println("Stopping all the registered Computers.");
		System.out.println("--------------------------------------");
		for (Entry<Integer, ComputerProxy> entry : computerMap.entrySet()) {
			System.out.println("Stopping computer " + entry.getKey());
			ComputerProxy computer = entry.getValue();
			computer.exit();
		}

		System.out.println("--------------------------------------");
		System.out.println("Stopping Space.");
		System.exit(0);
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		// Construct & set a security manager to allow downloading of classes
		// from a remote codebase
		System.setSecurityManager(new RMISecurityManager());
		// instantiate a space object
		Space space = new SpaceImpl();
		// construct an rmiregistry within this JVM using the default port
		Registry registry = LocateRegistry.createRegistry(1099);
		// bind space in rmiregistry.
		registry.rebind(Space.SERVICE_NAME, space);
		System.out.println("Space is ready.");

	}

	

	/* (non-Javadoc)
	 * @see system.Computer2Space#storeResult(api.Task)
	 */
	@Override
	public synchronized <T> void storeResult(Task<T> task) {
		String type = task.getTaskType() == Constants.CHILD_TASK ? "Child Task" : "Successor Task";
		// System.out.println(task.getTaskId() + "; " + type + "; " + Arrays.toString(task.getInputList()) + "; " + task.getSuccessorTaskId() + "; " + task.getTaskRunTime());
		
		totalTaskTime += task.getTaskRunTime();
		numTasks++;
		
		Object successorTaskId = task.getSuccessorTaskId();
		Task<T> successorTask = this.waitingTasks.get(successorTaskId);
		// if successorTask == null, then that's the last task to be executed
		if(successorTask == null){
			if(task.getLevel() == 0){
				storeFinalResult(task.getResult());
				return;
			}
			else{
				while(successorTask == null){
					System.out.println("Waiting for successor task");
					successorTask = this.waitingTasks.get(successorTaskId);
				}
			}
			
		}
		
		Task<T>[] inputs = successorTask.getInputList();
		inputs[task.getArgNo()] = task;
		int joinCounter = successorTask.getJoinCounter() - 1;
		successorTask.setJoinCounter(joinCounter);		
		
		if(joinCounter == 0){ // If the successor task has all its arguments set, move it from the waiting list to ready list
			// this.waitingTasks.remove(successorTaskId);
			this.readyTasks.addFirst(successorTask);
		}
	}

	
	/* (non-Javadoc)
	 * @see system.Computer2Space#storeTasks(api.Task, java.util.List, api.Task)
	 */
	@Override
	public synchronized <T> void storeTasks(Task<T> task) throws RemoteException {
		String type = task.getTaskType() == Constants.CHILD_TASK ? "Child Task" : "Successor Task";
		// System.out.println(parentTask.getTaskId() + "; " + type + "; " + Arrays.toString(parentTask.getInputList()) + "; " + parentTask.getSuccessorTaskId() + "; " + parentTask.getTaskRunTime());
		//System.out.println("Parent task " + task);
		totalTaskTime += task.getTaskRunTime();
		numTasks++;
		Task<T> successorTask = task.getChildren_successorTask();
		
		List<Task<T>> children = task.getChildren();
		// The successor tasks must go to the waiting list unless all of its children have been pruned.
		if(successorTask.getJoinCounter() == 0){			
			readyTasks.addFirst(successorTask);			
		}
		else{
			waitingTasks.put(successorTask.getTaskId(), successorTask);
		}
		
		// if the parent task is not the root task
		if(task.getSuccessorTaskId() != null){
			/* 
			 * Update the parent successor's inputList.
			 * In that list, the parent task must be substituted with the child tasks' successor task 
			 */
			Task<T> parentSuccessor = this.waitingTasks.get(task.getSuccessorTaskId());
			while(parentSuccessor == null){
				System.out.println("parent successor is null. Sleeping.");
				parentSuccessor = this.waitingTasks.get(task.getSuccessorTaskId());
			}
			//System.out.println("parent successor for " + task + ": " + parentSuccessor);
			Task<T>[] parentInputList = parentSuccessor.getInputList();
			successorTask.setArgNo(task.getArgNo());
			parentInputList[task.getArgNo()] = successorTask;
										
		}
		
		for(Task<T> t : children){
			
			// Set the successor task for the newly created tasks
			t.setSuccessorTaskId(successorTask.getTaskId());
			readyTasks.addFirst(t);
		}		

	}

	
	/**
	 * Stores the final result that is obtained by the Client.
	 *
	 * @param <T> the generic type
	 * @param result the result
	 */
	private <T> void storeFinalResult(Result<T> result){
		System.out.println("Storing final result.");
		System.out.println("Total task time: " + totalTaskTime + " numTasks: " + numTasks);
		System.out.println("Average task execution time: " + (totalTaskTime / numTasks));
		// process the result		
		try {
			resultQueue.put(result);			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see system.Computer2Space#setShared(shared.Shared, int)
	 */
	@Override
	public synchronized void setShared(Shared<?> proposedShared, final int computerId) throws RemoteException {
		if(this.shared == null || proposedShared.isNewerThan(this.shared)){
			//System.out.println("New cost received from " + computerId + ". Propagating to all other computers.");
			this.shared = proposedShared;
			Thread thread = new Thread(){
				public void run(){
					distributeShared(computerId);
				}
			};
			thread.start();
		}
		else{
			//System.out.println("Old cost received from " + computerId);
		}
	}

	/**
	 * Distribute the shared object to all the registered computers except the one which sent it.
	 *
	 * @param computerId the computer id
	 */
	private void distributeShared(int computerId){
		for (Entry<Integer, ComputerProxy> entry : computerMap.entrySet()) {
			if(entry.getKey().intValue() != computerId){
				ComputerProxy computer = entry.getValue();
				computer.setShared(this.shared, false);
			}
		}
	}
	
	/*
	 * This thread's run method loops forever, removing tasks from the task
	 * queue, invoking the associated Computer's execute method with the task as
	 * its argument, and putting the returned Result object in the result queue
	 * for retrieval by the client.
	 */
	/**
	 * It represents the remote proxy to the ComputeServer.
	 */
	private class ComputerProxy extends Thread implements Computer2Space{
		/** The computer. */
		private Computer computer;

		/** The computer id. */
		private int computerId;

		/** The computer tasks. */
		private Map<Object, Task> computerTasks;
		
		//private TaskTracker taskTracker;
		
		/** The space. */
		private Computer2Space space;		
		
		/**
		 * Instantiates a new computer proxy.
		 *
		 * @param c the c
		 * @param id the computer id
		 * @param space the space
		 * @throws RemoteException the remote exception
		 */
		public ComputerProxy(Computer c, int id, Computer2Space space) throws RemoteException{
			this.computer = c;
			this.computerId = id;
			this.space = space;
			this.computer.setComputerId(id);			
			computerTasks = new HashMap<Object, Task>();			
		}	
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			Task<?> t = null;
			while (true) {
				try {
					t = readyTasks.takeFirst();
					t.setComputer(computer);
					/*
					 * When a task's encapsulated computation is little less complex than reading its inputs,
					 * it is faster for the Space to execute the task itself than to send it to a Computer for execution.
					 * This is because the time to marshal and unmarshal the input plus the time to marshal and unmarshal the result is
					 * more than the time to simply compute the result (not to mention network latency).
					 */
					if(PerformanceSettings.isAmeliorateCommunicationLatency() && t.executeOnSpace()){
						space.execute(t);
						continue;
					}					
					computerTasks.put(t.getTaskId(), t);
					computer.execute(t);					
					
				} catch (RemoteException e) {
					/*
					 * The Space accommodates faulty computers: If a computer
					 * that is running a task returns a RemoteException, the
					 * task is assigned to another computer.
					 */
					System.out.println("Remote Exception while executing task "
							+ t.getClass().getName() + " from Computer "
							+ this.computerId);
					// Adding the task back to the task queue
					System.out.println("Adding all the tasks that were sent to this Computer back to the ready queue to be assigned to other Computer(s).");
					// Add all the tasks that were cached by the computer to the readyTasks list
					readyTasks.addAll(computerTasks.values());					
					computerMap.remove(this.computerId);
					break;
				} catch (InterruptedException e) {
					System.out.println("Interrupted Exception");
				}
			}
		}
				
		/**
		 * Stop the computer instance.
		 */
		public void exit() {
			try {
				computer.exit();
			} catch (RemoteException e) {

			}
		}
		
		/**
		 * Sets the shared object.
		 *
		 * @param shared the new shared
		 * @param canPropagate the can propagate
		 */
		public void setShared(Shared shared, boolean canPropagate){
			try {
				computer.setShared(shared, canPropagate);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		/* (non-Javadoc)
		 * @see system.Computer2Space#register(system.Computer)
		 */
		@Override
		public Computer2Space register(Computer computer) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		/* (non-Javadoc)
		 * @see system.Computer2Space#storeTasks(api.Task)
		 */
		@Override
		public <T> void storeTasks(Task<T> task) throws RemoteException {
			computerTasks.remove(task.getTaskId());
			space.storeTasks(task);			
		}

		/* (non-Javadoc)
		 * @see system.Computer2Space#storeResult(api.Task)
		 */
		@Override
		public  <T> void storeResult(Task<T> task) throws RemoteException {
			computerTasks.remove(task.getTaskId());
			space.storeResult(task);
		}

		/* (non-Javadoc)
		 * @see system.Computer2Space#setShared(shared.Shared, int)
		 */
		@Override
		public void setShared(Shared<?> shared, int computerId)
				throws RemoteException {
			space.setShared(shared, computerId);
		}

				
		/**
		 * Start workers.
		 */
		private void startWorkers(){
			try {
				computer.startWorkers();
			} catch (RemoteException e) {				
				e.printStackTrace();
			}
		}

		/* (non-Javadoc)
		 * @see system.Computer2Space#execute(api.Task)
		 */
		@Override
		public <T> void execute(Task<T> task) throws RemoteException {
			// TODO Auto-generated method stub
			
		}
	}

	/* (non-Javadoc)
	 * @see system.Computer2Space#execute(api.Task)
	 */
	@Override
	public <T> void execute(final Task<T> task) throws RemoteException {
		try {					
			if(task.isAtomic() || task.getTaskType() == Constants.SUCCESSOR_TASK){
				long startTime = System.currentTimeMillis();
				task.execute();
				long endTime = System.currentTimeMillis();
				task.setTaskRunTime(endTime - startTime);
				storeResult(task);
			}
			else{
				decomposeTask(task);
				storeTasks(task);
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}				
	}
	
	/**
	 * Decompose task.
	 *
	 * @param <T> the generic type
	 * @param t the t
	 * @throws RemoteException the remote exception
	 */
	private <T> void decomposeTask(Task<T> t) throws RemoteException {
		// Split the task into 'n' sub-tasks and 1 successor task and put
		// them all in Space.
		// When a task constructs subtasks, the first constructed subtask is cached on the Computer.
		
		long startTime = System.nanoTime();
		List<Task<T>> children = t.splitTask();

		Task<T> successorTask = t.createSuccessorTask();
		Task[] inputList = null;
		// if all the children have NOT been pruned
		if (children != null && children.size() > 0) { 
			inputList = new Task[children.size()];
			successorTask.setJoinCounter(inputList.length);			
		} else {
			successorTask.setJoinCounter(0);
		}

		successorTask.setInputList(inputList);
		// Successor's successor should be the current task's successor.
		successorTask.setSuccessorTaskId((Object) t
				.getSuccessorTaskId());
		t.setChildren_successorTask(successorTask);
		t.setChildren(children);
		long endTime = System.nanoTime();
		long elapsedTime = endTime - startTime;
		t.setTaskRunTime(elapsedTime);
		
	}
	
}