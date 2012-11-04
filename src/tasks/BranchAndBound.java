/*
 * @author gautham
 */
package tasks;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.UUID;

import shared.DoubleShared;
import shared.Shared;
import solution.EuclideanTSPSolution;
import solution.Solution;
import utils.Constants;
import api.Result;
import api.Task;

/**
 * This class represents a task involved in solving any combinatorial optimization problem (e.g. TSP) that uses the branch-and-bound technique. 
 */
public final class BranchAndBound extends Task<Solution<?>>{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	
	/** The partial solution corresponding to the node in the sub-tree that is being explored. */
	private Solution<?> solution;
	
	/**
	 * Instantiates a new branch-and-bound task.
	 *
	 * @param taskType the task type
	 */
	public BranchAndBound(int taskType){
		super(UUID.randomUUID(), taskType);
	}
	
	/**
	 * Instantiates a new branch and bound task.
	 *
	 * @param solution the partial solution that corresponds to the sub-tree that is to be explored
	 * @param level the level in the tree
	 */
	public BranchAndBound(Solution<Double> solution, int level){
		super(UUID.randomUUID(), Constants.CHILD_TASK, level);
		this.solution = solution;				
	}
	
	/**
	 *  Executes the Branch and Bound task. It either executes an atomic task or composes the result for a successor task 
	 *  If the task is an atomic task, then the search tree represented by the current node is explored and the minimum solution is computed.
	 *  Otherwise, if it is a successor task, the minimum solution is computed from all the (minimum) solutions corresponding to this successor's tasks. 
	 * @see api.Task#execute()
	 */
	@Override
	public Result<Solution<?>> execute() {
		
		Result<Solution<?>> result = new Result<Solution<?>>();
		Solution<?> minSolution = null;

		if(this.taskType == Constants.CHILD_TASK){ // atomic task; explore the sub-tree and find the minimum solution
			minSolution = searchSubTree();						
		}		
		else if(this.getInputList() != null){// Successor task; proceed only if it has children. It might be the case that all the children have been pruned.
			
			// Get the solution from all the sub-tasks corresponding to this successor task and compute the minimum  
			for(Task<?> task : this.getInputList()){
				Solution<?> solution = ((BranchAndBound)task).solution;
				if(solution == null){
					continue;
				}
				if(minSolution == null || (solution.compareTo(minSolution))){
					minSolution = solution;					
				}
				
			}
		}
		this.solution = minSolution;
		result.setTaskReturnValue(minSolution);
		this.setResult(result);
		return result;
	}
	
	/**
	 * Explore the search tree having the current node as the root and find the solution that is of minimum cost.
	 *
	 * @return solution the solution that has the minimum cost
	 */
	private Solution<?> searchSubTree(){
		// A Stack to hold the nodes traversed in the search tree.
		Stack<Solution<?>> stack = new Stack<Solution<?>>();
		// Push the current solution
		stack.push(solution);		
		
		Solution<?> minCostSolution = null;
		
		while(! stack.isEmpty()){
			// Pop each node and check to see if the sub-tree rooted at that node can be pruned.
			Solution<?> partialSolution = stack.pop();
			
			double lowerBound = (Double) partialSolution.getLowerBound();
			
			if(lowerBound >= getSharedValue()){ // Prune that sub-tree
				continue;
			}
			
			// Get all the immediate children
			Queue<?> children = null;
			try {
				children = partialSolution.getChildren(this.getShared());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			for(Object child : children){
				// If the child represents a complete solution, check if that solution is lesser than the upper bound
				if(((Solution<?>) child).isComplete()){
					double distance = (Double) ((Solution<?>)child).getLowerBound();
					if(distance <= getSharedValue()){
						//System.out.println("Partial tour " + Arrays.toString(((Solution<?>)child).getPathFromRoot()));
						//System.out.println("Setting shared value " + distance);
						setSharedValue(distance);
						minCostSolution = (Solution<?>) child;
					}
				}
				else{ // The child doesn't represent a complete solution yet; push the child to the stack.
					stack.push((Solution<?>) child);
				}
			}
		}		
		return minCostSolution;
	}
	
	
	/**
	 * Gets the shared value.
	 *
	 * @return the shared value
	 */
	private double getSharedValue(){
		double sharedValue = 0;
		try {
			DoubleShared shared = (DoubleShared) this.getShared();
			sharedValue = shared == null ? Double.MAX_VALUE : shared.get().doubleValue();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return sharedValue;
	}
	
	/**
	 * Sets the shared value.
	 *
	 * @param d the new shared value
	 */
	private void setSharedValue(double d){
		Shared<Double> shared = new DoubleShared(d);
		try {
			this.setShared(shared);			
		} catch (RemoteException e) {				
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see api.Task#isAtomic()
	 */
	@Override
	public boolean isAtomic() {
		return this.level == Constants.BB_BASE_LEVEL;
	}

	/**
	 * Splits the node (task) and constructs smaller Branch and Bound tasks that correspond to its children
	 *
	 * @return list
	 * @see api.Task#splitTask()
	 */
	@Override
	public List<Task<Solution<?>>> splitTask() {
		List<Task<Solution<?>>> tasks = new ArrayList<Task<Solution<?>>>();
		try {
			int argNo = 0;
			Queue<?> children = this.solution.getChildren(this.getShared());
			// The child list can be empty if all the children have a lower bound that is greater the current upper bound
			while (!children.isEmpty()) {
				Solution child = (Solution) children.remove();
				// Construct new Branch and Bound tasks
				Task<Solution<?>> task = new BranchAndBound(child, this.level + 1);
				task.setArgNo(argNo++);
				tasks.add(task);
			}
		}
		catch(Exception e){
			e.printStackTrace();			
		}
		return tasks;
	}
	
	
	/* (non-Javadoc)
	 * @see api.Task#createSuccessorTask()
	 */
	@Override
	public Task<Solution<?>> createSuccessorTask() {
		Task<Solution<?>> successorTask = new BranchAndBound(Constants.SUCCESSOR_TASK);
		return successorTask;
	}
	
	
	/* (non-Javadoc)
	 * @see api.Task#toString()
	 */
	/*@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("taskId: " + this.taskId + ", ");
		sb.append("taskType: " + this.taskType + ", ");
		if(this.solution != null){
			sb.append("prefix: " + Arrays.toString(this.solution.getPathFromRoot()) + ", ");
		}
		else{
			sb.append("prefix: null" + ", ");
		}
		sb.append("successor: " + this.successorTaskId);
		sb.append("inputList " + this.getInputList());
		sb.append("}");
		return sb.toString();
	}
	*/
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
//		double[][] cities = { { 1, 1 }, { 8, 1 }, { 8, 8 }, { 1, 8 },
//				{ 2, 2 }, { 7, 2 }, { 7, 7 }, { 2, 7 }, { 3, 3 }, { 6, 3 },
//				{ 6, 6 }, { 3, 6 }, { 4, 4 }, { 5, 4 }, { 5, 5 }, { 4, 5 } };
		double[][] cities = { { 1, 1 }, { 8, 1 }, { 8, 8 }, { 1, 8 }, { 2, 2 },
				{ 7, 2 }, { 7, 7 }, { 2, 7 }, { 3, 3 }, { 6, 3 }, {6, 6}, {3, 6} };
		// prefix represents the path taken (permutation) from the root task up to the current task 
		int[] prefix = {0};
		// permutation represents the array that needs to be permuted for computing the minimal tour
		Integer[] permutation = new Integer[cities.length - 1];
		for(int i = 0; i < permutation.length; i++){
			permutation[i] = i + 1;
		}
		DoubleShared shared = new DoubleShared(Double.MAX_VALUE);
		// The first task has level = 0
		Solution<Double> solution = new EuclideanTSPSolution(null, prefix, Arrays.asList(permutation));
				
		BranchAndBound task = new BranchAndBound(solution, 0);
		
		task.splitTask();
		
		
	}


}
