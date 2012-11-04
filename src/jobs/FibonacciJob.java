/*
 * @author gautham
 */
package jobs;

import java.rmi.RemoteException;

import tasks.FibonacciTask;
import utils.Constants;
import api.Result;
import api.Space;

/**
 * This class represents the entire work involved in computing the Fibonacci sum of a given value 
 */
public class FibonacciJob implements Job<Integer> {

	
	/** The value for which the fibonacci sum needs to be computed. */
	private int n;

	/** Denotes the job start time. It used to record the time taken for execution of the task.*/
	private long startTime; 
	
	/**
	 * Instantiates a new fibonacci job.
	 *
	 * @param n the n
	 */
	public FibonacciJob(int n){
		this.n = n;
	}
	
	/* (non-Javadoc)
	 * @see jobs.Job#generateTasks(api.Space)
	 */
	@Override
	public void generateTasks(Space space) {
		FibonacciTask task = new FibonacciTask(n, Constants.CHILD_TASK, 0);
		this.startTime = System.nanoTime();
		try{
			space.put(task);
		}
		catch(RemoteException e){
			e.printStackTrace();
		}		
		
	}

	/* (non-Javadoc)
	 * @see jobs.Job#collectResults(api.Space)
	 */
	@Override
	public Integer collectResults(Space space) {
		Result<Integer> result = null;
		try {
			result = space.take();
			long elapsedTime = System.nanoTime() - this.startTime;
			System.out.println("Elapsed Time for the task: " + elapsedTime + " ns");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result.getTaskReturnValue();
	}

}
