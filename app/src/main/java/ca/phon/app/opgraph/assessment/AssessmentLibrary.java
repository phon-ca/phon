package ca.phon.app.opgraph.assessment;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.MenuElement;

import ca.phon.project.Project;
import ca.phon.session.SessionPath;
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
public class AssessmentLibrary {
	
	private final static String PROJECT_ASSESSMENTS_FOLDER = "__res/assessments";
	
	/**
	 * Report loader
	 */
	private ResourceLoader<URL> loader = new ResourceLoader<>();
	
	public AssessmentLibrary() {
		super();
		loader.addHandler(new StockAssessmentHandler());
		loader.addHandler(new UserAssessmentHandler());
	}
	
	public List<URL> getAvailableAssessments() {
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
		retVal.addHandler(new StockAssessmentHandler());
		return retVal;
	}
	
	public ResourceLoader<URL> getUserGraphs() {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();
		retVal.addHandler(new UserAssessmentHandler());
		return retVal;
	}
	
	public ResourceLoader<URL> getProjectGraphs(Project project) {
		final ResourceLoader<URL> retVal = new ResourceLoader<>();
		retVal.addHandler(new UserAssessmentHandler(new File(project.getLocation(), PROJECT_ASSESSMENTS_FOLDER)));
		return retVal;
	}
	
	public void setupMenu(Project project, List<SessionPath> selectedSessions, MenuElement menu) {
		final MenuBuilder builder = new MenuBuilder(menu);
		
		for(URL reportURL:getStockGraphs()) {
			final AssessmentAction act = new AssessmentAction(project, selectedSessions, reportURL);
			act.setShowWizard(false);
			builder.addMenuItem(".", act);
		}
		
		final Iterator<URL> userGraphIterator = getUserGraphs().iterator();
		if(userGraphIterator.hasNext()) builder.addSeparator(".", "user");
		while(userGraphIterator.hasNext()) {
			final URL reportURL = userGraphIterator.next();
			final AssessmentAction act = new AssessmentAction(project, selectedSessions, reportURL);
			builder.addMenuItem(".", act);
		}
		
		final Iterator<URL> projectGraphIterator = getProjectGraphs(project).iterator();
		if(projectGraphIterator.hasNext()) builder.addSeparator(".", "project");
		while(projectGraphIterator.hasNext()) {
			final URL reportURL = projectGraphIterator.next();
			final AssessmentAction act = new AssessmentAction(project, selectedSessions, reportURL);
			builder.addMenuItem(".", act);
		}
		
//		builder.addSeparator(".", "editor");
//		builder.addMenuItem(".@editor", new ReportEditorAction());
	}
	
}
