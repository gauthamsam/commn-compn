/*
 * @author gautham
 */
package system;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class loads all the performance enhancement settings from the system properties file and stores it. 
 */
public class PerformanceSettings {
	
	/** The exploit multicore processors. */
	private static boolean exploitMulticoreProcessors;
	
	/** The ameliorate communication latency. */
	private static boolean ameliorateCommunicationLatency;

	/** The properties. */
	private static Properties properties = new Properties();
	
	/** The file. */
	private static File file = new File("src/system.properties");;
	
	/** The last modified. */
	private static long lastModified;
	
	static{
		loadProperties();		
	}
	
	/**
	 * Checks if is exploit multicore processors.
	 *
	 * @return true, if is exploit multicore processors
	 */
	public static boolean isExploitMulticoreProcessors() {
		// checkProperties();
		return exploitMulticoreProcessors;
	}

	/**
	 * Sets the exploit multicore processors.
	 *
	 * @param exploitMulticoreProcessors the new exploit multicore processors
	 */
	private static void setExploitMulticoreProcessors(
			boolean exploitMulticoreProcessors) {
		PerformanceSettings.exploitMulticoreProcessors = exploitMulticoreProcessors;
	}

	/**
	 * Checks if is ameliorate communication latency.
	 *
	 * @return true, if is ameliorate communication latency
	 */
	public static boolean isAmeliorateCommunicationLatency() {
		// checkProperties();
		return ameliorateCommunicationLatency;
	}

	/**
	 * Sets the ameliorate communication latency.
	 *
	 * @param ameliorateCommunicationLatency the new ameliorate communication latency
	 */
	private static void setAmeliorateCommunicationLatency(
			boolean ameliorateCommunicationLatency) {
		PerformanceSettings.ameliorateCommunicationLatency = ameliorateCommunicationLatency;
	}

	/**
	 * Load properties.
	 */
	private static void loadProperties(){
		System.out.println("Loading properties.");
		try {			
			lastModified = System.currentTimeMillis();
			InputStream in = new FileInputStream(file);
			properties.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setAmeliorateCommunicationLatency(Boolean.parseBoolean(properties.getProperty("AMELIORATE_COMMUNICATION_LATENCY")));
		System.out.println("AMELIORATE_COMMUNICATION_LATENCY " + isAmeliorateCommunicationLatency());
		setExploitMulticoreProcessors(Boolean.parseBoolean(properties.getProperty("EXPLOIT_MULTICORE_PROCESSORS")));
		System.out.println("EXPLOIT_MULTICORE_PROCESSORS " + isExploitMulticoreProcessors());
	}
	
	/**
	 * Check properties.
	 */
	private static void checkProperties(){
		if(file.lastModified() > lastModified){
			loadProperties();
		}
	}
}
