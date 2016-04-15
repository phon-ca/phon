package ca.phon.app.opgraph.assessment;

import java.util.List;

import javax.swing.SwingUtilities;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;

public class AssessmentRunner implements Runnable {
	
	private OpGraph graph;
	
	private Project project;
	
	private List<SessionPath> selectedSessions;
	
	public AssessmentRunner(OpGraph graph, Project project, List<SessionPath> selectedSessions) {
		super();
		this.graph = graph;
		this.project = project;
		this.selectedSessions = selectedSessions;
	}
	
	public OpGraph getGraph() {
		return graph;
	}

	public void setGraph(OpGraph graph) {
		this.graph = graph;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public List<SessionPath> getSelectedSessions() {
		return selectedSessions;
	}

	public void setSelectedSessions(List<SessionPath> selectedSessions) {
		this.selectedSessions = selectedSessions;
	}

	@Override
	public void run() {
		run(getGraph(), getProject(), getSelectedSessions());
	}

	public void run(OpGraph graph, Project project, List<SessionPath> selectedSessions) 
		throws ProcessingException {
		final Processor processor = new Processor(graph);
		final OpContext ctx = processor.getContext();
		ctx.put("_project", project);
		ctx.put("_selectedSessions", selectedSessions);
		
		final WizardExtension wizardExt = graph.getExtension(WizardExtension.class);
		if(wizardExt != null) {
			SwingUtilities.invokeLater( () -> {
				final NodeWizard wizard = wizardExt.createWizard(processor);
				wizard.pack();
				wizard.setSize(1024, 768);
				wizard.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
				wizard.setVisible(true);
			});
		} else {
			processor.stepAll();
		}
	}
}
