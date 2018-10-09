/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Methods for determining the version number of the application.
 * 
 * Version information is read from the file: build.properties
 * which should be located in the root of the class loader.
 */
public class VersionInfo {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(VersionInfo.class.getName());
	
	/**
	 * Properties file 
	 */
	private final static String VERSION_PROP_FILE = "phon.build.properties";
	
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
	
	private final static String BUILD_CODENAME = "build.codename";
	
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
				InputStream is = VersionInfo.class.getClassLoader().getResourceAsStream(VERSION_PROP_FILE);
				if(is == null) {
					_instance.versionProps.put(MAJOR_VERSION, "3");
					_instance.versionProps.put(BUILD_MINOR, "0");
					_instance.versionProps.put(BUILD_REVISION, ".1");
					_instance.versionProps.put(BUILD_SCREVISION, "XXXXXXXXXXXX");
				} else {
					_instance.versionProps.load(is);
				}
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.error(e.getMessage());
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
			getMinorVersion() + 
			getRevision();
		return retVal;
	}
	
	public String getShortVersion() {
		String retVal = 
				getMajorVersion() + "." +
				getMinorVersion();
		return retVal;
	}
	
	/**
	 * Return long version
	 * 
	 * <major>.<minor>.<build> <revision>
	 */
	public String getLongVersion() {
		String retVal = 
			getVersion() + " (" + getScRevision() + ")";
		return retVal;
	}
	
	public String getCodename() {
		return versionProps.getProperty(BUILD_CODENAME);
	}
	
}
