package ca.phon.app.opgraph.analysis;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.gedge.opgraph.OpGraph;
import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.report.ReportAction;
import ca.phon.opgraph.OpgraphIO;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;
import ca.phon.worker.PhonWorker;

public class AnalysisAction extends HookableAction {

	private static final long serialVersionUID = 7095649504101466591L;

	private final static Logger LOGGER = Logger.getLogger(ReportAction.class.getName());
	
	private Project project;
	
	private List<SessionPath> selectedSessions;
	
	private URL analysisURL;
	
	private boolean showWizard = true;
	
	public AnalysisAction(Project project, URL analysisURL) {
		this(project, new ArrayList<>(), analysisURL);
	}
	
	public AnalysisAction(Project project, List<SessionPath> selectedSessions, URL analysisURL) {
		super();
		
		this.project = project;
		this.selectedSessions = selectedSessions;
		this.analysisURL = analysisURL;
		
		@SuppressWarnings("deprecation")
		String name = URLDecoder.decode(analysisURL.getPath());
		if(name.endsWith(".xml")) name = name.substring(0, name.length()-4);
		if(name.endsWith(".opgraph")) name = name.substring(0, name.length()-8);
		final File asFile = new File(name);
		putValue(NAME, asFile.getName());
		putValue(SHORT_DESCRIPTION, analysisURL.getPath());
	}
	
	public boolean isShowWizard() {
		return showWizard;
	}

	public void setShowWizard(boolean showWizard) {
		this.showWizard = showWizard;
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		try {
			final OpGraph graph = loadAnalysis();
			
			final AnalysisRunner analysisRunner =
					new AnalysisRunner(graph, project, selectedSessions, showWizard);
			PhonWorker.getInstance().invokeLater(analysisRunner);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	private OpGraph loadAnalysis() throws IOException {
		return OpgraphIO.read(analysisURL.openStream());
	}

}
