package ca.phon.app.opgraph.report;

import java.awt.MenuContainer;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.MenuElement;

import ca.gedge.opgraph.OpGraph;
import ca.phon.project.Project;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.PrefHelper;
import ca.phon.util.resources.ResourceHandler;
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
	
	
	public final static String DEFAULT_USER_REPORT_FOLDER = 
			PrefHelper.getUserDataFolder() + File.separator + "reports";
	
	private final static String PROJECT_REPORT_FOLDER = "__res/reports";
	
	/**
	 * Report loader
	 */
	private ResourceLoader<URL> loader = new ResourceLoader<>();
	
	public ReportLibrary() {
		super();
		loader.addHandler(new StockReportHandler());
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
	
	public void setupMenu(Project project, String queryId, MenuElement menu) {
		final MenuBuilder builder = new MenuBuilder(menu);
		
		for(URL reportURL:getAvailableReports()) {
			final ReportAction act = new ReportAction(project, queryId, reportURL);
			builder.addMenuItem(".", act);
		}
		builder.addSeparator(".", "editor");
		builder.addMenuItem(".@editor", new ReportEditorAction());
	}
	
}
