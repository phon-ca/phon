/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.prefs;

import javax.swing.JPanel;

/**
 * A panel for the preferences dialog.  This
 * class acts as an extension point for plugins.
 * 
 */
public class PrefsPanel extends JPanel {
	
	/**
	 * Pref section title
	 */
	private String sectionTitle = "";
	
	public PrefsPanel(String title) {
		this.sectionTitle = title;
	}
	
	public String getTitle() {
		return this.sectionTitle;
	}
	
	public void setTitle(String title) {
		this.sectionTitle = title;
	}

}
