package ca.phon.app.log;

import java.io.IOException;

/**
 * Method for setup and management of application logs.
 */
public class LogManager {

	/**
	 * Log manager shared instance
	 */
	private final static LogManager _instance = new LogManager();
	
	private final static String PROPERTIES_FILE = "phonlog.properties";
	
	public static LogManager getInstance() {
		return _instance;
	}
	
	private LogManager() {
	}
	
	public void setupLogging() {
		final java.util.logging.LogManager manager = java.util.logging.LogManager.getLogManager();
		try {
			manager.readConfiguration(getClass().getResourceAsStream("phonlog.properties"));
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
