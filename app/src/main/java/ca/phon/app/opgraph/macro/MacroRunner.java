package ca.phon.app.opgraph.macro;

import java.awt.Toolkit;

import javax.swing.SwingUtilities;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;

public class MacroRunner implements Runnable {
	
	private OpGraph graph;
	
	private Project project;
	
	private boolean showWizard = true;
	
	public MacroRunner(OpGraph graph, Project project, boolean showWizard) {
		super();
		this.graph = graph;
		this.project = project;
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

	public boolean isShowWizard() {
		return showWizard;
	}

	public void setShowWizard(boolean showWizard) {
		this.showWizard = showWizard;
	}

	@Override
	public void run() {
		run(getGraph(), getProject(), isShowWizard());
	}

	public void run(OpGraph graph, Project project, boolean showWizard) 
		throws ProcessingException {
		final Processor processor = new Processor(graph);
		final OpContext ctx = processor.getContext();
		ctx.put("_window", CommonModuleFrame.getCurrentFrame());
		ctx.put("_project", project);
		
		final WizardExtension wizardExt = graph.getExtension(WizardExtension.class);
		if(wizardExt != null && showWizard) {
			SwingUtilities.invokeLater( () -> {
				final NodeWizard wizard = wizardExt.createWizard(processor);
				wizard.setParentFrame(CommonModuleFrame.getCurrentFrame());
				wizard.pack();
				int padding = 100;
				wizard.setSize(
						Toolkit.getDefaultToolkit().getScreenSize().width - padding, 
						Toolkit.getDefaultToolkit().getScreenSize().height - padding);
				wizard.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
				wizard.setVisible(true);
			});
		} else {
			processor.stepAll();
		}
	}
}
