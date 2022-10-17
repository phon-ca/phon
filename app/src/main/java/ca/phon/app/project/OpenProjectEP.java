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
package ca.phon.app.project;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.session.editor.SessionEditorEP;
import ca.phon.plugin.*;
import ca.phon.project.*;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.*;
import org.apache.logging.log4j.Level;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

@PhonPlugin(name="default")
public class OpenProjectEP implements IPluginEntryPoint {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(OpenProjectEP.class.getName());
	
	public static final String EP_NAME = "OpenProject";

	/**
	 * If true, OpenSessionEP will be called after the project window is opened with
	 * the provided entry point args which should point to an existing session in the project
	 */
	public static final String OPEN_WITH_SESSION = "open_with_session";

	@Override
	public String getName() {
		return EP_NAME;
	}
	
	/** Constructor */
	public OpenProjectEP() {
		super();
	}
			
	public void loadProject(EntryPointArgs epArgs) {
		final Project project = epArgs.getProject();
		if(project != null) {
			moveOldProperitesFile(project);

			final boolean openWithSession =
					epArgs.containsKey(OPEN_WITH_SESSION) ? (Boolean)epArgs.get(OPEN_WITH_SESSION) : false;
			loadLocalProject(project, !openWithSession);

			if(openWithSession) {
				try {
					PluginEntryPointRunner.executePlugin(SessionEditorEP.EP_NAME, epArgs);
				} catch (PluginException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	public void newProject() {
		PluginEntryPointRunner.executePluginInBackground("NewProject");
	}
	
	//	 load the project
    private boolean loadLocalProject(Project project, boolean requestFocus) {
    	final MessageDialogProperties props = new MessageDialogProperties();
    	props.setTitle("Open Project");
    	props.setHeader("Could not open project");
    	props.setRunAsync(false);
    	props.setOptions(MessageDialogProperties.okOptions);
    	props.setParentWindow(CommonModuleFrame.getCurrentFrame());
    	
    	try{
			File myFile = new File(project.getLocation());
			
			// file does not exist, return
			if(!myFile.exists()) {
				props.setMessage("Could not find a project at '" + project.getLocation() + "'");
				NativeDialogs.showMessageDialog(props);
				return false;
			}
			
			// check to see if the project is already open...
			for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
				if(!(cmf instanceof ProjectWindow)) continue;
				final Project pfe = cmf.getExtension(Project.class);
				if(pfe != null && pfe.getLocation().equals(project.getLocation())) {
					if(requestFocus) {
						cmf.toFront();
						cmf.requestFocus();
					}
					return true;
				}
			}
			
			// file is read-only
			if(!myFile.canWrite()) {
				int result = NativeDialogs.showOkCancelDialogBlocking(
						null,
						"",
						"File Read-only",
						"You are about to open a project that is read-only. Changes will not be saved.");
				
				if(result == NativeDialogEvent.CANCEL_OPTION)
					return true; // file was actually found, so return true
			}
			
			// check project version and see if an update is needed
			if(project.getVersion().equals("unk")) {
				String msgTitle = "Convert Project?";
				String msg = "This project needs to be upgraded for use in this version of Phon."
					+ " Click 'Yes' to convert this project.";
				int retVal = NativeDialogs.showYesNoDialogBlocking(CommonModuleFrame.getCurrentFrame(),
						"", msgTitle, msg);
				
				if(retVal == NativeDialogEvent.YES_OPTION) {
					HashMap<String, Object> initInfo = new HashMap<String, Object>();
					initInfo.put("oldProjectPath", myFile.getAbsolutePath());
					
					PluginEntryPointRunner.executePluginOnNewThread("ConvertProject", initInfo);
				}
				return true;
			}
			
			final ProjectWindow pwindow = new ProjectWindow(project, project.getLocation());
    		pwindow.pack();
    		pwindow.setSize(800, 600);
    		pwindow.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
    		pwindow.setVisible(true);
    		
    		return true;
		} catch (Exception e) {
			// catch anything and report
			LOGGER.error(e.getMessage());
			e.printStackTrace();
			props.setMessage(e.getLocalizedMessage());
			NativeDialogs.showMessageDialog(props);
		}
		
		return false;
    }

    private void moveOldProperitesFile(Project project) {
		final File oldPropsFile = new File(project.getLocation(), LocalProject.PREV_PROJECT_PROPERTIES_FILE);
		final File newPropsFile = new File(project.getLocation(), LocalProject.PROJECT_PROPERTIES_FILE);
		if(oldPropsFile.exists() && !newPropsFile.exists()) {
			LOGGER.log(Level.INFO, "Moving old .properties file to new project.properties");
			try {
				Files.move(oldPropsFile.toPath(), newPropsFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException e) {
				LOGGER.log(Level.ERROR, e.getLocalizedMessage(), e);
			}
		}
    }

	@Override
	public void pluginStart(Map<String, Object> args)  {
		if(GraphicsEnvironment.isHeadless()) return;
		
		final EntryPointArgs epArgs = new EntryPointArgs(args);
		final Runnable openProject = () -> {

			loadProject(epArgs);
		};

		if(SwingUtilities.isEventDispatchThread()) {
			openProject.run();
		} else {
			SwingUtilities.invokeLater(openProject);
		}
	}

}
