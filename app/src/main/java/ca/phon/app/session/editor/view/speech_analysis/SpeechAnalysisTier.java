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
package ca.phon.app.session.editor.view.speech_analysis;

import javax.swing.*;

/**
 * Extension point for waveform view tiers.
 *
 */
public interface SpeechAnalysisTier {
	
	/**
	 * Get the tier component
	 * 
	 * @return component
	 */
	public JComponent getTierComponent();
	
	/**
	 * Add custom commands to the editor view menu.
	 * 
	 * @param menu
	 * @param includeAccelerators
	 */
	public void addMenuItems(JMenu menuEle, boolean includeAccelerators);
	
	/**
	 * Called on the Refresh action for the tier.
	 * 
	 */
	public void onRefresh();
	
}
