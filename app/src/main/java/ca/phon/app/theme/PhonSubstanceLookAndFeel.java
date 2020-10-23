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
package ca.phon.app.theme;

import javax.swing.*;

import org.pushingpixels.lafwidget.*;
import org.pushingpixels.substance.api.*;
import org.pushingpixels.substance.api.fonts.*;

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
