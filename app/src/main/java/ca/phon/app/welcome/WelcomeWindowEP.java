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
package ca.phon.app.welcome;

import java.util.*;

import javax.swing.*;

import ca.phon.plugin.*;
import ca.phon.ui.*;

@PhonPlugin(author="Greg Hedlund", minPhonVersion="Phon 2.2", version="1")
public class WelcomeWindowEP implements IPluginEntryPoint {

	public final static String EP_NAME = "WelcomeWindow";
	
	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		final Runnable onEDT =  () -> {
			WelcomeWindow window = null;
			for(var cmf:CommonModuleFrame.getOpenWindows()) {
				if(cmf instanceof WelcomeWindow)
					window = (WelcomeWindow)cmf;
			}
			
			if(window == null) {
				window = new WelcomeWindow();
				window.pack();
				window.setSize(900, 710);
				window.centerWindow();
				window.setVisible(true);
			} else {
				window.setVisible(true);
				window.toFront();
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			SwingUtilities.invokeLater(onEDT);
	}

}
