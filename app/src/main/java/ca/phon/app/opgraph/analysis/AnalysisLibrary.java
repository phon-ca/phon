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
package ca.phon.app.opgraph.analysis;

import java.awt.Font;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.OpenFileLauncher;
import ca.phon.util.resources.ResourceLoader;

/**
 * <p>Library of analysis. These analysis are available
 * from the 'Analysis' menu button in the query and query history
 * dialogs.</p>
 * 
 * <p>Reports are stored in <code>~/Documents/Phon/reports</code>
 * by default. Reports can also be stored in the project 
 * <code>__res/reports/</code> folder.</p>
 */
public class AnalysisLibrary {
	
	private final static Logger LOGGER = Logger.getLogger(AnalysisLibrary.class.getName());
	
	private final static String PROJECT_ANALYSIS_FOLDER = "analysis";
	
	/**
	 * Report loader
	 */
	private ResourceLoader<URL> loader = new ResourceLoader<>();
	
	public AnalysisLibrary() {
		super();
		loader.addHandler(new StockAnalysisHandler());
		loader.addHandler(new UserAnalysisHandler());
	}
	
	public List<URL> getAvailableAnalysis() {
		List<URL> retVal = new ArrayList<URL>();
		
		final Iterator<URL> reportIterator = loader.iterator();
		while(reportIterator.hasNext()) {
			final URL url = reportIterator.next();
			if(url == null) continue;
			retVal.add(url);
		}
		
		return retVal;
	}
	
	public ResourceLoader<URL> getStockGraphs() {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();
		retVal.addHandler(new StockAnalysisHandler());
		return retVal;
	}
	
	public ResourceLoader<URL> getUserGraphs() {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();
		retVal.addHandler(new UserAnalysisHandler());
		return retVal;
	}
	
	public ResourceLoader<URL> getProjectGraphs(Project project) {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();
		retVal.addHandler(new UserAnalysisHandler(getProjectAnalysisFolder(project)));
		return retVal;
	}
	
	public File getProjectAnalysisFolder(Project project) {
		return new File(project.getResourceLocation(), PROJECT_ANALYSIS_FOLDER);
	}
	
	public void setupMenu(Project project, List<SessionPath> selectedSessions, MenuElement menu) {
		final MenuBuilder builder = new MenuBuilder(menu);
		
		for(URL reportURL:getStockGraphs()) {
			final AnalysisAction act = new AnalysisAction(project, selectedSessions, reportURL);
			
			try {
				final String fullPath = URLDecoder.decode(reportURL.getPath(), "UTF-8");
				final String relativePath = 
						fullPath.substring(fullPath.lastIndexOf(PROJECT_ANALYSIS_FOLDER + "/")+PROJECT_ANALYSIS_FOLDER.length()+1);
				
				String menuPath = ".";
				int lastFolderIndex = relativePath.lastIndexOf('/');
				if(lastFolderIndex >= 0) {
					menuPath = relativePath.substring(0, lastFolderIndex);
				}
				
				builder.addItem(menuPath, act);
			} catch (UnsupportedEncodingException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
		final JMenu userMenu = new JMenu("User Library");
		final MenuBuilder userMenuBuilder = new MenuBuilder(userMenu);
		final Iterator<URL> userGraphIterator = getUserGraphs().iterator();
		while(userGraphIterator.hasNext()) {
			try {
				final URL reportURL = userGraphIterator.next();
				final URI relativeURI = 
						(new File(UserAnalysisHandler.DEFAULT_USER_ANALYSIS_FOLDER)).toURI().relativize(reportURL.toURI());
				
				final String relativePath = URLDecoder.decode(relativeURI.getPath(), "UTF-8");
				String menuPath = ".";
				int lastFolderIndex = relativePath.lastIndexOf('/');
				if(lastFolderIndex >= 0) {
					menuPath += "/" + relativePath.substring(0, lastFolderIndex);
				}
				
				final AnalysisAction act = new AnalysisAction(project, selectedSessions, reportURL);
				act.setShowWizard(selectedSessions.size() == 0);
				userMenuBuilder.addItem(menuPath, act);
			} catch (URISyntaxException | UnsupportedEncodingException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		if(userMenu.getMenuComponentCount() > 0) {
			builder.addSeparator(".", "user_library");
			final JMenuItem userLibItem = builder.addItem(".@user_library", "-- User Library --");
			userLibItem.setFont(userLibItem.getFont().deriveFont(Font.BOLD));
			final File userLibFolder = new File(UserAnalysisHandler.DEFAULT_USER_ANALYSIS_FOLDER);
			userLibItem.setToolTipText("Show folder " + userLibFolder.getAbsolutePath());
			userLibItem.addActionListener( (e) -> {
				try {
					OpenFileLauncher.openURL( userLibFolder.toURI().toURL() );
				} catch (MalformedURLException e1) {
					LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
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
				
				final AnalysisAction act = new AnalysisAction(project, selectedSessions, reportURL);
				act.setShowWizard(selectedSessions.size() == 0);
				projectMenuBuilder.addItem(menuPath, act);
			} catch (URISyntaxException | UnsupportedEncodingException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		if(projectMenu.getMenuComponentCount() > 0) {
			builder.addSeparator(".", "project_library");
			final JMenuItem projectSepItem = builder.addItem(".@project_library", "-- Project Library --");
			projectSepItem.setFont(projectSepItem.getFont().deriveFont(Font.BOLD));
			final File projectFolder = getProjectAnalysisFolder(project);
			projectSepItem.addActionListener( (e) -> {
				try {
					OpenFileLauncher.openURL( projectFolder.toURI().toURL() );
				} catch (MalformedURLException e1) {
					LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
				}
			});
			projectSepItem.setToolTipText("Show folder " + projectFolder.getAbsolutePath());
			projectMenuBuilder.appendSubItems(".@-- Project Library --", projectMenu.getPopupMenu());
		}
		
		builder.addSeparator(".", "composer");
		final PhonUIAction showComposerAct = new PhonUIAction(AnalysisLibrary.class, "showComposer");
		showComposerAct.putValue(PhonUIAction.NAME, "Analysis Composer...");
		showComposerAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Create a new analysis using Composer...");
		builder.addItem(".@composer", showComposerAct);
	}
	
	public static void showComposer() {
		final AnalysisOpGraphEditorModel editorModel = new AnalysisOpGraphEditorModel();
		final OpgraphEditor editor =  new OpgraphEditor(editorModel);
		
		((AnalysisOpGraphEditorModel)editor.getModel()).getSessionSelector().setProject(
				CommonModuleFrame.getCurrentFrame().getExtension(Project.class));
		
		editor.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
		editor.pack();
		editor.setSize(1024, 768);
		editor.setVisible(true);
	}
	
}
