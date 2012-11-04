/*
 * @author gautham
 */
package solution;

import java.io.Serializable;
import java.util.Queue;

import jobs.Input;

import shared.Shared;

/**
 * The Solution object represents a partial feasible solution. 
 * The application programmers need to extend this class to represent the application-specific partial cost for a node in the search tree.
 *
 * @param <T> the generic type
 */
public abstract class Solution<T> implements Serializable{
	
	/** The Constant serialVersionUID. */
	protected static final long serialVersionUID = 1L;
	
	/** The lower bound. */
	protected double lowerBound;
	
	protected Input input;
	
	/** The path from root to the current node in the search tree. */
	protected int[] pathFromRoot;
	
	/**
	 * Checks if the path from root includes all the nodes that represent the given optmization problem.
	 *
	 * @return true, if it is complete
	 */
	public abstract boolean isComplete();
	
	/**
	 * Gets the children of the current node in the search tree.
	 *
	 * @param shared the shared object that has the upper bound
	 * @return the children
	 */
	public abstract Queue<Solution<T>> getChildren(Shared shared);
	
	/**
	 * Gets the lower bound.
	 *
	 * @return the lower bound
	 */
	public abstract T getLowerBound();
		
	/**
	 * Compute lower bound.
	 */
	protected abstract void computeLowerBound();
	
	/**
	 * Gets the path to this node from root.
	 *
	 * @return the path from root
	 */
	public abstract int[] getPathFromRoot();
		
	/**
	 * Checks if the current solution is better than the given solution. For instance, the comparison can be between the lowerbound values of two feasible solutions.
	 *
	 * @param solution solution to be compared with
	 * @return true, if this solution is better than the passed solution
	 */
	public abstract boolean compareTo(Solution<?> solution);
	
}
