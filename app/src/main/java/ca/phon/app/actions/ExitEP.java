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
package ca.phon.app.actions;



import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.actions.SaveOnExitDialog.QuitOption;
import ca.phon.app.hooks.PhonShutdownHook;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.plugin.PluginException;
import ca.phon.plugin.PluginManager;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogListener;

@PhonPlugin(name="default")
public class ExitEP implements IPluginEntryPoint
{
	
	private final static Logger LOGGER = Logger.getLogger(ExitEP.class.getName());
	
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
		// check for editors with changes
		ArrayList<CommonModuleFrame> editorsWithChanges
			= new ArrayList<CommonModuleFrame>();
		Set<Project> projectSet = new HashSet<Project>();
		for(CommonModuleFrame f:CommonModuleFrame.getOpenWindows()) {
			final Project project = f.getExtension(Project.class);
			if(project != null) {
				projectSet.add(project);
			}
			if(f.hasUnsavedChanges())
				editorsWithChanges.add(f);
		}
		
		if(editorsWithChanges.size() > 0) {
			// display dialog
			SaveOnExitDialog dlg = new SaveOnExitDialog(new CommonModuleFrame());
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
						LOGGER.log(Level.SEVERE, e.getMessage(), e);
						// critical - don't exit without saving!!
						throw new RuntimeException("Exit canceled, save data failed.");
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
				LOGGER.log(Level.SEVERE, pe.getMessage(), pe);
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
