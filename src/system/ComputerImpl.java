/*
 * @author gautham
 */
package system;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import shared.Shared;
import utils.Constants;
import api.Space;
import api.Task;

/**
 * This class enables different tasks to be executed by the Compute Space using its remote reference (proxy)
 * These tasks are run using the task's implementation of the execute method and the results are returned to the Compute Space.
 * Each task can either be executed right away (atomic) or be decomposed into multiple tasks. In the earlier case, the result is stored in the space while in the latter case, the subtasks are stored.
 */
public final class ComputerImpl extends UnicastRemoteObject implements Computer{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The remote reference to Space (proxy). */
	private Computer2Space space;	
	
	/** The shared object. */
	private Shared shared;
	
	/** The computer id. */
	private int computerId;
	
	/** The task queue. */
	private BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<Task>(); 
	
	
	/**
	 * Instantiates a new implementation object for the Computer Interface.
	 *
	 * @throws RemoteException the remote exception
	 */
	public ComputerImpl() throws RemoteException{		
	}

	/**
	 * Different tasks can be submitted to this method.
	 * The function checks to see if the task can be executed.
	 * If the task is atomic or if it's a successor task, then it's executed and the intermediate results are stored in Space. 
	 * Otherwise, the task is split into multiple subtasks and are put in Space.
	 * 
	 * @param <T> the generic type
	 * @param t the Task object
	 * @return Result the return value of the Task object's execute method
	 * @throws RemoteException the remote exception
	 */	
	@Override
	public <T> void execute(final Task<T> t) throws RemoteException {
		try {
			if(PerformanceSettings.isAmeliorateCommunicationLatency()){
				taskQueue.put(t);
			}
			else{
				runTask(t);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	

	/* (non-Javadoc)
	 * @see system.Computer#stop()
	 */
	@Override
	public void exit() throws RemoteException {
		System.out.println("Received command to stop.");
		System.exit(0);		
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {		
		String spaceDomainName = args[0];
		
		String spaceURL = "//" + spaceDomainName + "/" + Space.SERVICE_NAME;		
		Computer2Space space = (Computer2Space) Naming.lookup(spaceURL);
		
		Computer computer = new ComputerImpl();
		Computer2Space spaceProxy = (Computer2Space) space.register(computer);
		computer.setSpace(spaceProxy);
		System.out.println("Computer ready.");
	}
	

	/* (non-Javadoc)
	 * @see system.Computer#setSpace(system.Computer2Space)
	 */
	@Override
	public void setSpace(Computer2Space space) throws Exception {		
		this.space = space;		
	}

	/* (non-Javadoc)
	 * @see system.Computer#setShared(api.Shared)
	 */
	@Override
	public synchronized void setShared(final Shared<?> proposedShared, boolean canPropagate) throws RemoteException {
		if(this.shared == null || proposedShared.isNewerThan(this.shared)){
			//System.out.println("New cost received.");
			this.shared = proposedShared;
			if(canPropagate){
				Thread thread = new Thread(){
					public void run(){
						try {
							space.setShared(proposedShared, computerId);
						} catch (RemoteException e) {
							e.printStackTrace();							
						}						
					}
				};
				thread.start();
				
			}
		}
		else{
			//System.out.println("Old cost received from task.");
		}
	}

	/* (non-Javadoc)
	 * @see system.Computer#getShared()
	 */
	@Override
	public synchronized Shared getShared() throws RemoteException {
		return this.shared;
	}

	/* (non-Javadoc)
	 * @see system.Computer#setComputerId(int)
	 */
	@Override
	public void setComputerId(int computerId) throws RemoteException {
		this.computerId = computerId;		
	}

	/* (non-Javadoc)
	 * @see system.Computer#getComputerId()
	 */
	@Override
	public int getComputerId() throws RemoteException {
		return this.computerId;
	}
	
	/**
	 * The Class ComputerWorker.
	 */
	private class ComputerWorker extends Thread{
		
		/** The worker id. */
		private int workerId = 0;
		
		/**
		 * Instantiates a new computer worker.
		 *
		 * @param workerId the worker id
		 */
		public ComputerWorker(int workerId){
			this.workerId = workerId;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run(){
			//System.out.println("Starting Computer Worker " + this.workerId);
			while(true){
				try {
					Task t = taskQueue.take();
					runTask(t);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see system.Computer#startWorkers()
	 */
	@Override
	public void startWorkers() throws RemoteException {		
		int numProcessors = PerformanceSettings.isExploitMulticoreProcessors() ? Runtime.getRuntime().availableProcessors() : 1;
		System.out.println("Number of worker(s) started: " + numProcessors);
		for(int i = 1; i <= numProcessors; i++){
			ComputerWorker worker = new ComputerWorker(i);
			worker.start();
		}
	}

	
	/**
	 * Decompose task.
	 *
	 * @param <T> the generic type
	 * @param t the t
	 */
	private <T> void decomposeTask(Task<T> t){
		// Split the task into 'n' sub-tasks and 1 successor task and put
		// them all in Space.
		// When a task constructs subtasks, the first constructed subtask is cached on the Computer.
//		System.out.println("Decompose task");
		
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
	
	/**
	 * Run task.
	 *
	 * @param <T> the generic type
	 * @param t the t
	 */
	private <T> void runTask(Task<T> t){
		try {
			
			if(t.isAtomic() || t.getTaskType() == Constants.SUCCESSOR_TASK){
				long startTime = System.currentTimeMillis();
				t.execute();
				long endTime = System.currentTimeMillis();
				t.setTaskRunTime(endTime - startTime);
				space.storeResult(t);
			}
			else{
				decomposeTask(t);
				space.storeTasks(t);
			}
			
		} catch (RemoteException e) {
			// System.out.println("Task " + t);
			e.printStackTrace();
		}
		catch(NullPointerException e){
			System.out.println("Task " + t);
			e.printStackTrace();
		}
	}
}