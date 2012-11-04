/*
 * @author gautham
 */
package shared;

import java.io.Serializable;

/**
 * Represents the shared object that stores the upper bound on the cost of a minimal solution.
 * All tasks have access to this shared object.
 * To propagate a newer shared object to other tasks, the task invokes its setShared method which calls the executing computer’s setShared method. 
 * This, in turn propagates the proposed newer shared object to the Space by calling the Space’s setShared method (using a Space Proxy), only if the proposed shared object is newer than the computer's existing shared object. Similarly, the Space propagates the received shared object to all the computers except the one from which it received the shared object,  only if it is newer than the Space's shared object.
 *
 * @param <T> the generic type
 */
public interface Shared<T> extends Serializable{

	/**
	 * Gets the shared value.
	 *
	 * @return t
	 */
	public T get();
	
	/**
	 * Checks if this shared object is newer than the passed shared object
	 *
	 * @param shared the shared
	 * @return true, if is newer than
	 */
	public boolean isNewerThan(Shared<T> shared);
}
