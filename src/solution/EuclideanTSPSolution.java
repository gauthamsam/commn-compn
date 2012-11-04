/*
 * @author gautham
 */
package solution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import jobs.Input;
import shared.Shared;

/**
 * The EuclideanTSPSolution represents the partial tour (from the root to the current node) in the search tree of a Traveling Salesman Problem (TSP).
 */
public class EuclideanTSPSolution extends Solution<Double>{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	
	/** The taskPermutation denotes the permutation of cities that are yet to be explored from this node.*/
	private List<Integer> taskPermutation;
	
	/**
	 * Instantiates a new euclidean tsp solution.
	 *
	 * @param input the input
	 * @param pathFromRoot the path from root
	 * @param taskPermutation the task permutation
	 */
	public EuclideanTSPSolution(Input input, int[] pathFromRoot, List<Integer> taskPermutation){
		this.input = input;
		this.pathFromRoot = pathFromRoot;
		this.taskPermutation = taskPermutation;
	}
	
	/* (non-Javadoc)
	 * @see solution.Solution#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return pathFromRoot.length == input.getDistanceMatrix().length;
	}

	/* (non-Javadoc)
	 * @see solution.Solution#getChildren(shared.Shared)
	 */
	@Override
	public Queue<Solution<Double>> getChildren(Shared shared) {
		Queue<Solution<Double>> children = new LinkedList<Solution<Double>>();
		System.out.println("Task permutation size " + taskPermutation.size());
		if(taskPermutation != null){
			for(int i = 0, n = taskPermutation.size(); i < n; i++){
				int[] pathFromRoot = Arrays.copyOfRange(this.pathFromRoot, 0, this.pathFromRoot.length + 1);
				pathFromRoot[pathFromRoot.length - 1] = taskPermutation.get(i);
				
				List<Integer> childTaskPermutation = new ArrayList<Integer>(taskPermutation);
				
				childTaskPermutation.remove(i);
				
				Solution<Double> solution = new EuclideanTSPSolution(input, pathFromRoot, childTaskPermutation);
				solution.computeLowerBound();
				// System.out.println("Shared " + shared);
				double upperBound = (Double) shared.get();
				//System.out.println("Upper bound " + upperBound);
				if(solution.lowerBound <= upperBound){
					//System.out.println("Adding child");
					children.add(solution);
				}
			}
		}
		return children;
	}

	/* (non-Javadoc)
	 * @see solution.Solution#getLowerBound()
	 */
	@Override
	public Double getLowerBound() {
		return lowerBound;
	}

	/* (non-Javadoc)
	 * @see solution.Solution#getPathFromRoot()
	 */
	@Override
	public int[] getPathFromRoot() {
		return pathFromRoot;
	}

	/** 
	 * The lower bound is computed by adding up the euclidean distance between the nodes in the path from root.
	 * @see solution.Solution#computeLowerBound()
	 */
	@Override
	public void computeLowerBound(){
		
		Map<Integer, double[]> minDistance = input.getMinDistance();
		double[][] distanceMatrix = input.getDistanceMatrix();
		
		for(int i = 0; i < pathFromRoot.length - 1; i++){
			lowerBound += distanceMatrix[pathFromRoot[i]][pathFromRoot[i+1]];
		}
		if(this.isComplete()){
			lowerBound += distanceMatrix[pathFromRoot[pathFromRoot.length - 1]][0];
			return;
		}
		
		// cost of the minimum of the 2 virtual edges for root node
		lowerBound += minDistance.get(0)[0];
		
		// cost of the minimum of the 2 virtual edges for last node in the partial tour.
		//lowerBound += minDistance.get(pathFromRoot[pathFromRoot.length - 1])[0];
		
		// Now, for the remaining vertices, calculate the lowerBound.
		
		//lowerBound += distanceMatrix[pathFromRoot[pathFromRoot.length - 1]][taskPermutation.get(0)];
		double lowerBound_taskPermutation = computeLowerBoundForPermutation();
		
		
		lowerBound += lowerBound_taskPermutation;
				
		//lowerBound /= 2;
		//int lastCity = false ? pathFromRoot[pathFromRoot.length - 1] : taskPermutation.get(taskPermutation.size() - 1); 
		// distance between the last vertex in the path and the root
		//lowerBound += distanceMatrix[lastCity][0];
		//lowerBound /= 1.2;
		//System.out.println(Arrays.toString(pathFromRoot) + "->" + lowerBound);
		//System.out.println("Final lower bound " + lowerBound);
	}
	
	/**
	 * Compute lower bound for the permutation of cities that are yet to be explored from this node.
	 *
	 * @return double
	 */
	private double computeLowerBoundForPermutation(){		
		//System.out.println("input " + input);
		//System.out.println("task Permutation " + taskPermutation);
		double[][] distanceMatrix = input.getDistanceMatrix();
		double lowerBound = 0;
		int size = taskPermutation.size();
		if(size == 2){
			return distanceMatrix[taskPermutation.get(0)][taskPermutation.get(1)];			
		}
		else if (size == 1){
			return 0;
		}
		for(int i = 0; i < size; i++){
			int index = 0;
			double[] distance = new double[size - 1];
			for(int j = 0; j < size; j++){
				if(j != i){
					//distanceMatrix[i][j] = getEuclideanDistance(cities[taskPermutation.get(i)], cities[taskPermutation.get(j)]);
					distance[index++] = distanceMatrix[taskPermutation.get(i)][taskPermutation.get(j)];
				}
			}
			Arrays.sort(distance);
			lowerBound += distance[0] + distance[1];
			//minDistance.put(i, new double[]{distance[0], distance[1]});			
		}
		lowerBound /= 2;
		//System.out.println("lower bound for taskPermutation " + Arrays.toString(taskPermutation.toArray()) + ": " + lowerBound);
		return lowerBound;	
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("pathFromRoot " + Arrays.toString(pathFromRoot));
		sb.append("taskPermutation " + Arrays.toString(taskPermutation.toArray()));
		return sb.toString();
	}

	/**
	 * Compares the lower bound of this partial solution to the given solution.	 *
	 *
	 * @param solution the solution
	 * @return true, if successful
	 * @see solution.Solution#compareTo(solution.Solution)
	 */
	@Override
	public boolean compareTo(Solution<?> solution) {
		return (Double)this.getLowerBound() <= (Double)solution.getLowerBound();
	}
	
}
