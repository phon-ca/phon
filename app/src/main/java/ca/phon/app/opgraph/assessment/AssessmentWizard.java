package ca.phon.app.opgraph.assessment;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;

public class AssessmentWizard extends NodeWizard {

	private static final long serialVersionUID = -3667158379797520370L;

	private Project project;
	
	private SessionSelector sessionSelector;
	
	public AssessmentWizard(String title, Processor processor, OpGraph graph) {
		super(title, processor, graph);
		
		if(processor.getContext().containsKey("_project")) {
			setProject((Project)processor.getContext().get("_project"));
			
			if(processor.getContext().containsKey("_selectedSessions")) {
				@SuppressWarnings("unchecked")
				final List<SessionPath> selectedSessions = 
						(List<SessionPath>)processor.getContext().get("_selectedSessions");
				sessionSelector.setSelectedSessions(selectedSessions);
			}
		}
	}
	
	public void setProject(Project project) {
		this.project = project;
		this.sessionSelector = new SessionSelector(project);
		
		final JScrollPane scroller = new JScrollPane(sessionSelector);
		scroller.setBorder(BorderFactory.createTitledBorder("Select sessions"));
		add(scroller, BorderLayout.WEST);
	}
	
	public Project getProject() {
		return this.project;
	}
	
	public SessionSelector getSessionSelector() {
		return this.sessionSelector;
	}

}
