/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.macro;

import ca.phon.app.opgraph.wizard.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;

import javax.swing.*;

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
				wizard.setSize(1024, 768);
				wizard.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
				wizard.setVisible(true);
			});
		} else {
			processor.stepAll();
		}
	}
}
