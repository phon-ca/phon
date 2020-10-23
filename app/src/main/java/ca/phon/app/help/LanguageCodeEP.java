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
package ca.phon.app.help;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import ca.phon.plugin.*;
import ca.phon.ui.*;
import ca.phon.ui.decorations.*;

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
