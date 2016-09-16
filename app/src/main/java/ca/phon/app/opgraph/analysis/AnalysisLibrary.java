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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.MenuElement;

import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
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
		retVal.addHandler(new UserAnalysisHandler(new File(project.getResourceLocation(), PROJECT_ANALYSIS_FOLDER)));
		return retVal;
	}
	
	public void setupMenu(Project project, List<SessionPath> selectedSessions, MenuElement menu) {
		final MenuBuilder builder = new MenuBuilder(menu);
		
		final List<String> createdFolders = new ArrayList<>();
		for(URL reportURL:getStockGraphs()) {
			final AnalysisAction act = new AnalysisAction(project, selectedSessions, reportURL);
			act.setShowWizard(selectedSessions.size() == 0);
			
			String menuPath = ".";
			final String path = reportURL.getPath();
			final String parentPath = path.substring(0, path.lastIndexOf('/'));
			try {
				final String parentFolder = 
						URLDecoder.decode(parentPath.substring(parentPath.lastIndexOf('/')+1), "UTF-8");
				if(!parentFolder.equals(PROJECT_ANALYSIS_FOLDER)) {
					if(!createdFolders.contains(parentFolder)) {
						builder.addMenu(".", parentFolder);
						createdFolders.add(parentFolder);
					}
					menuPath = parentFolder;
				}
			} catch (UnsupportedEncodingException e) { }
			
			builder.addItem(menuPath, act);
		}
		
		final Iterator<URL> userGraphIterator = getUserGraphs().iterator();
		if(userGraphIterator.hasNext()) {
			builder.addSeparator(".", "user");
			final JMenuItem sepItem = new JMenuItem("-- User Library --");
			sepItem.setEnabled(false);
			builder.addItem(".@user", sepItem);
		}
		while(userGraphIterator.hasNext()) {
			final URL reportURL = userGraphIterator.next();
			final AnalysisAction act = new AnalysisAction(project, selectedSessions, reportURL);
			act.setShowWizard(selectedSessions.size() == 0);
			builder.addItem(".", act);
		}
		
		final Iterator<URL> projectGraphIterator = getProjectGraphs(project).iterator();
		if(projectGraphIterator.hasNext()) {
			builder.addSeparator(".", "project");
			final JMenuItem sepItem = new JMenuItem("-- Project Library --");
			sepItem.setEnabled(false);
			builder.addItem(".@user", sepItem);
		}
		while(projectGraphIterator.hasNext()) {
			final URL reportURL = projectGraphIterator.next();
			final AnalysisAction act = new AnalysisAction(project, selectedSessions, reportURL);
			act.setShowWizard(selectedSessions.size() == 0);
			builder.addItem(".", act);
		}
		
		builder.addSeparator(".", "editor");
		final PhonUIAction showEditorAct = new PhonUIAction(AnalysisLibrary.class, "showEditor");
		showEditorAct.putValue(PhonUIAction.NAME, "Analysis Editor...");
		showEditorAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Open analysis editor");
		builder.addItem(".@editor", showEditorAct);
	}
	
	public static void showEditor() {
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
