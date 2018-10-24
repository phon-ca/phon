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
package ca.phon.app.menu.query;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import ca.phon.app.log.LogUtil;
import ca.phon.project.Project;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptLibrary;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.util.resources.ResourceLoader;

/**
 * Dynamic building of query menu
 */
public class QueryMenuListener implements MenuListener {
	
	private final static String STOCK_MENU_QUERIES[] = new String[] { "Data Tiers", "Deletions", "Epenthesis", "Phones", "Segmental Relations" };
	
	public void menuCanceled(MenuEvent e) {
		
	}

	public void menuDeselected(MenuEvent e) {
	
	}

	public void menuSelected(MenuEvent e) {
		final JMenu queryMenu = (JMenu)e.getSource();
		queryMenu.removeAll();
		
		final CommonModuleFrame currentFrame = CommonModuleFrame.getCurrentFrame();
		if(currentFrame == null) return;
		final Project project = CommonModuleFrame.getCurrentFrame().getExtension(Project.class);
		if(project == null) return;

		final QueryScriptLibrary queryScriptLibrary = new QueryScriptLibrary();
		
		// add stock scripts
		final ResourceLoader<QueryScript> stockScriptLoader = queryScriptLibrary.stockScriptFiles();
		final Iterator<QueryScript> stockScriptIterator = stockScriptLoader.iterator();
		while(stockScriptIterator.hasNext()) {
			final QueryScript qs = stockScriptIterator.next();
			
			final QueryName qn = qs.getExtension(QueryName.class);
			if(qn != null && Arrays.binarySearch(STOCK_MENU_QUERIES, qn.getName()) >= 0) {
				final JMenuItem sItem = new JMenuItem(new QueryScriptCommand(project, qs));
				queryMenu.add(sItem);
			}
		}

		// add user library scripts
		final ResourceLoader<QueryScript> userScriptLoader = queryScriptLibrary.userScriptFiles();
		final Iterator<QueryScript> userScriptIterator = userScriptLoader.iterator();
		if(userScriptIterator.hasNext()) {
			queryMenu.addSeparator();
			
			final JMenuItem lbl = new JMenuItem("-- User Library --");
			
			lbl.addActionListener( (evt) -> {
				if(Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().open(new File(QueryScriptLibrary.USER_SCRIPT_FOLDER));
					} catch (IOException e1) {
						LogUtil.warning(e1);
						Toolkit.getDefaultToolkit().beep();
					}
				}
			} );
			queryMenu.add(lbl);
		}
		while(userScriptIterator.hasNext()) {
			final QueryScript qs = userScriptIterator.next();
			
			final JMenuItem sItem = new JMenuItem(new QueryScriptCommand(project, qs));
			queryMenu.add(sItem);
		}
		
		// project scripts
		final ResourceLoader<QueryScript> projectScriptLoader = queryScriptLibrary.projectScriptFiles(project);
		final Iterator<QueryScript> projectScriptIterator = projectScriptLoader.iterator();
		if(projectScriptIterator.hasNext()) {
			queryMenu.addSeparator();
			final JMenuItem lbl = new JMenuItem("-- Project Library --");
			lbl.addActionListener( (evt) -> {
				if(Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().open(new File(QueryScriptLibrary.projectScriptFolder(project)));
					} catch (IOException e1) {
						LogUtil.warning(e1);
						Toolkit.getDefaultToolkit().beep();
					}
				}
			} );
			queryMenu.add(lbl);
		}
		while(projectScriptIterator.hasNext()) {
			final QueryScript qs = projectScriptIterator.next();
			
			final JMenuItem sItem = new JMenuItem(new QueryScriptCommand(project, qs));
			queryMenu.add(sItem);
		}
		
		// plug-in script
		final ResourceLoader<QueryScript> pluginScriptLoader = queryScriptLibrary.pluginScriptFiles(project);
		final Iterator<QueryScript> pluginScriptIterator = pluginScriptLoader.iterator();
		// organize into categories
		
		final Map<String, List<QueryScript>> categories = new TreeMap<String, List<QueryScript>>();
		
		while(pluginScriptIterator.hasNext()) {
			final QueryScript queryScript = pluginScriptIterator.next();
			final QueryName qn = queryScript.getExtension(QueryName.class);
			
			List<QueryScript> categoryScripts = categories.get(qn.getCategory());
			if(categoryScripts == null) {
				categoryScripts = new ArrayList<QueryScript>();
				categories.put(qn.getCategory(), categoryScripts);
			}
			categoryScripts.add(queryScript);
		}
		for(String category:categories.keySet()) {
			queryMenu.addSeparator();
			final JMenuItem lbl = new JMenuItem("-- " + category + " --");
			lbl.setEnabled(false);
			queryMenu.add(lbl);
			for(QueryScript qs:categories.get(category)) {
				final JMenuItem sItem = new JMenuItem(new QueryScriptCommand(project, qs));
				queryMenu.add(sItem);
			}
		}
		
		final PhonUIAction browseAct = new PhonUIAction(QueryMenuListener.class, "onBrowseForQuery", project);
		browseAct.putValue(PhonUIAction.NAME, "Browse...");
		browseAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Browse for query...");
		final JMenuItem browseItem = new JMenuItem(browseAct);
		
//		final JMenuItem historyItem = new JMenuItem(new QueryHistoryCommand(project));
		
		queryMenu.addSeparator();
		queryMenu.add(browseItem);
//		queryMenu.add(historyItem);
	}

	public static void onBrowseForQuery(PhonActionEvent pae) {
		final Project project = (Project)pae.getData();
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setRunAsync(true);
		props.setTitle("Open Query");
		props.setCanChooseDirectories(false);
		props.setCanChooseFiles(true);
		props.setAllowMultipleSelection(false);
		
		final FileFilter filter = new FileFilter(new FileFilter[] {FileFilter.jsFilter, FileFilter.xmlFilter});
		props.setFileFilter(filter);
		props.setListener( (e) -> {
			if(e.getDialogData() != null) {
				final String selectedFile = e.getDialogData().toString();
				SwingUtilities.invokeLater( () -> {
					try {
						URL url = (new File(selectedFile)).toURI().toURL();
						final QueryScriptCommand cmd = new QueryScriptCommand(project, new QueryScript(url));
						cmd.actionPerformed(pae.getActionEvent());
					} catch (MalformedURLException e1) {
						Toolkit.getDefaultToolkit().beep();
						LogUtil.severe(e1);
					}
				});
			}
		});
		NativeDialogs.showOpenDialog(props);
	}
	
}
