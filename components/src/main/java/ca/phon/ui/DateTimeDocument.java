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
package ca.phon.ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class DateTimeDocument extends PlainDocument {
	
	private static final long serialVersionUID = -3209553456147504916L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(DateTimeDocument.class.getName());
	
	String testRegex = 
		"([0-9]{4})-([0-9]{2})-([0-9]{2})";
	
	final DateTimeFormatter dateFormatter = 
			DateTimeFormatter.ofPattern("yyyy-MM-dd");
					
	public DateTimeDocument(LocalDate date) {
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
				dateFormatter.parse(testString);
			} catch (Exception e) {
//				LOGGER.error( e.getLocalizedMessage(), e);
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
		super.remove(offs, len);
	}
	
	public LocalDate getDateTime() {
		LocalDate retVal = LocalDate.now();
		
		String dateText = "";
		try {
			dateText = getText(0, getLength());
		} catch (BadLocationException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
		
		try {
			retVal = LocalDate.parse(dateText, dateFormatter);
		} catch (Exception e) {
			LOGGER.info( e.getLocalizedMessage(), e);
		}
		
		return retVal;
	}
	
	public void setDateTime(LocalDate date) {
		try {
			super.remove(0, getLength());
			final String dateText = dateFormatter.format(date);
			super.insertString(0, dateText, null);
		} catch (BadLocationException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}
}
