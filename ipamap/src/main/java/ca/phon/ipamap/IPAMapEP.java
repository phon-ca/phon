/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.ipamap;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import ca.phon.plugin.*;

@PhonPlugin(name="default")
public class IPAMapEP implements IPluginEntryPoint {

	/** The static window instance */
	private static IpaMapFrame _window;
//	private static int numCalls = 0;
	
	public  final static String EP_NAME = "IPAMap";
	
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	private void begin() {
		IpaMapFrame window = _window;
		if(window == null) {
			window = new IpaMapFrame();
			
			window.addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosed(WindowEvent e) {
					_window = null;
				}
				
			});
			_window = window;
		}
		
		if(_window.isVisible()) {
			if(_window.getExtendedState() == JFrame.ICONIFIED) {
				_window.setExtendedState(JFrame.NORMAL);
			} else {
				_window.setVisible(false);
			}
		} else {
			_window.showWindow();
			_window.toFront();
		}
	}

	@Override
	public void pluginStart(Map<String, Object> initInfo) {	
		begin();
	}

}
