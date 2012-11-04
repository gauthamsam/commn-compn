/*
 * @author gautham
 */
package tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import utils.Constants;

import api.Result;
import api.Task;

/**
 * This class represents the unit of work involved in computing the Fibonacci sum of a given value. 
 * Each subtask computes the fibonacci sum of a value lesser than the original value and the results are composed to form the fibonacci of the original value.
 */
public class FibonacciTask extends Task<Integer>{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The n. */
	private int n;	
		
	/**
	 * Instantiates a new fibonacci task.
	 *
	 * @param n the n
	 * @param taskType the task type
	 */
	public FibonacciTask(int n, int taskType, int level){
		super(UUID.randomUUID(), taskType);
		this.n = n;		
		this.level = level;
	}
	
	
	/* (non-Javadoc)
	 * @see api.Task#execute()
	 */
	@Override
	public Result<Integer> execute() {
		Result<Integer> result = new Result<Integer>();
		if(this.getTaskType() == Constants.CHILD_TASK){
			result.setTaskReturnValue(this.n);
			this.setResult(result);
		}
		else{ // successor task
			int sum = 0;
			if(inputList == null){
				System.out.println("input list is null");
			}
			for(Task<Integer> task : this.inputList){
				if(task.getResult() == null)
				System.out.println("Result is null");
				sum += task.getResult().getTaskReturnValue();
			}
			result.setTaskReturnValue(sum);
			this.setResult(result);
		}
		return result;
	}

	/**
	 * The Fibonacci task is split into two subtasks. One with the value n-1 and another with the value n-2.
	 *
	 * @return list
	 * @see api.Task#splitTask()
	 */
	@Override
	public List<Task<Integer>> splitTask() {
		//System.out.println("split task");
		List<Task<Integer>> tasks = new ArrayList<Task<Integer>>(2);
		for(int i = 0; i < 2; i++){
			Task<Integer> task = new FibonacciTask(this.n - 1 - i, Constants.CHILD_TASK, this.level + 1);
			task.setArgNo(i);
			tasks.add(task);
		}
		return tasks;
	}

	/* (non-Javadoc)
	 * @see api.Task#createSuccessorTask()
	 */
	@Override
	public Task<Integer> createSuccessorTask() {
		// successor tasks have the 'n' value as -1 and they are in the same level as the task that's creating it
		Task<Integer> successorTask = new FibonacciTask(-1, Constants.SUCCESSOR_TASK, this.level);
		return successorTask;
	}

	

	@Override
	public boolean isAtomic() {	
		return (this.n < Constants.FIBONACCI_BASE_CASE);
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("n:" + this.n + ", ");
		sb.append("taskId: " + this.taskId + ", ");
		sb.append("taskType: " + this.taskType + ", ");
		sb.append("successor: " + this.successorTaskId);
		sb.append("join counter " + this.joinCounter);
		sb.append("}");
		return sb.toString();
	}
	

}
