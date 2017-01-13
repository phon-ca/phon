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
package ca.phon.app.opgraph.report;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.MenuElement;

import ca.phon.project.Project;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.resources.ResourceLoader;

/**
 * <p>Library of query reports. These reports are available
 * from the 'Report' menu button in the query and query history
 * dialogs.</p>
 * 
 * <p>Reports are stored in <code>~/Documents/Phon/reports</code>
 * by default. Reports can also be stored in the project 
 * <code>__res/reports/</code> folder.</p>
 */
public class ReportLibrary {
	
	private final static String PROJECT_REPORT_FOLDER = "reports";
	
	/**
	 * Report loader
	 */
	private ResourceLoader<URL> loader = new ResourceLoader<>();
	
	public ReportLibrary() {
		super();
		loader.addHandler(new StockReportHandler());
		loader.addHandler(new UserReportHandler());
	}
	
	public List<URL> getAvailableReports() {
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
		retVal.addHandler(new StockReportHandler());
		return retVal;
	}
	
	public ResourceLoader<URL> getUserGraphs() {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();
		retVal.addHandler(new UserReportHandler());
		return retVal;
	}
	
	public ResourceLoader<URL> getProjectGraphs(Project project) {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();
		retVal.addHandler(new UserReportHandler(new File(project.getResourceLocation(), PROJECT_REPORT_FOLDER)));
		return retVal;
	}
	
	public void setupMenu(Project project, String queryId, MenuElement menu) {
		final MenuBuilder builder = new MenuBuilder(menu);
		
		final JMenuItem stockSep = new JMenuItem("Stock Reports");
		stockSep.setEnabled(false);
		builder.addItem(".", stockSep);
		for(URL reportURL:getStockGraphs()) {
			final ReportAction act = new ReportAction(project, queryId, reportURL);
			builder.addItem(".", act);
		}
		
		final Iterator<URL> userGraphIterator = getUserGraphs().iterator();
		if(userGraphIterator.hasNext()) {
			builder.addSeparator(".", "user");
			final JMenuItem userSep = new JMenuItem("User Reports");
			userSep.setEnabled(false);
			builder.addItem(".", userSep);
		}
		while(userGraphIterator.hasNext()) {
			final URL reportURL = userGraphIterator.next();
			final ReportAction act = new ReportAction(project, queryId, reportURL);
			builder.addItem(".", act);
		}
		
		final Iterator<URL> projectGraphIterator = getProjectGraphs(project).iterator();
		if(projectGraphIterator.hasNext()) {
			builder.addSeparator(".", "project");
			final JMenuItem projectSep = new JMenuItem("Project Reports");
			projectSep.setEnabled(false);
			builder.addItem(".", projectSep);
		}
		while(projectGraphIterator.hasNext()) {
			final URL reportURL = projectGraphIterator.next();
			final ReportAction act = new ReportAction(project, queryId, reportURL);
			builder.addItem(".", act);
		}
		
		builder.addSeparator(".", "all");
		builder.addItem(".@all", new AllReportsAction(project, queryId));
		
		builder.addSeparator(".", "editor");
		builder.addItem(".@editor", new ReportEditorAction());
	}
	
}
