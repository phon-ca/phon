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
package ca.phon.app.phonex;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import ca.phon.plugin.*;
import ca.phon.ui.*;
import ca.phon.ui.decorations.*;

@PhonPlugin(minPhonVersion = "3.1.1")
public class PhonexDebuggerEP implements IPluginEntryPoint {

	public static String EP_NAME = "PhonexDebugger";
	
	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		PhonexDebuggerWindow debugWindow = new PhonexDebuggerWindow();
		debugWindow.pack();
		debugWindow.setLocationByPlatform(true);
		debugWindow.setVisible(true);
	}
	
	private class PhonexDebuggerWindow extends CommonModuleFrame {
		
		PhonexDebugger debugger = new PhonexDebugger();
		
		public PhonexDebuggerWindow() {
			super();
			
			setTitle("Phonex Debugger");
			init();
		}
		
		private void init() {
			setLayout(new BorderLayout());
			
			DialogHeader header = new DialogHeader("Phonex Debugger", "Visualize and debug phonex expressions");
			add(header, BorderLayout.NORTH);
			add(debugger, BorderLayout.CENTER);
		}

		@Override
		public void setJMenuBar(JMenuBar menubar) {
			super.setJMenuBar(menubar);
		}
		
	}

}
