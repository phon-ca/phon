/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.opgraph.report;

import javax.swing.SwingUtilities;

import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.Processor;
import ca.phon.opgraph.exceptions.ProcessingException;
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
		this(graph, null);
	}

	public ReportRunner(OpGraph graph, Project project) {
		this(graph, project, "");
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
		ctx.put("_window", CommonModuleFrame.getCurrentFrame());
		ctx.put("_project", project);
		ctx.put("_queryId", queryId);

		final WizardExtension wizardExt = graph.getExtension(WizardExtension.class);
		if(wizardExt != null) {
			SwingUtilities.invokeLater(() -> {
				final NodeWizard wizard = wizardExt.createWizard(processor);
				wizard.putExtension(Project.class, project);
				wizard.pack();
				wizard.setSize(1024, 768);
				wizard.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
				wizard.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
				wizard.setVisible(true);
			});
		} else {
			processor.stepAll();
		}
	}

}
