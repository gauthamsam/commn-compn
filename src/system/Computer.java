/*
 * @author gautham
 */
package system;

import java.rmi.Remote;
import java.rmi.RemoteException;

import shared.Shared;

import api.Task;

/**
 * This is the remote interface through which different tasks can be executed by the ComputeSpace.
 * These tasks are run using the task's implementation of the execute method and the results are returned to the ComputeSpace.
 * Each task can either be atomic and executed right away or be decomposed into multiple subtasks. In the earlier case, the result is stored in the space while in the latter case, the subtasks are stored.
 * Any object that implements this interface can be a remote object.
 */
public interface Computer extends Remote{
	
	/**
	 * A remote method to which different tasks can be submitted by the remote clients and executed.
	 * These tasks are run using the task's implementation of the execute method and the results are returned to the remote client.
	 *
	 * @param <T> the generic type
	 * @param t the task
	 * @return result
	 * @throws RemoteException the remote exception
	 */
	public <T> void execute(Task<T> t) throws RemoteException;
	
	/**
	 * Stop the compute instance.
	 *
	 * @throws RemoteException the remote exception
	 */
	public void exit() throws RemoteException;
	

	/**
	 * Sets the remote reference to space (Space Proxy).
	 *
	 * @param space the new space
	 * @throws Exception the exception
	 */
	public void setSpace(Computer2Space space) throws Exception;
	
	/**
	 * Sets the computer id.
	 *
	 * @param computerId the new computer id
	 * @throws RemoteException the remote exception
	 */
	public void setComputerId(int computerId) throws RemoteException;
	
	/**
	 * Gets the computer id.
	 *
	 * @return the computer id
	 * @throws RemoteException the remote exception
	 */
	public int getComputerId() throws RemoteException;
	
	/**
	 * Sets the shared object.
	 * The task calls the computer’s setShared method when it finds a minimal solution that is less than the computer's current upper bound.
 	 * The computer, in turn propagates the proposed newer shared object to the Space by calling the Space’s setShared method (using a Space Proxy), only if the proposed shared object is newer than the computer's existing shared object. 
 	 * Similarly, the Space propagates the received shared object to all the computers except the one from which it received the shared object, only if it is newer than the Space's shared object.
 	 * The Computer need not propagate the shared value to Space when it receives a new shared value from Space itself.  
	 *
	 * @param shared the shared object
	 * @param canPropagate the boolean value which lets the computer know if the shared object can be propagated to the Space.
	 * @throws RemoteException the remote exception
	 */
	public void setShared(Shared<?> shared, boolean canPropagate) throws RemoteException;
	
	/**
	 * Gets the shared object.
	 *
	 * @return the shared
	 * @throws RemoteException the remote exception
	 */
	public Shared getShared() throws RemoteException;
	
	
	/**
	 * Starts the workers threads on the Computer. The number of workers is equal to the number of processors that is available to the JVM.
	 *
	 * @throws RemoteException the remote exception
	 */
	public void startWorkers() throws RemoteException;
}