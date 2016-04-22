package ca.phon.app.opgraph.assessment;

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

public class AssessmentAction extends HookableAction {

	private static final long serialVersionUID = 7095649504101466591L;

	private final static Logger LOGGER = Logger.getLogger(ReportAction.class.getName());
	
	private Project project;
	
	private List<SessionPath> selectedSessions;
	
	private URL assessmentURL;
	
	private boolean showWizard = true;
	
	public AssessmentAction(Project project, URL assessmentURL) {
		this(project, new ArrayList<>(), assessmentURL);
	}
	
	public AssessmentAction(Project project, List<SessionPath> selectedSessions, URL assessmentURL) {
		super();
		
		this.project = project;
		this.selectedSessions = selectedSessions;
		this.assessmentURL = assessmentURL;
		
		@SuppressWarnings("deprecation")
		String name = URLDecoder.decode(assessmentURL.getPath());
		if(name.endsWith(".xml")) name = name.substring(0, name.length()-4);
		if(name.endsWith(".opgraph")) name = name.substring(0, name.length()-8);
		final File asFile = new File(name);
		putValue(NAME, asFile.getName());
		putValue(SHORT_DESCRIPTION, assessmentURL.getPath());
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
			final OpGraph graph = loadAssessment();
			
			final AssessmentRunner assessmentRunner =
					new AssessmentRunner(graph, project, selectedSessions, showWizard);
			PhonWorker.getInstance().invokeLater(assessmentRunner);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	private OpGraph loadAssessment() throws IOException {
		return OpgraphIO.read(assessmentURL.openStream());
	}

}
