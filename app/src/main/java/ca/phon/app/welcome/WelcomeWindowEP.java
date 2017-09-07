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
package ca.phon.app.welcome;

import java.util.Map;

import javax.swing.SwingUtilities;

import ca.phon.plugin.*;

@PhonPlugin(author="Greg Hedlund", minPhonVersion="Phon 2.2", version="1")
public class WelcomeWindowEP implements IPluginEntryPoint {

	@Override
	public String getName() {
		return "WelcomeWindow";
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		final Runnable onEDT =  () -> {
			final WelcomeWindow window = new WelcomeWindow();
			window.pack();
			window.setSize(900, 710);
			window.centerWindow();
			window.setVisible(true);
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			SwingUtilities.invokeLater(onEDT);
	}

}
