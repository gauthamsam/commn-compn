/*
 * @author gautham
 */
package shared;

/**
 * Represents the shared object that stores the upper bound on the cost of a minimal solution as a double value. 
 */
public class DoubleShared implements Shared<Double>{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The shared value. */
	private double shared;
	
	/**
	 * Instantiates a new double shared.
	 *
	 * @param shared the shared
	 */
	public DoubleShared(double shared){
		this.shared = shared;
	}
	
	/* (non-Javadoc)
	 * @see shared.Shared#get()
	 */
	@Override
	public synchronized Double get() {
		return this.shared;
	}

	/* (non-Javadoc)
	 * @see shared.Shared#isNewerThan(shared.Shared)
	 */
	@Override
	public synchronized boolean isNewerThan(Shared<Double> shared) {
		return this.shared < shared.get();
	}

}
