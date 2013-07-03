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
package ca.phon.app.menu;

import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import ca.phon.app.workspace.WorkspaceDialog;
import ca.phon.plugin.IPluginMenuFilter;
import ca.phon.plugin.PluginAction;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;

/**
 * Create the default menu for all Phon windows.
 * 
 */
public class DefaultMenuFilter implements IPluginMenuFilter {

	@Override
	public void filterWindowMenu(Window owner, JMenuBar menu) {
		addFileMenu(owner, menu);
		addEditMenu(owner, menu);
		addQueryMenu(owner, menu);
		addToolsMenu(owner, menu);
		addPluginMenu(owner, menu);
		addWindowMenu(owner, menu);
		addHelpMenu(owner, menu);
	}
	
	/**
	 * Add 'File' menu
	 */
	protected void addFileMenu(Window owner, JMenuBar menu) {
		JMenu fileMenu = new JMenu("File");
		
		// start dialog item
		PluginAction sdAct = new PluginAction("StartWindow");
		sdAct.putValue(Action.NAME, "Workspace...");
		sdAct.putValue(Action.SHORT_DESCRIPTION, "Open the workspace dialog.");
		sdAct.putValue(Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		JMenuItem workspaceItem = new JMenuItem(sdAct);
		fileMenu.add(workspaceItem);
		
		fileMenu.addSeparator();
		
		// recent menu
		JMenu recentsMenu = new JMenu("Recent projects");
		recentsMenu.addMenuListener(recentsMenuListener);
		fileMenu.add(recentsMenu);
		
		// exit item
		fileMenu.addSeparator();
		
		PluginAction exitAct = new PluginAction("Exit");
		exitAct.putValue(Action.NAME, "Exit");
		exitAct.putValue(Action.SHORT_DESCRIPTION, "Exit the application.");
		exitAct.putValue(Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		JMenuItem exitItem = new JMenuItem(exitAct);
		fileMenu.add(exitItem);
		
		menu.add(fileMenu);
	}

	/**
	 * Add 'Edit' menu
	 * 
	 */
	protected void addEditMenu(Window owner, JMenuBar menu) {
		JMenu editMenu = new JMenu("Edit");
		
		// cut 
		PluginAction cutAct = new PluginAction("Cut");
		cutAct.putValue(Action.NAME, "Cut");
		cutAct.putValue(Action.SHORT_DESCRIPTION, "Edit: cut");
		cutAct.putValue(Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		JMenuItem cutItem = new JMenuItem(cutAct);
		editMenu.add(cutItem);
		
		// copy 
		PluginAction copyAct = new PluginAction("Copy");
		copyAct.putValue(Action.NAME, "Copy");
		copyAct.putValue(Action.SHORT_DESCRIPTION, "Edit: copy");
		copyAct.putValue(Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		JMenuItem copyItem = new JMenuItem(copyAct);
		editMenu.add(copyItem);
		
		// paste
		PluginAction pasteAct = new PluginAction("Paste");
		pasteAct.putValue(Action.NAME, "Paste");
		pasteAct.putValue(Action.SHORT_DESCRIPTION, "Edit: paste");
		pasteAct.putValue(Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		JMenuItem pasteItem = new JMenuItem(pasteAct);
		editMenu.add(pasteItem);
		
		editMenu.addSeparator();
		
		// prefs
		PluginAction prefsAct = new PluginAction("Properties");
		prefsAct.putValue(Action.NAME, "Preferences...");
		prefsAct.putValue(Action.SHORT_DESCRIPTION, "Edit application preferences");
//		prefsAct.putValue(PluginAction.ACCELERATOR_KEY, 
//				KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		JMenuItem prefsItem = new JMenuItem(prefsAct);
		editMenu.add(prefsItem);
		
		menu.add(editMenu);
	}
	
	/**
	 * Add 'Query' menu
	 */
	protected void addQueryMenu(Window owner, JMenuBar menu) {
		if(!(owner instanceof CommonModuleFrame)) return;
		final CommonModuleFrame frame = (CommonModuleFrame)owner;
		if(!(frame instanceof WorkspaceDialog)) {
			final JMenu queryMenu  = new JMenu("Query");
			queryMenu.addMenuListener(queryMenuListener);
			menu.add(queryMenu);
		}
	}
	
	private class SearchAction extends AbstractAction {
		
		private Project project;
		private File script;
		
		public SearchAction(Project project, File script) {
			super();
			this.project = project;
			this.script = script;
			
			String scriptName = script.getName();
			if(scriptName.indexOf('.') > 0) 
				scriptName = scriptName.substring(0, scriptName.lastIndexOf('.'));
			
			super.putValue(NAME, scriptName + "...");
			super.putValue(SHORT_DESCRIPTION, script.getAbsolutePath());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
//			final QueryEditorWindow scriptFrame = new QueryEditorWindow(script.getName(), project);
//			scriptFrame.setWindowName(script.getName());
//			scriptFrame.openFromFile(script.getAbsolutePath(), false);
//			scriptFrame.pack();
//			scriptFrame.setLocationByPlatform(true);
//			scriptFrame.setVisible(true);
		}
		
	}
	
	/**
	 * Add 'Tools' menu
	 */
	protected void addToolsMenu(Window owner, JMenuBar menu) {
		JMenu toolsMenu = new JMenu("Tools");
		
		// ipa chart
		PluginAction ipaAct = new PluginAction("CharmapView");
		ipaAct.putValue(Action.NAME, "IPA Map");
		ipaAct.putValue(Action.SHORT_DESCRIPTION, "IPA Map");
		ipaAct.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		JMenuItem ipaItem = new JMenuItem(ipaAct);
		toolsMenu.add(ipaItem);
		
		toolsMenu.addSeparator();
		
		// language codes
		PluginAction langAct = new PluginAction("LanguageCode");
		langAct.putValue(Action.NAME, "ISO-639-3 Language Codes");
		langAct.putValue(Action.SHORT_DESCRIPTION, "Standard 3 letter language codes");
		JMenuItem langItem = new JMenuItem(langAct);
		toolsMenu.add(langItem);
		
		menu.add(toolsMenu);
	}
	
	/**
	 * Add 'Window' menu
	 */
	protected void addWindowMenu(Window owner, JMenuBar menu) {
		JMenu windowMenu = new JMenu("Window");
		
		windowMenu.addMenuListener(openWindowMenuListener);
		
		// generic close item
		PluginAction closeAct = new PluginAction("CloseWindow");
		closeAct.putArg("window", owner);
		closeAct.putValue(Action.NAME, "Close");
		closeAct.putValue(Action.SHORT_DESCRIPTION, "Close window");
		closeAct.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		JMenuItem closeItem = new JMenuItem(closeAct);
		windowMenu.add(closeItem);
		
		// keep on tops
		menu.add(windowMenu);
	}
	
	/**
	 * Add 'Plugin' menu
	 */
	protected void addPluginMenu(Window owner, JMenuBar menu) {
		JMenu pluginMenu = new JMenu("Plugins");
		
		menu.add(pluginMenu);
	}
	
	/**
	 * Add 'Help' menu
	 */
	protected void addHelpMenu(Window owner, JMenuBar menu) {
		JMenu helpMenu = new JMenu("Help");
		
		// log
		PluginAction logAct = new PluginAction("Log");
		logAct.putValue(Action.NAME, "Application log...");
		logAct.putValue(Action.SHORT_DESCRIPTION, "View application error log");
		JMenuItem logItem = new JMenuItem(logAct);
		helpMenu.add(logItem);
		
		helpMenu.addSeparator();
		
		// update
		PluginAction updateAct = new PluginAction("Update");
		updateAct.putValue(Action.NAME, "Check for updates...");
		updateAct.putValue(Action.SHORT_DESCRIPTION, "Check for updates to the application");
		JMenuItem updateItem = new JMenuItem(updateAct);
		helpMenu.add(updateItem);
		
		helpMenu.addSeparator();
		
		// user manual
		PluginAction manAct = new PluginAction("UserManual");
		manAct.putValue(Action.NAME, "User manual");
		manAct.putValue(Action.SHORT_DESCRIPTION, "View user manual in your default pdf viewer");
		JMenuItem manItem = new JMenuItem(manAct);
		helpMenu.add(manItem);
		
		// about
		PluginAction aboutAct = new PluginAction("HelpAbout");
		aboutAct.putValue(Action.NAME, "About Phon");
		aboutAct.putValue(Action.SHORT_DESCRIPTION, "View about dialog and licence agreement");
		JMenuItem aboutItem = new JMenuItem(aboutAct);
		helpMenu.add(aboutItem);
		
		menu.add(helpMenu);
	}
	
	/**
	 * Recents menu generator
	 */
	private final MenuListener recentsMenuListener = new  MenuListener() {

		@Override
		public void menuCanceled(MenuEvent arg0) {
			
		}

		@Override
		public void menuDeselected(MenuEvent arg0) {
			
		}

		@Override
		public void menuSelected(MenuEvent arg0) {
			JMenu menu = (JMenu)arg0.getSource();
			menu.removeAll();
			
//			// add recent projects to menu
//			SystemProperties recentProjects = 
//				   UserPrefManager.getUserRecentProjects();
//			   
//			for(int i = 1; i <= 5; i++) {
//				final String projectString =
//					recentProjects.getProperty("ca.phon.project.recent."+i).toString();
//				   
//				if(projectString.length() > 0) {
//					PluginAction projectAct = new PluginAction("OpenProject");
//					projectAct.putArg("ca.phon.modules.core.OpenProjectController.projectpath", projectString);
//					projectAct.putValue(Action.NAME, projectString);
//					projectAct.putValue(Action.SHORT_DESCRIPTION, "Open project");
//					
//					JMenuItem projectItem = new JMenuItem(projectAct);
//					menu.add(projectItem);
//				}
//				
//			}
		}
		
	};
	
	/**
	 * Open window menu generator
	 */
	private final MenuListener openWindowMenuListener = new MenuListener() {

		@Override
		public void menuCanceled(MenuEvent arg0) {
		}

		@Override
		public void menuDeselected(MenuEvent arg0) {
		}

		@Override
		public void menuSelected(MenuEvent arg0) {
			JMenu menu = (JMenu)arg0.getSource();
			menu.removeAll();
			
//			// add windows sorted by open project
//			// get open projects
//			IPhonProject openProjects[] = 
//				PhonEnvironment.getInstance().getOpenProjects().toArray(new IPhonProject[0]);
//			
//			CommonModuleFrame openWindows[] = 
//				CommonModuleFrame.getOpenWindows().toArray(new CommonModuleFrame[0]);
//			for(IPhonProject proj:openProjects) {
//				String projName = new String();
//					projName = 
//						proj.getProjectName();
//				if(menu.getItemCount() > 0) 
//					menu.addSeparator();
////				JMenu projMenu = new JMenu(projName);
////				JMenuItem projItem = new JMenuItem(projName);
////				projItem.setEnabled(false);
////				menu.add(projItem);
//					
//				for(CommonModuleFrame f:openWindows) {
//					if(!f.isShowInWindowMenu() || !f.isShowing())
//						continue;
//					
//					if(f.getProject() != null
//							&& f.getProject() == proj) {
//						JMenuItem windowItem = new JMenuItem(f.getTitle());
//						
//						windowItem.addActionListener(new ActionListener() {
//
//							@Override
//							public void actionPerformed(ActionEvent arg0) {
//								for(CommonModuleFrame f:CommonModuleFrame.getOpenWindows()) {
//									if(f.getTitle().equals( ((JMenuItem)arg0.getSource()).getText())) {
//										
//										f.toFront();
////										CommonModuleFrame.currentFrame = f;
//									}
//								}
//							}
//							
//						});
//						menu.add(windowItem);
//					}
//				}
////				menu.add(projMenu);
//			}
//			
//			menu.addSeparator();
//			
//			for(CommonModuleFrame f:openWindows) {
//				
//				if(f.getProject() != null || f.getParentFrame() != null)
//					continue;
//				
//				if(!f.isShowInWindowMenu() || !f.isShowing())
//					continue;
//				
//				JMenuItem windowItem = new JMenuItem(f.getTitle());
//				windowItem.addActionListener(new ActionListener() {
//
//					@Override
//					public void actionPerformed(ActionEvent arg0) {
//						for(CommonModuleFrame f:CommonModuleFrame.getOpenWindows()) {
//							if(f.getTitle().equals( ((JMenuItem)arg0.getSource()).getText())) {
//								f.toFront();
////								CommonModuleFrame.currentFrame = f;
//							}
//						}
//					}
//					
//				});
//				
//				menu.add(windowItem);
//			}
//			
//			menu.addSeparator();
			
			// generic close item
			PluginAction closeAct = new PluginAction("CloseWindow");
			closeAct.putArg("window", CommonModuleFrame.getCurrentFrame());
			closeAct.putValue(Action.NAME, "Close");
			closeAct.putValue(Action.SHORT_DESCRIPTION, "Close window");
			closeAct.putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			JMenuItem closeItem = new JMenuItem(closeAct);
			menu.add(closeItem);
		}
		
	};
	
	private final MenuListener queryMenuListener = new MenuListener() {

			public void menuCanceled(MenuEvent e) {
			
			}

			public void menuDeselected(MenuEvent e) {
			
			}

			public void menuSelected(MenuEvent e) {
				final JMenu queryMenu = (JMenu)e.getSource();
				queryMenu.removeAll();
				
//				final IPhonProject project = CommonModuleFrame.getCurrentFrame().getProject();
//				if(project == null) return;
//				
//				final QueryScriptLibrary queryScriptLibrary = new QueryScriptLibrary();
//				
//				// add stock scripts
//				for(File stockScriptFile:queryScriptLibrary.stockScriptFiles()) {
//					final SearchAction act = new SearchAction(project, stockScriptFile);
//					JMenuItem sItem = new JMenuItem(act);
//					
//					queryMenu.add(sItem);
//				}
//
//				// add user library scripts
//				List<JMenuItem> libItems = new ArrayList<JMenuItem>();
//				for(File libScriptFile:queryScriptLibrary.userScriptFiles()) {
//					final SearchAction act = new  SearchAction(project, libScriptFile);
//					JMenuItem sItem = new JMenuItem(act);
//					
//					libItems.add(sItem);
//				}
//				if(libItems.size() > 0) {
//					JMenuItem libSepItem = new JMenuItem("User Library");
//					libSepItem.setEnabled(false);
//					
//					queryMenu.add(libSepItem);
//					for(JMenuItem itm:libItems)
//						queryMenu.add(itm);
//				}
//
//				if(project != null) {
//					// add project scripts
//					List<JMenuItem> projItems = new ArrayList<JMenuItem>();
//					
//					for(File projScriptFile:queryScriptLibrary.projectScriptFiles(project)) {
//						final SearchAction act = new SearchAction(project, projScriptFile);
//						JMenuItem sItem = new JMenuItem(act);
//
//						projItems.add(sItem);
//					}
//					if(projItems.size() > 0) {
//						JMenuItem projSepItem = new JMenuItem("Project Scripts");
//						projSepItem.setEnabled(false);
//
//						queryMenu.add(projSepItem);
//						for(JMenuItem itm:projItems)
//							queryMenu.add(itm);
//					}
//				}
//				
//				JMenuItem scriptItem = new JMenuItem("Script Editor...");
//				scriptItem.addActionListener(new ActionListener() {
//
//					public void actionPerformed(ActionEvent e) {
//						String projectName = "";
//						projectName = project.getProjectName();
//						
//						QueryScript scriptTemplate = new QueryScript(new File("data/script/script.template"));
//						scriptTemplate.setLocation(null);
//						QueryEditorWindow sd = new QueryEditorWindow("Script Editor : " + projectName, project,
//								scriptTemplate);
////							sd.setParentFrame(CommonModuleFrame.getCurrentFrame());
//						sd.pack();
//						sd.setLocationByPlatform(true);
//						sd.setVisible(true);
//					}
//					
//				});
//				
//				JMenuItem historyItem = new JMenuItem("Query History...");
//				historyItem.addActionListener(new ActionListener() {
//
//					public void actionPerformed(ActionEvent e) {
//						final Map<String, Object> initInfo = 
//								new HashMap<String, Object>();
//						initInfo.put("project", project);
//						
//						try {
//							PluginEntryPointRunner.executePlugin("QueryHistory", initInfo);
//						} catch (PluginException e1) {
//							e1.printStackTrace();
//							PhonLogger.severe(e1.toString());
//						}
//					}
//					
//				});
//				
//				queryMenu.addSeparator();
//				queryMenu.add(scriptItem);
//				queryMenu.add(historyItem);
			}
			
		};
}
