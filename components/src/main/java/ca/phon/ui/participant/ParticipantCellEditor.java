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
package ca.phon.ui.participant;

import java.awt.Component;
import java.time.LocalDate;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import ca.phon.session.Sex;
import ca.phon.ui.DateTimeDocument;

public class ParticipantCellEditor extends DefaultCellEditor {
	
	private static final long serialVersionUID = 5326108178290694072L;
	
	/** Constructor */
	public ParticipantCellEditor() {
		super(new JTextField());
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		JTextField textField =
			(JTextField)super.getTableCellEditorComponent(table, value, isSelected, row, column);
		
		if(row == ParticipantTableField.Sex.ordinal()) {
			textField.setDocument(new SexDocument((Sex)value));
		} else if(row == ParticipantTableField.Birthday.ordinal()) {
			textField.setDocument(new DateTimeDocument((LocalDate)value));
			textField.setCaretPosition(0);
		}
		else{
			textField.setDocument(new PlainDocument());
			textField.setText(value.toString());
		}
		
		textField.setBorder(null);
		return textField;
	}
	
//	private class CalendarDocument extends PlainDocument {
//
//		String testRegex = 
//			"([0-9]{4})-([0-9]{2})-([0-9]{2})";
//						
//		public CalendarDocument(Calendar cal) {
//			super();
//			
//			try {
//				insertString(getLength(),
//						dateFormat.format(cal.getTime()), null);
//			} catch (BadLocationException e) {
//			}
//		}
//		
//		public void insertString(int offs, String val, AttributeSet attr) 
//			throws BadLocationException {
//			if(val.length() == 1) {
//				
//				
//				int numChars = 1;
//				if(offs < getLength()-1) {
//					String nextChar = 
//						getText(offs+1, 1);
//					if(nextChar.equals("-")) {
//						numChars = 2;
//						val = val + "-";
//					}
//				}
//				
//				String testString = 
//					getText(0, offs) + val + getText(offs+numChars, getLength()-(offs+numChars));
//				
////				System.out.println(testString);
//				try {
//					Date testDate =
//						dateFormat.parse(testString);
//					
////					System.out.println("Parsed Date: " + dateFormat.format(testDate));
//				} catch (Exception e) {
//					return;
//				}
//				
//				super.remove(offs, numChars);
//				super.insertString(offs, val, attr);
//				
//				
//			} else if(val.matches(testRegex)) {
//				super.remove(0, getLength());
//				super.insertString(getLength(), val, attr);
//			}
//		}
//		
//		public void remove(int offs, int len) 
//			throws BadLocationException {
//			return;
//		}
//	}
	
	private class SexDocument extends PlainDocument {
		
		private Sex sex;
		
		public SexDocument(Sex sex) {
			super();
			
			this.sex = sex;
			
		}
		
		@Override
		public String getText(int offs, int len) 
			throws BadLocationException {
			if(sex == Sex.MALE)
				return "Male";
			else
				return "Female";
		}
		
		@Override
		public void insertString(int offs, String val, AttributeSet attr) 
			throws BadLocationException {
			
			if(val.toLowerCase().equals("m"))
				sex = Sex.MALE;
			else if(val.toLowerCase().equals("f"))
				sex = Sex.FEMALE;
			else
				return;
			
			super.remove(0, getLength());
			super.insertString(getLength(), getText(0, getLength()), attr);
		}
		
		@Override
		public void remove(int offs, int len) 
			throws BadLocationException {
			return;
		}
	}
}
