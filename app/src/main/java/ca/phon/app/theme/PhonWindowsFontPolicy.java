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
package ca.phon.app.theme;

import javax.swing.UIDefaults;
import javax.swing.plaf.FontUIResource;

import com.jgoodies.looks.FontPolicy;
import com.jgoodies.looks.FontSet;

import ca.phon.ui.fonts.FontPreferences;

public class PhonWindowsFontPolicy implements FontPolicy {

	@Override
	public FontSet getFontSet(String lafName, UIDefaults table) {
		return new FontSet() {
			
			@Override
			public FontUIResource getWindowTitleFont() {
				return new FontUIResource(FontPreferences.getWindowTitleFont());
			}
			
			@Override
			public FontUIResource getTitleFont() {
				return new FontUIResource(FontPreferences.getTitleFont());
			}
			
			@Override
			public FontUIResource getSmallFont() {
				return new FontUIResource(FontPreferences.getSmallFont());
			}
			
			@Override
			public FontUIResource getMessageFont() {
				return new FontUIResource(FontPreferences.getMessageDialogFont());
			}
			
			@Override
			public FontUIResource getMenuFont() {
				return new FontUIResource(FontPreferences.getMenuFont());
			}
			
			@Override
			public FontUIResource getControlFont() {
				return new FontUIResource(FontPreferences.getControlFont());
			}
		};
	}

}
