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
package ca.phon.app.session.editor;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Displays a number between (min, max).
 * The component will issue a property change
 * event with name "ManualRecordChange" when
 * the document changes which the component is focused.
 * 
 *
 */
public class RecordNumberField extends JTextField {
	
	private static final long serialVersionUID = -6748798601181931345L;
	
	private int minNumber = 0;
	private int maxNumber = 0;
	
	public RecordNumberField() {
		this(1, 100);
	}
	
	public RecordNumberField(int min, int max) {
		super();
		
		this.minNumber = min;
		this.maxNumber = max;
		
		this.setDocument(new RecordNumberDocument());
	}
	
	
	/** The document */
	private class RecordNumberDocument extends PlainDocument {
		
		private static final long serialVersionUID = -5478726486756460148L;

		@Override
		public void insertString(int offs, String val, AttributeSet attr) 
			throws BadLocationException {
			String newString = getText(0, offs) + 
				val + getText(offs, getLength() - offs);
			
			if(checkNumber(newString)) {
				super.insertString(offs, val, attr);
				
				if(hasFocus()) {
					firePropertyChange("ManualRecordChange", true, false);
				}
			}
		}
		
		private boolean checkNumber(String val) {
			int num = 0;
			try {
				num = Integer.parseInt(val);
			} catch (Exception ex) {
				return false;
			}
			
			if(num < minNumber || num > maxNumber)
				return false;
			else
				return true;
		}
		
	}


	public int getMaxNumber() {
		return maxNumber;
	}

	public void setMaxNumber(int maxNumber) {
		this.maxNumber = maxNumber;
	}

	public int getMinNumber() {
		return minNumber;
	}

	public void setMinNumber(int minNumber) {
		this.minNumber = minNumber;
	}
}
