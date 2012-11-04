/*
 * @author gautham
 */
package jobs;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import shared.DoubleShared;
import shared.Shared;
import solution.EuclideanTSPSolution;
import solution.Solution;
import tasks.BranchAndBound;
import api.Result;
import api.Space;
import api.Task;

/**
 * This class represents the entire work involved in solving a Traveling Salesman Problem (TSP), where the cities are points in the 2D Euclidean plane. * 
 */
public class EuclideanTspJob implements Job<int[]>{

	/** The cities in 2D Euclidean plane that are part of the TSP. */
	private double[][] cities;
	
	/**
	 * Instantiates a new Euclidean TSP task.
	 *
	 * @param cities the cities in 2D Euclidean plane that are part of the TSP; it codes the x and y coordinates of city[i]: cities[i][0] is the x-coordinate of city[i] and cities[i][1] is the y-coordinate of city[i]
	 */
	public EuclideanTspJob(double[][] cities){
		this.cities = cities;		
	}	
	
	
	/** Denotes the job start time. It used to record the time taken for execution of the task.*/
	private long startTime;
	
	
	/* (non-Javadoc)
	 * @see jobs.Job#generateTasks(api.Space)
	 */
	@Override
	public void generateTasks(Space space) {
		// prefix represents the path taken (permutation) from the root task up to the current task 
		int[] prefix = {0};
		// permutation represents the array that needs to be permuted for computing the minimal tour
		Integer[] permutation = new Integer[cities.length - 1];
		for(int i = 0; i < permutation.length; i++){
			permutation[i] = i + 1;
		}
		
		Input input = computeInitLowerBound();
		// The first task has level = 0
		Solution<Double> solution = new EuclideanTSPSolution(input, prefix, Arrays.asList(permutation));
		
		Task task = new BranchAndBound(solution, 0);
		// The initial upper bound is calculated using the greedy approach.
		double upperBound = getGreedyUpperBound();
		System.out.println("Init Upperbound: " + upperBound);
		Shared<Double> shared = new DoubleShared(upperBound);
		// setting root task's upper bound
		task.setInitUpperBound(shared);
		try{
			this.startTime = System.nanoTime();
			space.put(task);
		}
		catch(RemoteException e){
			e.printStackTrace();
		}
		catch(NullPointerException e){
			e.printStackTrace();
		}
	}

	/**
	 * Collects the computed result that represents the composed result of the execution of all the tasks
	 * The result in the EuclideanTSP job is a map of the minimal tour among the permutations computed by the tasks and the cost involved for that tour.
	 * A map is used to store the minimum distance computed for each task so that it need not be re-computed each time when composing tasks.  
	 * @param space the space
	 * @return int[]
	 */
	@Override
	public int[] collectResults(Space space) {
		System.out.println("Collect Results");
		int[] minTour = null;		
		Solution minSolution = null;
		try {
			Result<Solution> result = space.take();
			long elapsedTime = System.nanoTime() - this.startTime;
			System.out.println("Elapsed Time for the task: " + elapsedTime + " ns");
			minSolution = result.getTaskReturnValue();
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}catch(NullPointerException e){
			e.printStackTrace();
		}
		System.out.println("Min Tour: " + Arrays.toString(minSolution.getPathFromRoot()));
		System.out.println("Min distance: " + minSolution.getLowerBound());
		return minSolution.getPathFromRoot();
	}
	
	
	/**
	 * Calculates the distance between the cities in the order passed.
	 *
	 * @param order the order
	 * @return double
	 */
	private double calculateDistance(int[] order){
		double distance = 0;
		for(int j = 0; j < order.length - 1; j++){
			distance += getEuclideanDistance(cities[order[j]], cities[order[j + 1]]);				
		}
		distance += getEuclideanDistance(cities[order[order.length - 1]], cities[0]);
		return distance;
	}
	
	/**
	 * Calculate the Euclidean distance.
	 *
	 * @param pointA the starting point
	 * @param pointB the ending point
	 * @return distance the distance between the points
	 */
	private double getEuclideanDistance(double[] pointA, double[] pointB){		
		double temp1 = Math.pow((pointA[0] - pointB[0]), 2);
		double temp2 = Math.pow((pointA[1] - pointB[1]), 2);
		double distance = Math.sqrt(temp1 + temp2);
		return distance;
	}
	
	
	/**
	 * Gets the greedy upper bound.
	 *
	 * @return the greedy upper bound
	 */
	private double getGreedyUpperBound(){
		double upperBound = 0;
		// list to store the visited cities
		List<Integer> visitedCities = new ArrayList<Integer>();
		visitedCities.add(0);
		for(int i = 0; i < visitedCities.size(); i++){
			int index = 0;
			Map<Double, List<Integer>> distanceMap = new HashMap<Double, List<Integer>>();
			double[] distance = new double[cities.length - 1];
			for(int j = 0; j < cities.length; j++){
				if(j != visitedCities.get(i)){
					//System.out.println("Distance between " + prefix[i] + " and " + j);
					distance[index] = getEuclideanDistance(cities[visitedCities.get(i)], cities[j]);
					List<Integer> list = distanceMap.get(distance[index]);
					if(list == null){
						list = new LinkedList<Integer>();
					}
					list.add(j);
					distanceMap.put(distance[index], list);
					index++;
				}
			}
			Arrays.sort(distance);
			
			boolean cityAdded = false;
			for(int k = 0; k < distance.length; k++){
				List<Integer> cityList = distanceMap.get(distance[k]);
				for(Integer city : cityList){
					if(! visitedCities.contains(city)){
						upperBound += distance[k];
						visitedCities.add(city);
						cityAdded = true;
						break;
					}
				}
				if(cityAdded){
					break;
				}
			}
		}		
		upperBound += getEuclideanDistance(cities[visitedCities.get(visitedCities.size() - 1)], cities[0]);
		return upperBound;
	}
	
	private Input computeInitLowerBound(){		
		Input input = new Input();
		Map<Integer, double[]> minDistance = new HashMap<Integer, double[]>();
		double[][] distanceMatrix = new double[cities.length][cities.length];
		for(int i = 0; i < cities.length; i++){
			int index = 0;
			double[] distance = new double[cities.length - 1];
			for(int j = 0; j < cities.length; j++){
				if(j != i){
					distanceMatrix[i][j] = getEuclideanDistance(cities[i], cities[j]);
					distance[index++] = distanceMatrix[i][j];
				}
			}
			Arrays.sort(distance);			
			minDistance.put(i, new double[]{distance[0], distance[1]});			
		}
		input.setDistanceMatrix(distanceMatrix);
		input.setMinDistance(minDistance);
		return input;	
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
//		double[][] cities = { { 1, 1 }, { 8, 1 }, { 8, 8 }, { 1, 8 },
//				{ 2, 2 }, { 7, 2 }, { 7, 7 }, { 2, 7 }, { 3, 3 }, { 6, 3 },
//				{ 6, 6 }, { 3, 6 }, { 4, 4 }, { 5, 4 }};
		double[][] cities = { { 1, 1 }, { 8, 1 }, { 8, 8 }, { 1, 8 }, { 2, 2 },
				{ 7, 2 }, { 7, 7 }, { 2, 7 }, { 3, 3 }, { 6, 3 }, {6, 6}, {3, 6} };
		EuclideanTspJob tspJob = new EuclideanTspJob(cities);
		Input input = tspJob.computeInitLowerBound();
		double distance = input.getDistanceMatrix()[0][1] + input.getDistanceMatrix()[1][0];
		System.out.println("Distance from distance matrix " + distance);
		//System.out.println("Init Upper Bound: " + tspJob.getGreedyUpperBound());
		int[] arr = {0, 1};
		System.out.println("Distance " + tspJob.calculateDistance(arr));
	}
	
	
}
