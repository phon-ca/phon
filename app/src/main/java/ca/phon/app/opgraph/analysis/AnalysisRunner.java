/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.analysis;

import java.awt.Toolkit;
import java.util.*;

import javax.swing.SwingUtilities;

import ca.gedge.opgraph.*;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.opgraph.wizard.*;
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
				int padding = 100;
				wizard.setSize(
						Toolkit.getDefaultToolkit().getScreenSize().width - padding,
						Toolkit.getDefaultToolkit().getScreenSize().height - padding);
				wizard.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
				wizard.setVisible(true);

				wizard.gotoStep(0);
			});
		} else {
			processor.stepAll();
		}
	}
}
