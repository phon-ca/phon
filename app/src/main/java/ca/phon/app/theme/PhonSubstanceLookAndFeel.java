/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.app.theme;

import javax.swing.UIDefaults;

import org.pushingpixels.lafwidget.LafWidget;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.fonts.FontPolicy;
import org.pushingpixels.substance.api.fonts.FontSet;

public class PhonSubstanceLookAndFeel extends SubstanceLookAndFeel {

	private static final long serialVersionUID = 6899234733111691465L;

	public PhonSubstanceLookAndFeel() {
		super(new PhonSubstanceSkin());
	}

	@Override
	public UIDefaults getDefaults() {
		final UIDefaults retVal = super.getDefaults();
		
		SubstanceLookAndFeel.setFontPolicy(null);
        
        // Create the wrapper font set
        FontPolicy newFontPolicy = new FontPolicy() {
          public FontSet getFontSet(String lafName,
              UIDefaults table) {
            return new PhonUIFontSet();
          }
        };

		SubstanceLookAndFeel.setFontPolicy(newFontPolicy);
		
		retVal.put(
                SubstanceLookAndFeel.COLORIZATION_FACTOR,
                1.0);
		retVal.put(
                SubstanceLookAndFeel.SHOW_EXTRA_WIDGETS,
                Boolean.TRUE);
		retVal.put(LafWidget.TEXT_EDIT_CONTEXT_MENU,
                Boolean.TRUE);

		
		return retVal;
	}
	

}
