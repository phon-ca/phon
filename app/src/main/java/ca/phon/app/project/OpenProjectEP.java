/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.app.project;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.WindowConstants;

import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.project.Project;
import ca.phon.project.ProjectFactory;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.OSInfo;
import ca.phon.util.PrefHelper;

@PhonPlugin(name="default")
public class OpenProjectEP implements IPluginEntryPoint {
	
	private final static Logger LOGGER = Logger.getLogger(OpenProjectEP.class.getName());
	
	/** The controller's window */
	private static OpenProjectWindow _window;
	
	/** The project path property */
	public final static String PROJECTPATH_PROPERTY = 
		"ca.phon.modules.core.OpenProjectController.projectpath";
	
	public final static String PROJECT_UPGRADE_WARNING_ISSUED = 
		"ca.phon.modules.core.OpenProjectController.upgradewarningissued";

	private String projectpath;
	
	private final static String EP_NAME = "OpenProject";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	/** Constructor */
	public OpenProjectEP() {
		super();
	}
	
	private void begin() {
		if(projectpath == null 
				|| projectpath.length() == 0) {
			if(OpenProjectEP._window.isVisible())
				return; // only one copy at a time
			
			_window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			_window.pack();
			_window.setLocationByPlatform(true);
			
			_window.setResizable(false);
			_window.setVisible(true);
		
			final boolean warningIssued = PrefHelper.getBoolean(PROJECT_UPGRADE_WARNING_ISSUED, Boolean.FALSE);
			if(!warningIssued) {
				NativeDialogs.showMessageDialogBlocking(_window, null, "Projects will be upgraded", 
						"Project changes made using this version of Phon may not be compatible with previous versions of Phon.");
				PrefHelper.getUserPreferences().putBoolean(PROJECT_UPGRADE_WARNING_ISSUED, Boolean.TRUE);
			}
		} else {
			loadProject();
		}
	}
	
	public void setProjectPath(String projectPath) {
		this.projectpath = projectPath;
	}
	
	public String getProjectPath() {
		return projectpath;
	}
	
	public void loadProject() {
		if(projectpath == null || projectpath.length() == 0)
			return;
		
		boolean projectLoaded = false;
		// is the project remote or local?
//		if(projectpath.startsWith("rmi://"))
//			projectLoaded = loadRemoteProject();
//		else
			projectLoaded = loadLocalProject();
		
//		if(projectLoaded) {
//			ca.phon.gui.CommonModuleFrame.updateWindowMenus();
//		}
	}
	
	public void newProject() {
		
		PluginEntryPointRunner.executePluginInBackground("NewProject");
//		ModuleInformation mi = ResourceLocator.getInstance().getModuleInformationByAction(
//				"ca.phon.modules.core.NewController");
//		LoadModule lm = new LoadModule(mi, new HashMap<String, Object>());
//		lm.start();
		
		_window.setVisible(false);
		//_window.dispose();
		//_window = null;
	}
	
	//	 load the project
    private boolean loadLocalProject() {
    	try{
			File myFile = new File(projectpath);
			
			// file does not exist, return
			if(!myFile.exists()) {
				NativeDialogs.showMessageDialogBlocking(
						CommonModuleFrame.getCurrentFrame(), "",
						"Project not found",
						"Could not find a project at '" + projectpath + "'");
				return false;
			}
			
			// check to see if the project is already open...
			for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
				if(!(cmf instanceof ProjectWindow)) continue;
				final ProjectFrameExtension pfe = cmf.getExtension(ProjectFrameExtension.class);
				if(pfe != null && pfe.getProject().getLocation().equals(projectpath)) {
					cmf.toFront();
					cmf.requestFocus();
					return true;
				}
			}

			// 2010-11-24: New project format - plain directories.  We
			//             need to force people into decompressing the project.
			// is the file a .phon (or .zip) file or a directory?
			// if it's a .phon file we need to de-compress the file
			if(myFile.isFile() && (myFile.getName().endsWith(".phon")
									|| myFile.getName().endsWith(".zip")) ) {
				int retVal =
						NativeDialogs.showYesNoDialogBlocking(
							CommonModuleFrame.getCurrentFrame(), "",
							"Expand Project",
							"Phon 1.5+ no longer uses a compressed file format for projects. "
							+ " Press yes to expand the contents of your project before opening.");
				if(retVal == NativeDialogEvent.YES_OPTION) {
					File parentDir = myFile.getParentFile();
					String newProjName = myFile.getName().substring(0, myFile.getName().length()-5);
					File extractDir = new File(parentDir, newProjName);
					extractZip(myFile, extractDir);

					myFile = extractDir;
					
				} else {
					NativeDialogs.showMessageDialogBlocking(
							CommonModuleFrame.getCurrentFrame(), "",
							"Failed to Open Project",
							"Could not open project, see log for details.");
					return false;
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
			
			final ProjectFactory factory = new ProjectFactory();
			final Project proj = factory.openProject(myFile);
			
			// check project version and see if an update is needed
			if(proj.getVersion().equals("unk")) {
				String msgTitle = "Convert Project?";
				String msg = "This project needs to be upgraded for use in this version of Phon."
					+ " Click 'Yes' to convert this project.";
				int retVal = NativeDialogs.showYesNoDialogBlocking(CommonModuleFrame.getCurrentFrame(),
						"", msgTitle, msg);
				
				if(retVal == NativeDialogEvent.YES_OPTION) {
					HashMap<String, Object> initInfo = new HashMap<String, Object>();
					initInfo.put("oldProjectPath", myFile.getAbsolutePath());
					
					PluginEntryPointRunner.executePluginOnNewThread("ConvertProject", initInfo);
					
//					ModuleInformation mi = ResourceLocator.getInstance().getModuleInformationByAction("ca.phon.modules.project.ConvertProjectController");
//					LoadModule lm = new LoadModule(mi, initInfo);
//					lm.start();
				}
				return true;
			}
			
//			PhonEnvironment.getInstance().setCurrentProject(proj);
			
//			UserPrefManager.updateRecentProjects(proj.getLocation());
//			
//			ProjectRMIServer rmiServer = ProjectRMIServer.getInstance();
//			rmiServer.startServer();
//			rmiServer.registerProject(proj);
			
			ProjectWindow pwindow = new ProjectWindow(proj, proj.getLocation());
    		pwindow.pack();
//    		pwindow.centerWindow();
    		pwindow.setLocationByPlatform(true);
    		pwindow.setVisible(true);
    		
    		_window.setVisible(false);
    		_window.dispose();
    		_window = null;
    		
    		return true;
		} catch (Exception e) {
			// catch anything and report
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
			NativeDialogs.showMessageDialogBlocking(_window, "", "Could not open project", 
					e.getMessage());
		}
		
		return false;
    }

	private final static int ZIP_BUFFER = 2048;
	/**
	 * Extract contents of a zip file to the destination directory.
	 */
	private void extractZip(File zipFile, File destDir)
		throws IOException {

		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		ZipFile zip = new ZipFile(zipFile);

		// create output directory if it does not exist
		if(destDir.exists() && !destDir.isDirectory()) {
			throw new IOException("'" + destDir.getAbsolutePath() + "' is not a directory.");
		}

		if(!destDir.exists()) {
			destDir.mkdirs();
		}

		ZipEntry entry = null;
		Enumeration<? extends ZipEntry> entries = zip.entries();
		while(entries.hasMoreElements()) {
			entry = entries.nextElement();

			if(entry.isDirectory()) {
				File outDir = new File(destDir, entry.getName());
				if(!outDir.exists())
					outDir.mkdirs();
			} else {
				LOGGER.info("Extracting: " + entry);

				in = new BufferedInputStream(zip.getInputStream(entry));
				int count = -1;
				byte data[] = new byte[ZIP_BUFFER];
				File outputFile = new File(destDir, entry.getName());

				if(outputFile.exists()) {
					LOGGER.warning("Overwriting file '" + outputFile.getAbsolutePath() + "'");
				}

				File parentFile = outputFile.getParentFile();

				if(!parentFile.exists())
					parentFile.mkdirs();

				FileOutputStream fos = new FileOutputStream(outputFile);
				out = new BufferedOutputStream(fos, ZIP_BUFFER);
				while((count = in.read(data, 0, ZIP_BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				out.flush();
				out.close();
				in.close();
			}
		}
	}

	@Override
	public void pluginStart(Map<String, Object> initInfo)  {
		if(OpenProjectEP._window == null) {
			OpenProjectEP._window = new OpenProjectWindow(this);
			OpenProjectEP._window.addWindowListener(new WindowListener() {

				@Override
				public void windowActivated(WindowEvent arg0) {
				}

				@Override
				public void windowClosed(WindowEvent arg0) {
					// are there any other project windows open?
					boolean otherProjectsOpen = false;
					
					// close all other project windows
					for(CommonModuleFrame f:CommonModuleFrame.getOpenWindows()) {
						
						if(f instanceof ProjectWindow)
							otherProjectsOpen = true;
					}
					
					if(!otherProjectsOpen && !OSInfo.isMacOs()) {
//						ModuleInformation mi = ResourceLocator.getInstance().getModuleInformationByAction(
//							"ca.phon.modules.core.OpenProjectController");
//						LoadModule lm = new LoadModule(mi, new HashMap<String, Object>());
//						lm.start();
						System.exit(0);
					}
				}

				@Override
				public void windowClosing(WindowEvent arg0) {
				}

				@Override
				public void windowDeactivated(WindowEvent arg0) {
				}

				@Override
				public void windowDeiconified(WindowEvent arg0) {
				}

				@Override
				public void windowIconified(WindowEvent arg0) {
				}

				@Override
				public void windowOpened(WindowEvent arg0) {
				}
				
			});
		}
		
//		PhonLogger.warning("Hello : " + initInfo.get(PROJECTPATH_PROPERTY));
		if(initInfo.get(PROJECTPATH_PROPERTY) != null)
			projectpath = initInfo.get(PROJECTPATH_PROPERTY).toString();
		
		begin();
	}

	public static CommonModuleFrame getFrame() {
		return _window;
	}
	
}
