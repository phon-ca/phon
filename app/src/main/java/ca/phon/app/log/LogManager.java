/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.log;

import ca.phon.util.PrefHelper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;

import java.io.*;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Method for setup and management of application logs.
 */
public class LogManager {

	/**
	 * Log manager shared instance
	 */
	private final static LogManager _instance = new LogManager();
	
	public final static String LOG_FOLDER =
			PrefHelper.getUserDataFolder() + File.separator + "logs";
	
	public final static String LOG_FILE = 
			LOG_FOLDER + File.separator + "phon.log.html";
	
	public final static String LOG_FILEPATTERN = 
			LOG_FOLDER + File.separator + "phon-%i.log.html.gz";
	
	private final static String LOG_FILEREGEX = "phon-.+\\.log\\.html\\.gz";
	
	public static LogManager getInstance() {
		return _instance;
	}
	
	private LogManager() {
	}
	
	public File[] getPreviousLogs() {
		var logFolder = new File(LOG_FOLDER);
		if(logFolder.exists()) {
			var logFiles = logFolder.listFiles( (path) -> path.getName().matches(LOG_FILEREGEX) );
			return logFiles;
		} else {
			return new File[0];
		}
	}
	
	public void setupLogging() {
		// turn off java.util.logging and re-direct all messages to log4j
		java.util.logging.LogManager.getLogManager().reset();
		
		var handler = new Handler() {
			@Override
			public void publish(LogRecord record) {
				Level level = Level.INFO;
				if(record.getLevel() == java.util.logging.Level.SEVERE) {
					level = Level.ERROR;
				} else if(record.getLevel() == java.util.logging.Level.WARNING) {
					level = Level.WARN;
				} else if(record.getLevel() == java.util.logging.Level.FINE) {
					level = Level.TRACE;
				}
				LogUtil.log(level, record.getMessage(), record.getThrown());
			}
			
			@Override
			public void flush() {
				
			}
			
			@Override
			public void close() throws SecurityException {
				
			}
			
		};
		
		var caPhonLogger = java.util.logging.Logger.getLogger("ca.phon");
		caPhonLogger.addHandler(handler);			
		
		// setup rolling file appender add add to root logger
		final RollingFileAppender ap = RollingFileAppender.newBuilder()
			.withName("PhonRollingFileAppender")
			.withFileName(LOG_FILE)
			.withFilePattern(LOG_FILEPATTERN)
			.withPolicy(OnStartupTriggeringPolicy.createPolicy(0L))
			.withStrategy(DefaultRolloverStrategy.newBuilder().withMax("10").build())
			.withLayout(new LogLayout())
			.build();

		final LoggerContext ctx = (LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        config.getRootLogger().addAppender(ap, Level.DEBUG, null);
        ctx.updateLoggers();
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
