/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeDocument extends PlainDocument {
	
	private static final long serialVersionUID = -3209553456147504916L;

	private final static Logger LOGGER = Logger.getLogger(DateTimeDocument.class.getName());
	
	String testRegex = 
		"([0-9]{4})-([0-9]{2})-([0-9]{2})";
	
	final DateTimeFormatter dateFormatter = 
			DateTimeFormat.forPattern("yyyy-MM-dd");
					
	public DateTimeDocument(DateTime date) {
		super();
		
		setDateTime(date);
	}
	
	@Override
	public void insertString(int offs, String val, AttributeSet attr) 
		throws BadLocationException {
		if(val.length() == 0)
			super.insertString(offs, val, attr);
				
		if(val.length() == 1) {
			
			
			int numChars = 1;
			if(offs < getLength()-1) {
				String nextChar = 
					getText(offs+1, 1);
				if(nextChar.equals("-")) {
					numChars = 2;
					val = val + "-";
				}
			}
			
			String testString = 
				getText(0, offs) + val + getText(offs+numChars, getLength()-(offs+numChars));
			
			try {
				dateFormatter.parseDateTime(testString);
			} catch (Exception e) {
//				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				return;
			}
			
			super.remove(offs, numChars);
			super.insertString(offs, val, attr);
			
			
		} else if(val.matches(testRegex)) {
			super.remove(0, getLength());
			super.insertString(getLength(), val, attr);
		}
	}
	
	@Override
	public void remove(int offs, int len) 
		throws BadLocationException {
		// do some magic to see caret
		if(offs > 0)
			super.insertString(offs-1, "", null);
			
		return;
	}
	
	public DateTime getDateTime() {
		DateTime retVal = DateTime.now();
		
		String dateText = "";
		try {
			dateText = getText(0, getLength());
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		try {
			retVal = dateFormatter.parseDateTime(dateText);
		} catch (Exception e) {
//			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		return retVal;
	}
	
	public void setDateTime(DateTime date) {
		try {
			super.remove(0, getLength());
			final String dateText = dateFormatter.print(date);
			super.insertString(0, dateText, null);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
}
