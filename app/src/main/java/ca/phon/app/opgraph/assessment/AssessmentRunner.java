package ca.phon.app.opgraph.assessment;

import java.awt.Toolkit;
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
	
	private boolean showWizard = true;
	
	public AssessmentRunner(OpGraph graph, Project project, 
			List<SessionPath> selectedSessions, boolean showWizard) {
		super();
		this.graph = graph;
		this.project = project;
		this.selectedSessions = selectedSessions;
		this.showWizard = showWizard;
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
	
	public boolean isShowWizard() {
		return showWizard;
	}

	public void setShowWizard(boolean showWizard) {
		this.showWizard = showWizard;
	}

	@Override
	public void run() {
		run(getGraph(), getProject(), getSelectedSessions(), isShowWizard());
	}

	public void run(OpGraph graph, Project project, List<SessionPath> selectedSessions, boolean showWizard) 
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
				int padding = 100;
				wizard.setSize(
						Toolkit.getDefaultToolkit().getScreenSize().width - padding, 
						Toolkit.getDefaultToolkit().getScreenSize().height - padding);
				wizard.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
				wizard.setVisible(true);
				
				if(!showWizard) {
					wizard.gotoStep(1);
				}
			});
		} else {
			processor.stepAll();
		}
	}
}
