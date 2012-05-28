package ca.phon.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Methods for determining the version number of the application.
 * 
 * Version information is read from the file: build.properties
 * which should be located in the root of the class loader.
 */
public class VersionInfo {
	
	/**
	 * Properties file 
	 */
	private final static String VERSION_PROP_FILE = "/build.properties";
	
	/**
	 * Major version prop name
	 */
	private final static String MAJOR_VERSION = "build.major";
	
	/**
	 * Minor version prop name
	 */
	private final static String BUILD_MINOR = "build.minor";
	
	/**
	 * Revision
	 */
	private final static String BUILD_REVISION = "build.revision";
	
	/**
	 * Source Control revision 
	 */
	private final static String BUILD_SCREVISION = "build.screvision";
	
	/**
	 * The shared instance
	 */
	private static VersionInfo _instance;
	
	/**
	 * Loaded properties
	 */
	private Properties versionProps;

	/**
	 * Get the shared instance
	 */
	public static VersionInfo getInstance() {
		if(_instance == null) {
			_instance = new VersionInfo();
			_instance.versionProps = new Properties();
			
			try {
				InputStream is = VersionInfo.class.getResourceAsStream(VERSION_PROP_FILE);
				if(is == null) {
					_instance.versionProps.put(MAJOR_VERSION, "1");
					_instance.versionProps.put(BUILD_MINOR, "5");
					_instance.versionProps.put(BUILD_REVISION, "2");
					_instance.versionProps.put(BUILD_SCREVISION, "XXXXXXXXXXXX");
				} else {
					_instance.versionProps.load(is);
				}
			} catch (IOException e) {
				e.printStackTrace();
				Logger.getLogger(VersionInfo.class.getName()).severe(e.getMessage());
			}
		}
		return _instance;
	}
	
	/**
	 * Get major version 
	 */
	public String getMajorVersion() {
		return versionProps.getProperty(MAJOR_VERSION);
	}
	
	/**
	 * Get minor version
	 */
	public String getMinorVersion() {
		return versionProps.getProperty(BUILD_MINOR);
	}
	
	/**
	 * Revision
	 */
	public String getRevision() {
		return versionProps.getProperty(BUILD_REVISION);
	}
	
	/**
	 * Source control number
	 */
	public String getScRevision() {
		return versionProps.getProperty(BUILD_SCREVISION);
	}
	
	/**
	 * Return version as:
	 * 
	 * <major>.<minor>.<build>
	 */
	public String getVersion() {
		String retVal = 
			getMajorVersion() + "." +
			getMinorVersion() + "." + 
			getRevision();
		return retVal;
	}
	
	/**
	 * Return long version
	 * 
	 * <major>.<minor>.<build> <revision>
	 */
	public String getLongVersion() {
		String retVal = 
			getVersion() + " " + getScRevision();
		return retVal;
	}
	
}
