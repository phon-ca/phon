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
package ca.phon.app.session.editor;

import javax.swing.*;
import javax.swing.text.*;

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
