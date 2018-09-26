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
package ca.phon.app.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.HtmlLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.jcraft.jsch.Logger;

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
	
	public final static String LOG_FOLDER =
			PrefHelper.getUserDataFolder() + File.separator + "logs";
	
	public final static String LOG_FILE = 
			LOG_FOLDER + File.separator + "phon.log.html";
	
	public final static String LOG_FILEPATTERN = 
			LOG_FOLDER + File.separator + "phon-%d{MM-dd-yyyy}-%i.log.html.gz";
	
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
			.withLayout(HtmlLayout.newBuilder().withTitle("Phon Log").withLocationInfo(true).build())
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
