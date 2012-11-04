/*
 * @author gautham
 */
package jobs;

import api.Space;

/**
 * The Job represents the work to be done from the Client's perspective and it represents the root task in a Cilk scheduler. 
 * @param <T> a type parameter, T, which represents the result type of the job computation.
 */
public interface Job<T> {

	/**
	 * Generates task(s) from this job. The client puts the entire job into the Space which are then decomposed and executed by the Computers
	 * @param space the Compute space
	 */
	public void generateTasks(Space space);
	
	/**
	 * Collects result(s) from the Space, obtaining the composed result of execution of all the subtasks.
	 *
	 * @param space the space
	 * @return t
	 */
	public T collectResults(Space space);
	
}
