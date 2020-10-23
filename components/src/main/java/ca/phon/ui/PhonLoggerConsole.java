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
package ca.phon.ui;

import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

import javax.swing.*;
import javax.swing.text.*;

/**
 * A panel which prints messages from a given {@link Logger}
 * 
 */
public class PhonLoggerConsole extends JTextPane {
	
	private static final long serialVersionUID = 7912735422196424586L;

	/**
	 * Map of Level icons
	 */
	private final Map<Level, AttributeSet> iconMap = 
			Collections.synchronizedMap(new HashMap<Level, AttributeSet>());
	
	/**
	 * Style map
	 */
	private final Map<Level, AttributeSet> styleMap =
			Collections.synchronizedMap(new HashMap<Level, AttributeSet>());
	
	/**
	 * Log formatter
	 */
	private Formatter formatter;
	
	public PhonLoggerConsole() {
		super();
		setEditable(false);
		
		formatter = new SimpleFormatter();
		
		setupStyles();
		setupIcons();
	}
	
	/**
	 * Set the icon used for the given log level.
	 * 
	 * @param level
	 * @param icon
	 * 
	 */
	public void setLevelIcon(Level level, ImageIcon icon) {
		final SimpleAttributeSet as = new SimpleAttributeSet();
		as.addAttribute(StyleConstants.IconAttribute, icon);
		iconMap.put(level, as);
	}
	
	// setup default level icons
	private void setupIcons() {
		// TODO
	}
	
	/**
	 * Set the text style used for the given log level.
	 * 
	 * @param level
	 * @param style
	 */
	public void setLevelStyle(Level level, AttributeSet style) {
		styleMap.put(level, style);
	}
	
	// setup default styles
	private void setupStyles() {
		// TODO 
	}
	
	public Formatter getFormatter() {
		return formatter;
	}

	public void setFormatter(Formatter formatter) {
		this.formatter = formatter;
	}

	/**
	 * Add log record to document
	 * 
	 * @param record
	 */
	private void appendLogRecord(LogRecord record) {
		final String text = formatter.format(record);
		
		// insert text into document
		final Document doc = getDocument();
		try {
			doc.insertString(doc.getLength(), " ", iconMap.get(record.getLevel()));
			doc.insertString(doc.getLength(), text, styleMap.get(record.getLevel()));
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a logger
	 * 
	 * @param logger
	 */
	public void addLogger(Logger logger) {
		logger.addHandler(handler);
	}
	
	/**
	 * Remove a logger
	 *
	 * @param logger
	 */
	public void removeLogger(Logger logger) {
		logger.removeHandler(handler);
	}
	
	/**
	 * The logging handler for the text pane
	 */
	private final Handler handler = new Handler() {
		
		@Override
		public void publish(LogRecord arg0) {
			appendLogRecord(arg0);
		}
		
		@Override
		public void flush() {
		}
		
		@Override
		public void close() throws SecurityException {
		}
	};
	
}
