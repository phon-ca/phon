package ca.phon.app.opgraph.report;

import javax.swing.SwingUtilities;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;

/**
 * Execute an opgraph report given a project and query.
 *
 */
public class ReportRunner implements Runnable {
	
	private OpGraph graph;
	
	private Project project;
	
	private String queryId;
	
	public ReportRunner() {
		super();
	}
	
	public ReportRunner(OpGraph graph) {
		super();
		this.graph = graph;
	}
	
	public ReportRunner(OpGraph graph, Project project, String queryId) {
		super();
		this.graph = graph;
		this.project = project;
		this.queryId = queryId;
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

	public String getQueryId() {
		return queryId;
	}

	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}

	public void run() {
		run(getGraph(), getProject(), getQueryId());
	}
	
	public void run(OpGraph graph, Project project, String queryId) throws ProcessingException {
		final Processor processor = new Processor(graph);
		final OpContext ctx = processor.getContext();
		ctx.put("_project", project);
		ctx.put("_queryId", queryId);
		
		final WizardExtension wizardExt = graph.getExtension(WizardExtension.class);
		if(wizardExt != null) {
			SwingUtilities.invokeLater(() -> {
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
