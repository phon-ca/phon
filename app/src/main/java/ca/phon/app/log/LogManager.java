package ca.phon.app.log;

import java.io.IOException;
import java.io.InputStream;

import ca.phon.util.PrefHelper;

/**
 * Method for setup and management of application logs.
 */
public class LogManager {

	/**
	 * Log manager shared instance
	 */
	private final static LogManager _instance = new LogManager();
	
	public final static String PROPERTIES_FILE_LOCATION = LogManager.class.getName() + ".logProps";
	
	private final static String DEFAULT_PROPERTIES_FILE = "phonlog.properties";
	
	public static LogManager getInstance() {
		return _instance;
	}
	
	private LogManager() {
	}
	
	private InputStream getLogProps() {
		return getClass().getClassLoader().getResourceAsStream(
				PrefHelper.get(PROPERTIES_FILE_LOCATION, DEFAULT_PROPERTIES_FILE));
	}
	
	public void setupLogging() {
		final java.util.logging.LogManager manager = java.util.logging.LogManager.getLogManager();
		try {
			manager.readConfiguration(getLogProps());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
