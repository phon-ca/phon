/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

import java.awt.Font;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import ca.phon.worker.PhonTask;

/**
 * Component for displaying log messages.
 *
 */
public class LogConsole extends JTextArea {
	
	private static final long serialVersionUID = -7916501175515060956L;

	private final static Logger LOGGER = Logger
			.getLogger(LogConsole.class.getName());

	private final LogFormatter formatter = new LogFormatter();
	
	private LogConsole() {
		super();
		
		setEditable(false);
		setAutoscrolls(true);
		setFont(Font.decode("Monospace-PLAIN-12"));
	}
	
	/**
	 * Process log events for given logger.
	 * 
	 * @param logger
	 */
	public void addLogger(Logger logger) {
		logger.addHandler(logHandler);
	}
	
	/**
	 * Stop processing log events for given logger
	 * 
	 * @param logger
	 */
	public void removeLogger(Logger logger) {
		logger.removeHandler(logHandler);
	}
	
	// holds a cache of log messages to be displayed
	private final Handler logHandler = new Handler() {
		
		@Override
		public void publish(LogRecord record) {
			SwingUtilities.invokeLater(new PublishTask(record));
		}
		
		@Override
		public void flush() {
		}
		
		@Override
		public void close() throws SecurityException {
		}
		
	};
	
	private final class PublishTask extends PhonTask {

		private final LogRecord record;
		
		public PublishTask(LogRecord r) {
			super();
			this.record = r;
		}
		
		@Override
		public void performTask() {
			setStatus(TaskStatus.RUNNING);
			final String txt = formatter.format(record);
			try {
				getDocument().insertString(getDocument().getLength(), txt, null);
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
			setStatus(TaskStatus.FINISHED);
		}
		
	}
	
}
