/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Methods for determining the version number of the application.
 * 
 * Version information is read from the file: build.properties
 * which should be located in the root of the class loader.
 */
public class VersionInfo {
	
	private final static Logger LOGGER = Logger.getLogger(VersionInfo.class.getName());
	
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
					_instance.versionProps.put(MAJOR_VERSION, "2");
					_instance.versionProps.put(BUILD_MINOR, "2");
					_instance.versionProps.put(BUILD_REVISION, ".0");
					_instance.versionProps.put(BUILD_SCREVISION, "XXXXXXXXXXXX");
				} else {
					_instance.versionProps.load(is);
				}
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.severe(e.getMessage());
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
