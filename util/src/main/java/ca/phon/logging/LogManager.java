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
package ca.phon.logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ca.phon.util.OSInfo;
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
	
	private final static String DEFAULT_PROPERTIES_FILE = 
			"ca/phon/app/log/phonlog.xml";
	
	public final static String LOG_FILE = 
			PrefHelper.getUserDataFolder() + File.separator + "phon.log";
	
	public final static String LOG_FILEPATTERN = 
			PrefHelper.getUserDataFolder() + "File.separator" + "logs" + File.separator + "phon-%d{MM-dd-yyyy}-%i.log.gz";
	
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
		// read configuration from classpath
	}
	
	public void shutdownLogging() {
	}
	
	public String readLogFile() throws IOException {
		return readFile(LOG_FILE);
	}
	
	private String readFile(String filename) throws IOException {
		final StringBuffer buffer = new StringBuffer();
		
		final BufferedReader reader = 
				new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
		String line = null;
		while((line = reader.readLine()) != null) {
			buffer.append(line).append('\n');
		}
		reader.close();
		
		return buffer.toString();
	}
	
}
