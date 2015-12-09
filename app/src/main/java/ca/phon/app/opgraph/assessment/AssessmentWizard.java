package ca.phon.app.opgraph.assessment;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;

public class AssessmentWizard extends NodeWizard {

	private Project project;
	
	private SessionSelector sessionSelector;
	
	public AssessmentWizard(String title, Processor processor, OpGraph graph) {
		super(title, processor, graph);
	}
	
	public void setProject(Project project) {
		this.project = project;
		this.sessionSelector = new SessionSelector(project);
		
		add(new JScrollPane(sessionSelector), BorderLayout.WEST);
	}
	
	public Project getProject() {
		return this.project;
	}
	
	public SessionSelector getSessionSelector() {
		return this.sessionSelector;
	}

}
