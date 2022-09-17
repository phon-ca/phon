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
package ca.phon.app;

import ca.phon.app.actions.OpenFileEP;
import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.app.log.LogUtil;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.*;
import ca.phon.util.OSInfo;

import java.awt.*;
import java.io.File;

/**
 * 
 */
public class PhonDesktopStartupHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {

	@Override
	public void startup() throws PluginException {
		Desktop desktop = Desktop.getDesktop();
		
		if(Desktop.isDesktopSupported() && OSInfo.isMacOs()) {
			desktop.setQuitHandler( 			
				(e, r) -> {
					try {
						PluginEntryPointRunner.executePlugin("Exit");
						r.performQuit();
					} catch (PluginException e1) {
						LogUtil.severe(e1);
						r.cancelQuit();
					}
				}
			);
			
			desktop.setAboutHandler( (e) -> {
				try {
					PluginEntryPointRunner.executePlugin("Help");
				} catch (PluginException ex) {
					Toolkit.getDefaultToolkit().beep();
					LogUtil.severe(ex);
				}
			});
			
			desktop.setPreferencesHandler( (e) -> {
				try {
					PluginEntryPointRunner.executePlugin("Preferences");
				} catch (PluginException ex) {
					Toolkit.getDefaultToolkit().beep();
					LogUtil.severe(ex);
				}
			});
			
			desktop.setOpenFileHandler( (e) -> {
				for(File file:e.getFiles()) {
					EntryPointArgs args = new EntryPointArgs();
					args.put(OpenFileEP.INPUT_FILE, file);
					try {
						PluginEntryPointRunner.executePlugin(OpenFileEP.EP_NAME, args);
					} catch (PluginException ex) {
						Toolkit.getDefaultToolkit().beep();
						LogUtil.severe(ex);
					}
				}
			});
			
		}
	}

	@Override
	public Class<?> getExtensionType() {
		return PhonStartupHook.class;
	}

	@Override
	public IPluginExtensionFactory<PhonStartupHook> getFactory() {
		return (args) -> this;
	} 

}
