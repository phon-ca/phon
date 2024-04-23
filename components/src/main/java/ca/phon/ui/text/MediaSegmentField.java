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
package ca.phon.ui.text;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Text field for editing media segment times.  Times are displayed
 * as 'HHH:MM.SSS'
 *
 */
public class MediaSegmentField extends JFormattedTextField {
	
	private static final String MASK = "###:##.###-###:##.###";
	
	public MediaSegmentField() {
		super();
		
		this.setFormatterFactory(new SegmentFormatterFactory());
	}

	/** Formatter factory */
	private class SegmentFormatterFactory extends AbstractFormatterFactory {

		@Override
		public AbstractFormatter getFormatter(JFormattedTextField arg0) {
			AbstractFormatter retVal = null;
			try {
				retVal = new MaskFormatter(MASK);
				((MaskFormatter)retVal).setPlaceholderCharacter('0');
			} catch (ParseException e) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getLocalizedMessage(), e);
			}
			return retVal;
		}

	}
	
}
