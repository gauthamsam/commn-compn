/*
 * @author gautham
 */
package jobs;

import java.io.Serializable;
import java.util.Map;

/**
 * The Class Input.
 */
public class Input implements Serializable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The distance matrix. */
	private double[][] distanceMatrix;
	
	/** The min distance. */
	private Map<Integer, double[]> minDistance;
	
	/**
	 * Gets the distance matrix.
	 *
	 * @return the distance matrix
	 */
	public double[][] getDistanceMatrix() {
		return distanceMatrix;
	}

	/**
	 * Sets the distance matrix.
	 *
	 * @param distanceMatrix the new distance matrix
	 */
	public void setDistanceMatrix(double[][] distanceMatrix) {
		this.distanceMatrix = distanceMatrix;
	}

	/**
	 * Gets the min distance.
	 *
	 * @return the min distance
	 */
	public Map<Integer, double[]> getMinDistance() {
		return minDistance;
	}

	/**
	 * Sets the min distance.
	 *
	 * @param minDistance the min distance
	 */
	public void setMinDistance(Map<Integer, double[]> minDistance) {
		this.minDistance = minDistance;
	}
	
}
