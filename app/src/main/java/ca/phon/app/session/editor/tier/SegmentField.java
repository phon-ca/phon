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
package ca.phon.app.session.editor.tier;

import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;

public class SegmentField extends JFormattedTextField {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5405525676580970609L;

	private static final Logger LOGGER = Logger
			.getLogger(SegmentField.class.getName());

	public SegmentField() {
		super();
		
		this.setFormatterFactory(new SegmentFormatterFactory());
	}

	/** Formatter factory */
	private class SegmentFormatterFactory extends AbstractFormatterFactory {

		@Override
		public AbstractFormatter getFormatter(JFormattedTextField arg0) {
			AbstractFormatter retVal = null;
			try {
				retVal = new MaskFormatter("###:##.###-###:##.###");
				((MaskFormatter)retVal).setPlaceholderCharacter('0');
			} catch (ParseException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
			return retVal;
		}

	}
	
}
