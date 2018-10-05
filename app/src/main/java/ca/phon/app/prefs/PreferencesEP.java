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
package ca.phon.app.prefs;

import java.util.Map;

import javax.swing.SwingUtilities;

import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.ui.CommonModuleFrame;

/**
 * Application properties module.
 * 
 */
@PhonPlugin(name="default")
public class PreferencesEP implements IPluginEntryPoint {
	
	private final static String EP_NAME = "Preferences";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	@Override
	public void pluginStart(final Map<String, Object> mi) {
		final Runnable onEDT = new Runnable() {
			public void run() {
				final CommonModuleFrame currentFrame = CommonModuleFrame.getCurrentFrame();
				
				PrefsDialog dlg = null;
				if(currentFrame != null)
					dlg = new PrefsDialog(currentFrame);
				else
					dlg = new PrefsDialog();
				
				dlg.pack();
				if(currentFrame != null)
					dlg.setLocationRelativeTo(currentFrame);
				
				if(mi.containsKey("prefpanel")) {
					dlg.setActiveTab(mi.get("prefpanel").toString());
				}
				
				dlg.setVisible(true);
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			SwingUtilities.invokeLater(onEDT);
	}
}