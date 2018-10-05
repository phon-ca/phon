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
package ca.phon.app.opgraph.analysis;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.Processor;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;

public class AnalysisRunner implements Runnable {

	private OpGraph graph;

	private Project project;

	private List<SessionPath> selectedSessions;

	private NodeWizard wizard;

	private boolean showWizard = true;

	public AnalysisRunner(OpGraph graph, Project project) {
		this(graph, project, new ArrayList<>(), true);
	}

	public AnalysisRunner(OpGraph graph, Project project,
			List<SessionPath> selectedSessions, boolean showWizard) {
		super();
		this.graph = graph;
		this.project = project;
		this.selectedSessions = selectedSessions;
		this.showWizard = showWizard;
	}

	public void setWizard(NodeWizard wizard) {
		this.wizard = wizard;
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
		ctx.put("_window", CommonModuleFrame.getCurrentFrame());
		ctx.put("_project", project);
		ctx.put("_selectedSessions", selectedSessions);

		final WizardExtension wizardExt = graph.getExtension(WizardExtension.class);
		if(wizardExt != null && showWizard) {
			SwingUtilities.invokeLater( () -> {
				final NodeWizard wizard = wizardExt.createWizard(processor);
//				wizard.setParentFrame(CommonModuleFrame.getCurrentFrame());
				wizard.pack();
				wizard.setSize(1024, 768);
				wizard.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
				wizard.setVisible(true);

				wizard.gotoStep(0);
			});
		} else {
			processor.stepAll();
		}
	}
}
