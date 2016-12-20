/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.opgraph.report;

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
import ca.phon.worker.PhonWorker;

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
				int padding = 100;
				wizard.setSize(
						Toolkit.getDefaultToolkit().getScreenSize().width - padding, 
						Toolkit.getDefaultToolkit().getScreenSize().height - padding);
				wizard.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
				wizard.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
				wizard.setVisible(true);
				
				if(wizard.numberOfSteps() == 1)
					PhonWorker.getInstance().invokeLater( wizard::executeGraph );
			});
		} else {
			processor.stepAll();
		}
	}
	
}
