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
package ca.phon.app.opgraph.macro;

import ca.phon.app.log.LogUtil;
import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.resources.ResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.*;

public class MacroLibrary {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(MacroLibrary.class.getName());
	
	private final static String MACRO_FOLDER = "macro";
	
	/**
	 * Report loader
	 */
	private ResourceLoader<URL> loader = new ResourceLoader<>();
	
	public MacroLibrary() {
		super();
		loader.addHandler(new StockMacroHandler());
		loader.addHandler(new UserMacroHandler());
	}
		
	public List<URL> getAvailableMacros() {
		List<URL> retVal = new ArrayList<URL>();
		
		final Iterator<URL> macroIterator = loader.iterator();
		while(macroIterator.hasNext()) {
			final URL url = macroIterator.next();
			if(url == null) continue;
			retVal.add(url);
		}
		
		return retVal;
	}
	
	public ResourceLoader<URL> getStockGraphs() {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();
		retVal.addHandler(new StockMacroHandler());
		return retVal;
	}
	
	public ResourceLoader<URL> getUserGraphs() {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();
		retVal.addHandler(new UserMacroHandler());
		return retVal;
	}
	
	public ResourceLoader<URL> getProjectGraphs(Project project) {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();
		retVal.addHandler(new UserMacroHandler(getProjectAnalysisFolder(project)));
		return retVal;
	}
	
	public File getProjectAnalysisFolder(Project project) {
		return new File(project.getResourceLocation(), MACRO_FOLDER);
	}
	
	public void setupMenu(Project project, MenuElement menu) {
		final MenuBuilder builder = new MenuBuilder(menu);
		
		for(URL macroURL:getStockGraphs()) {
			final MacroAction act = new MacroAction(project, macroURL);
			
			try {
				final String fullPath = URLDecoder.decode(macroURL.getPath(), "UTF-8");
				final String relativePath = 
						fullPath.substring(fullPath.lastIndexOf(MACRO_FOLDER + "/")+MACRO_FOLDER.length()+1);
				
				String menuPath = ".";
				int lastFolderIndex = relativePath.lastIndexOf('/');
				if(lastFolderIndex >= 0) {
					menuPath = relativePath.substring(0, lastFolderIndex);
				}
				
				builder.addItem(menuPath, act);
			} catch (UnsupportedEncodingException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		
		final JMenu userMenu = new JMenu("User Library");
		final MenuBuilder userMenuBuilder = new MenuBuilder(userMenu);
		final Iterator<URL> userGraphIterator = getUserGraphs().iterator();
		while(userGraphIterator.hasNext()) {
			try {
				final URL reportURL = userGraphIterator.next();
				final URI relativeURI = 
						(new File(UserMacroHandler.DEFAULT_USER_MACRO_FOLDER)).toURI().relativize(reportURL.toURI());
				
				final String relativePath = URLDecoder.decode(relativeURI.getPath(), "UTF-8");
				String menuPath = ".";
				int lastFolderIndex = relativePath.lastIndexOf('/');
				if(lastFolderIndex >= 0) {
					menuPath += "/" + relativePath.substring(0, lastFolderIndex);
				}
				
				final MacroAction act = new MacroAction(project, reportURL);
				userMenuBuilder.addItem(menuPath, act);
			} catch (URISyntaxException | UnsupportedEncodingException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		if(userMenu.getMenuComponentCount() > 0) {
			builder.addSeparator(".", "user_library");
			final JMenuItem userLibItem = builder.addItem(".@user_library", "-- User Library --");
			userLibItem.setFont(userLibItem.getFont().deriveFont(Font.BOLD));
			final File userLibFolder = new File(UserMacroHandler.DEFAULT_USER_MACRO_FOLDER);
			userLibItem.setToolTipText("Show folder " + userLibFolder.getAbsolutePath());
			userLibItem.addActionListener( (e) -> {
				if(Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().open(userLibFolder);
					} catch (IOException e1) {
						LogUtil.warning(e1);
						Toolkit.getDefaultToolkit().beep();
					}
				}
			});
			builder.appendSubItems(".@-- User Library --", userMenu.getPopupMenu());
		}
		
		final JMenu projectMenu = new JMenu("Project Library");
		final MenuBuilder projectMenuBuilder = new MenuBuilder(projectMenu);
		final Iterator<URL> projectGraphIterator = getProjectGraphs(project).iterator();
		while(projectGraphIterator.hasNext()) {
			try {
				final URL reportURL = projectGraphIterator.next();
				final URI relativeURI = 
						getProjectAnalysisFolder(project).toURI().relativize(reportURL.toURI());
				
				final String relativePath = URLDecoder.decode(relativeURI.getPath(), "UTF-8");
				String menuPath = ".";
				int lastFolderIndex = relativePath.lastIndexOf('/');
				if(lastFolderIndex >= 0) {
					menuPath += "/" + relativePath.substring(0, lastFolderIndex);
				}
				
				final MacroAction act = new MacroAction(project, reportURL);
				projectMenuBuilder.addItem(menuPath, act);
			} catch (URISyntaxException | UnsupportedEncodingException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		if(projectMenu.getMenuComponentCount() > 0) {
			builder.addSeparator(".", "project_library");
			final JMenuItem projectSepItem = builder.addItem(".@project_library", "-- Project Library --");
			projectSepItem.setFont(projectSepItem.getFont().deriveFont(Font.BOLD));
			final File projectFolder = getProjectAnalysisFolder(project);
			projectSepItem.addActionListener( (e) -> {
				if(Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().open(projectFolder);
					} catch (IOException e1) {
						LogUtil.warning(e1);
						Toolkit.getDefaultToolkit().beep();
					}
				}
			});
			projectSepItem.setToolTipText("Show folder " + projectFolder.getAbsolutePath());
			projectMenuBuilder.appendSubItems(".@-- Project Library --", projectMenu.getPopupMenu());
		}
		
		builder.addSeparator(".", "composer");
		final PhonUIAction<Void> showComposerAct = PhonUIAction.runnable(MacroLibrary::showComposer);
		showComposerAct.putValue(PhonUIAction.NAME, "Composer...");
		showComposerAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Create a new macro using Composer...");
		builder.addItem(".@composer", showComposerAct);
	}
	
	public static void showComposer() {
		final MacroOpgraphEditorModel editorModel = new MacroOpgraphEditorModel();
		final OpgraphEditor editor =  new OpgraphEditor(editorModel);
		
		final Project project = CommonModuleFrame.getCurrentFrame().getExtension(Project.class);
		if(project != null) {
			editor.putExtension(Project.class, project);
		}
		
		editor.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
		editor.pack();
		editor.setSize(1024, 768);
		editor.setVisible(true);
	}

}
