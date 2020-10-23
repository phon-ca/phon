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

import java.text.*;

import javax.swing.*;
import javax.swing.text.*;

import org.apache.logging.log4j.*;

/**
 * Text field for editing media segment times.  Times are displayed
 * as 'HHH:MM.SSS'
 *
 */
public class MediaSegmentField extends JFormattedTextField {
	
	private static final long serialVersionUID = 3170635774945499727L;

	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(MediaSegmentField.class.getName());

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
				LOGGER.error( e.getLocalizedMessage(), e);
			}
			return retVal;
		}

	}
	
}
