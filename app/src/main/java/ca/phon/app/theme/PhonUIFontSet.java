/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import javax.swing.plaf.FontUIResource;

import org.pushingpixels.substance.api.fonts.FontSet;

import ca.phon.ui.fonts.FontPreferences;

class PhonUIFontSet implements FontSet {

	public FontUIResource getControlFont() {
		return new FontUIResource(FontPreferences.getControlFont());
	}

	public FontUIResource getMenuFont() {
		return new FontUIResource(FontPreferences.getMenuFont());
	}

	public FontUIResource getMessageFont() {
		return new FontUIResource(FontPreferences.getMenuFont());
	}

	public FontUIResource getSmallFont() {
		return new FontUIResource(FontPreferences.getSmallFont());
	}

	public FontUIResource getTitleFont() {
		return new FontUIResource(FontPreferences.getTierFont());
	}

	public FontUIResource getWindowTitleFont() {
		return new FontUIResource(FontPreferences.getWindowTitleFont());
	}

}