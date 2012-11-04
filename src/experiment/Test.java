/*
 * @author gautham
 */
package experiment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A class to create an execution environment where the Client, Space, and computer are all instantiated in the same JVM.
 */
public class Test {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws ClassNotFoundException the class not found exception
	 * @throws SecurityException the security exception
	 * @throws NoSuchMethodException the no such method exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InterruptedException {
	    String[] classNames = {"system.SpaceImpl", "system.ComputerImpl", "client.Client"};
	    String[][] paramsArray = {{""}, {args[0]}, {args[0], args[1]}};
	    int index = 0;
	    for(String className : classNames){
			Class<?> cls = Class.forName(className);
		    Method meth = cls.getMethod("main", String[].class);
		    String[] params = paramsArray[index++]; // init params accordingly
		    meth.invoke(null, (Object) params); // static method doesn't have an instance
		    Thread.sleep(2000);
	    }
	}
}
