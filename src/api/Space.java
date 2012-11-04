/*
 * @author gautham
 */
package api;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The remote interface through which different tasks are submitted and results obtained.
 * It acts as a channel for passing messages between Client and ComputeServers.
 * It stores all the intermediate results and the subtasks that are produced after decomposing each task.
   The implementation defines mechanisms to wait for an available Task, to hold them and assign it to the ComputeServers and then process the Result objects.
 */
public interface Space extends Remote {

	/** The name under which the RMI registry binds the remote reference. */
	public static final String SERVICE_NAME = "Space";
	
	/**
	 * A remote method used by the Clients to put the Task into the ComputeSpace.
	 *
	 * @param <T> the generic type
	 * @param task the actual task
	 * @throws RemoteException the remote exception
	 */
	<T> void put( Task<T> task ) throws RemoteException;
	
	
	/**
	 * A remote method to take the Result that has been computed by the ComputeServers. This method blocks until a Result is available to return to the client
	 *
	 * @param <T> the generic type
	 * @return result
	 * @throws RemoteException the remote exception
	 * @throws InterruptedException the interrupted exception
	 */
	<T> Result<T> take() throws RemoteException, InterruptedException;
	
	
	/**
	 * A remote method to stop the execution of the ComputeSpace. This in turn will stop all the Computers that are registered.
	 *
	 * @throws RemoteException the remote exception
	 */
	void stop() throws RemoteException;	
	
}
