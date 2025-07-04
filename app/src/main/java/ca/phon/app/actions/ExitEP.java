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
package ca.phon.app.actions;


import ca.phon.app.actions.SaveOnExitDialog.QuitOption;
import ca.phon.app.hooks.PhonShutdownHook;
import ca.phon.app.log.LogUtil;
import ca.phon.plugin.*;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.*;

import java.io.IOException;
import java.util.*;

@PhonPlugin(name="default")
public class ExitEP implements IPluginEntryPoint
{
	private final static String EP_NAME = "Exit";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
    public ExitEP() {
    }

	/* (non-Javadoc)
	 * @see modules.IModuleController#begin()
	 */
	private void begin() {
		performQuit();
	}
	
	private class SaveListener implements NativeDialogListener {

		public volatile boolean hasReturned = false;
		public volatile int result = NativeDialogEvent.UNKNOWN;
		
		public SaveListener() {
			super();
			this.hasReturned = false;
		}
		
		@Override
		public void nativeDialogEvent(NativeDialogEvent event) {
			result = event.getDialogResult();
			
			this.hasReturned = true;
		}
	}
	
	private void performQuit() {
		final SaveOnExitDialog dlg = new SaveOnExitDialog(new CommonModuleFrame());
		if(dlg.getSelectedEditors().size() > 0 || dlg.getCheckForChangesOnExit().size() > 0) {
			// display dialog
			QuitOption r = dlg.showDialog();
			
			// wait for close ...
			
			if(r == QuitOption.Cancel) {
//				exitStatus = ExitStatus.ERR;
				throw new RuntimeException("Exit canceled by user");
//				return;
			} else if(r == QuitOption.SaveSelected) {
				// save each selected session
				List<CommonModuleFrame> editors = 
					dlg.getSelectedEditors();
				for(CommonModuleFrame editor:editors) { 
					try {
						if(!editor.saveData()) {
							throw new IOException("Could not save data for window " + editor.getTitle());
						}
					} catch (IOException e) {
						LogUtil.severe(e);
						// critical - don't exit without saving!!
						throw new RuntimeException("Exit canceled, save data failed.");
					}
				}

				// other changes
				List<CheckForChangesOnExit> checkForChangesOnExit = dlg.getCheckForChangesOnExit();
				for(CheckForChangesOnExit checkForChanges:checkForChangesOnExit) {
					try {
						checkForChanges.save();
					} catch (IOException e) {
						LogUtil.severe(e);
						throw new RuntimeException("Exit canceled, save data failed for " + checkForChanges.getName(), e);
					}
				}
			}
		}
	    
		// if we get here, we are shutting down
		// call plug-in shutdown methods
		final List<IPluginExtensionPoint<PhonShutdownHook>> shutdownHookPts = 
				PluginManager.getInstance().getExtensionPoints(PhonShutdownHook.class);
		for(IPluginExtensionPoint<PhonShutdownHook> shutdownHookPt:shutdownHookPts) {
			final PhonShutdownHook hook = shutdownHookPt.getFactory().createObject();
			try {
				hook.shutdown();
			} catch (PluginException pe) {
				LogUtil.severe(pe);
			}
		}
		
	    // exit program
	    System.exit( 0 );
	}

	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		begin();
	}

}
