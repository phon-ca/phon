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
package ca.phon.app.help;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import javax.swing.WindowConstants;

import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;

/**
 * Displays a table (with filter) for looking up ISO language codes.
 *
 */
@PhonPlugin(name="default")
public class LanguageCodeEP implements IPluginEntryPoint{
	
	/** 
	 * Table
	 */
	private static LanguageCodePanel languagePanel;
	
	/**
	 * Window
	 */
	private static CommonModuleFrame _window;
	
	public final static String EP_NAME = "LanguageCode";
	
	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		CommonModuleFrame window = getWindow();
		window.setWindowName("Language Codes");
		window.setSize(600, 500);
//		window.centerWindow();
		window.setLocationByPlatform(true);
		window.setVisible(true);
	}

	private static CommonModuleFrame getWindow() {
		CommonModuleFrame retVal = _window;
		if(_window == null) {
			languagePanel = new LanguageCodePanel();
			CommonModuleFrame window = new CommonModuleFrame("ISO-639-3 Language Codes");
			window.addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosed(WindowEvent e) {
					_window = null;
				}
				
			});
			window.setLayout(new BorderLayout());
			DialogHeader header = new DialogHeader("ISO-639-3 Language Codes", "For more information see http://en.wikipedia.org/wiki/ISO_639-3");
			window.add(header, BorderLayout.NORTH);
			window.add(languagePanel, BorderLayout.CENTER);
			
			window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			window.setResizable(false);
			
			_window = window;
		}
		return _window;
	}

}
