/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

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
