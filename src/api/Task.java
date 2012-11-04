/*
 * @author gautham
 */

package api;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

import shared.Shared;
import system.Computer;
import utils.Constants;

/**
 * An abstract class that acts as a link between the Computer implementation and the work that it needs to do, providing the way to start the work.
 * The client decomposes the original problem into a set of Task objects and they therefore represent the unit of work that is to be done by the Computers.
 * The objects represent the nodes in the directed acyclic graph (DAG) which is the computation model used by the applications. 
 * 
 * @param <T> a type parameter, T, which represents the result type of the task's computation.
 */
public abstract class Task<T> implements Serializable{
	
	/** The Constant serialVersionUID. */ 
	private static final long serialVersionUID = 1L;

	/** The task id. */
	protected Object taskId;
	
	/** The number that denotes the order of input to its successor task. **/
	protected int argNo;
	
	/** The task type differentiates between a child task and a successor task. The value 0 corresponds to a child task and 1 corresponds to a successor task.*/
	protected int taskType;
	
	/** The join counter denotes the number of arguments that the successor task is waiting for. */
	protected int joinCounter;
	
	/** The successor's task id. */
	protected Object successorTaskId;
	
	/** The input list that the successor is waiting for. */
	protected Task[] inputList;
	
	/** The result of execution of the task. */
	protected Result<T> result;
	
	/** The time taken to run the task on the computer. */
	protected long elapsedTime;		
	
	/** The remote reference to computer. */
	private Computer computer;
	
	/** The initial upper bound that is set by the client. */
	private Shared initUpperBound;
	
	/** The level in the directed acyclic graph (DAG) that denotes the task computation. */
	protected int level;
		
	/** The children. */
	private List<Task<T>> children;
	
	/** The children_successor task. */
	private Task<T> children_successorTask;

	/**
	 * Instantiates a new task.
	 *
	 * @param taskId the task id
	 */
	public Task(Object taskId){
		this.taskId = taskId;
	}
	
	/**
	 * Instantiates a new task.
	 *
	 * @param taskId the task id
	 * @param taskType the task type
	 */
	public Task(Object taskId, int taskType){
		this.taskId = taskId;
		this.taskType = taskType;
	}
	
	/**
	 * Instantiates a new task.
	 *
	 * @param taskId the task id
	 * @param taskType the task type
	 * @param level the level
	 */
	public Task(Object taskId, int taskType, int level){
		this.taskId = taskId;
		this.taskType = taskType;
		this.level = level;
	}
	
	/**
	 * Gets the input list that the successor task is waiting for.
	 *
	 * @return the input list
	 */
	public Task[] getInputList() {
		return this.inputList;
	}

	/**
	 * Sets the input list for the successor task.
	 *
	 * @param inputList the new input list
	 */
	public void setInputList(Task[] inputList) {
		this.inputList = inputList;
	}

	/**
	 * Sets the argument number that denotes the order of input to its successor task.
	 *
	 * @param argNo the new arg no
	 */
	public void setArgNo(int argNo){
		this.argNo = argNo;
	}
	
	/**
	 * Gets the task's argument number that denotes the order of input to its successor task.
	 *
	 * @return the arg no
	 */
	public int getArgNo(){
		return this.argNo;
	}
		

	/**
	 * Gets the successor task id.
	 *
	 * @return the successor task id
	 */
	public Object getSuccessorTaskId() {
		return successorTaskId;
	}

	/**
	 * Sets the successor task id.
	 *
	 * @param successorTaskId the new successor task id
	 */
	public void setSuccessorTaskId(Object successorTaskId) {
		this.successorTaskId = successorTaskId;
	}

	/**
	 * Gets the join counter that denotes the number of arguments that the successor task is waiting for.
	 *
	 * @return the join counter
	 */
	public int getJoinCounter() {
		return joinCounter;
	}

	/**
	 * Sets the join counter for this successor task, denoting the number of arguments that it is waiting for.
	 *
	 * @param joinCounter the new join counter
	 */
	public void setJoinCounter(int joinCounter) {
		this.joinCounter = joinCounter;
	}	

	/**
	 * Gets the task id.
	 *
	 * @return the task id
	 */
	public Object getTaskId() {
		return taskId;
	}

	/**
	 * Sets the task id.
	 *
	 * @param taskId the new task id
	 */
	public void setTaskId(Object taskId) {
		this.taskId = taskId;
	}

	/**
	 * Gets the type of this task. The task type differentiates between a child task and a successor task. The value 0 corresponds to a child task and 1 corresponds to a successor task.
	 *
	 * @return the task type
	 */
	public int getTaskType() {
		return taskType;
	}

	/**
	 * Sets the task type. The task type differentiates between a child task and a successor task. The value 0 corresponds to a child task and 1 corresponds to a successor task.
	 * 
	 * @param taskType the new task type with the acceptable values being 0 and 1.
	 */
	public void setTaskType(int taskType) {
		this.taskType = taskType;
	}
	
	
	/**
	 * Gets the result object that represents the result of execution of the task.
	 *
	 * @return the result
	 */
	public Result<T> getResult() {
		return result;
	}

	/**
	 * Sets the result after the task finishes executing.
	 *
	 * @param result the new result
	 */
	public void setResult(Result<T> result) {
		this.result = result;
	}
	
	/**
	 * Sets the task run time that denotes the time taken by the task to finish executiion.
	 *
	 * @param elapsedTime the new task run time
	 */
	public void setTaskRunTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
	
	/**
	 * Gets the task run time that denotes the time taken by the task to finish executiion.
	 *
	 * @return the task run time
	 */
	public long getTaskRunTime() {
		return elapsedTime;
	}	


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return String.valueOf(this.taskId);
	}
	
	/**
	 * Splits the task up to a desired number of subtasks as defined by the implementation classes. 
	 *
	 * @return list
	 */
	public abstract List<Task<T>> splitTask();
	
	
	/**
	 * Creates the successor task that is responsible for composing the results of the child tasks. 
	 *
	 * @return task
	 */
	public abstract Task<T> createSuccessorTask();	
	
	/**
	 * Executes a given task.
	 * This method returns the result of the implementing task's computation and thus its return type is T.
	 *
	 * @return Object of type T
	 */
	public abstract Result<T> execute();
	
	/**
	 * Checks if the task is atomic so as to decide whether or not to decompose the task.
	 *
	 * @return true, if the task is atomic
	 */
	public abstract boolean isAtomic();		
	
	/**
	 * Sets the initial value of upper bound for branch-and-bound tasks. The initial value is computed using a greedy approach.
	 *
	 * @param shared the Shared object encapsulating the upper bound
	 */
	public void setInitUpperBound(Shared shared){
		this.initUpperBound = shared;
	}
	
	/**
	 * Gets the initial value of upper bound for branch-and-bound tasks.
	 *
	 * @return the Shared object encapsulating the upper bound
	 */
	public Shared getInitUpperBound(){
		return this.initUpperBound;
	}
	
	/**
	 * Gets the shared object from Computer.
	 *
	 * @return the shared
	 * @throws RemoteException the remote exception
	 */
	public synchronized Shared getShared() throws RemoteException{
		return computer.getShared(); 
	}
	
	/**
	 * To propagate a newer shared object to other tasks, the task invokes its setShared method which calls the executing computer’s setShared method.
	 *
	 * @param shared the new shared
	 * @throws RemoteException the remote exception
	 */
	public synchronized void setShared(Shared shared) throws RemoteException{
		computer.setShared(shared, true);
	}	 
	
	/**
	 * Sets the computer that executes this task.
	 *
	 * @param computer the new computer
	 */
	public void setComputer(Computer computer) {
		this.computer = computer;
	}

	/**
	 * Sets the level of the task in the directed acyclic graph (DAG).
	 *
	 * @param level the new level
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * Gets the level of the task in the directed acyclic graph (DAG).
	 *
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Checks whether the task can be executed on Space. The default behavior is to make all the composition tasks execute on Space. This can be overridden by making the application specific task classes
	 *
	 * @return true, if the task is to be executed on Space.
	 */
	public boolean executeOnSpace() {
		return this.taskType == Constants.SUCCESSOR_TASK;
	}

	/**
	 * Gets the children of this task in the directed acyclic graph (DAG).
	 *
	 * @return the children
	 */
	public List<Task<T>> getChildren() {
		return children;
	}

	/**
	 * Sets the children of this task in the directed acyclic graph (DAG).
	 *
	 * @param children the new children
	 */
	public void setChildren(List<Task<T>> children) {
		this.children = children;
	}

	/**
	 * Gets the successor task that is responsible for composing the results of the current task's children.
	 *
	 * @return the children's successor task
	 */
	public Task<T> getChildren_successorTask() {
		return children_successorTask;
	}

	/**
	 * Sets the successor task that is responsible for composing the results of the current task's children
	 *
	 * @param the new successor task
	 */
	public void setChildren_successorTask(Task<T> children_successorTask) {
		this.children_successorTask = children_successorTask;
	}

	
}
