package ca.phon.app.opgraph.analysis;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

import org.antlr.stringtemplate.language.ActionEvaluator;

import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.Tuple;
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
	
	private final static String PROJECT_ANALYSIS_FOLDER = "__res/analysis";
	
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
		retVal.addHandler(new UserAnalysisHandler(new File(project.getLocation(), PROJECT_ANALYSIS_FOLDER)));
		return retVal;
	}
	
	public void setupMenu(Project project, List<SessionPath> selectedSessions, MenuElement menu) {
		final MenuBuilder builder = new MenuBuilder(menu);
		
		for(URL reportURL:getStockGraphs()) {
			final AnalysisAction act = new AnalysisAction(project, selectedSessions, reportURL);
			act.setShowWizard(selectedSessions.size() == 0);
			builder.addMenuItem(".", act);
		}
		
		final Iterator<URL> userGraphIterator = getUserGraphs().iterator();
		if(userGraphIterator.hasNext()) {
			builder.addSeparator(".", "user");
		}
		while(userGraphIterator.hasNext()) {
			final URL reportURL = userGraphIterator.next();
			final AnalysisAction act = new AnalysisAction(project, selectedSessions, reportURL);
			act.setShowWizard(selectedSessions.size() == 0);
			builder.addMenuItem(".", act);
		}
		
		final Iterator<URL> projectGraphIterator = getProjectGraphs(project).iterator();
		if(projectGraphIterator.hasNext()) {
			builder.addSeparator(".", "project");
			// TODO add menu header
		}
		while(projectGraphIterator.hasNext()) {
			final URL reportURL = projectGraphIterator.next();
			final AnalysisAction act = new AnalysisAction(project, selectedSessions, reportURL);
			act.setShowWizard(selectedSessions.size() == 0);
			builder.addMenuItem(".", act);
		}
		
		builder.addSeparator(".", "editor");
		final PhonUIAction showEditorAct = new PhonUIAction(AnalysisLibrary.class, "showEditor");
		showEditorAct.putValue(PhonUIAction.NAME, "Analysis Editor...");
		showEditorAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Open analysis editor");
		builder.addMenuItem(".@editor", showEditorAct);
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
	
	private Tuple<String, String> URLtoName(URL assessmentURL) {
		Tuple<String, String> retVal = new Tuple<>();
		
		@SuppressWarnings("deprecation")
		String name = URLDecoder.decode(assessmentURL.getPath());
		if(name.endsWith(".xml")) name = name.substring(0, name.length()-4);
		if(name.endsWith(".opgraph")) name = name.substring(0, name.length()-8);
		
		final File asFile = new File(name);
		if(asFile.getParentFile() != null) {
			retVal.setObj1(asFile.getParentFile().getName());
			retVal.setObj2(asFile.getAbsolutePath().substring(asFile.getParent().length()+1));
		} else {
			retVal.setObj1("");
			retVal.setObj2(asFile.getName());
		}
		
		return retVal;
	}
	
}
