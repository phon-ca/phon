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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;

import ca.phon.app.log.*;
import ca.phon.app.opgraph.wizard.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.project.*;
import ca.phon.session.*;
import ca.phon.ui.*;

public class AnalysisRunner implements Runnable {

	private URL graphURL;
	
	private OpGraph graph;

	private Project project;

	private List<SessionPath> selectedSessions;

	private NodeWizard wizard;

	private boolean showWizard = true;
	
	public AnalysisRunner(URL graphURL, Project project, 
			List<SessionPath> selectedSessions, boolean showWizard) {
		super();
		this.graphURL = graphURL;
		this.project = project;
		this.selectedSessions = selectedSessions;
		this.showWizard = showWizard;
	}

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

	private OpGraph loadAnalysis() throws IOException {
		return OpgraphIO.read(graphURL.openStream());
	}
	
	@Override
	public void run() {
		run(getProject(), getSelectedSessions(), isShowWizard());
	}

	public void run(Project project, List<SessionPath> selectedSessions, boolean showWizard)
		throws ProcessingException {

		if(showWizard) {
			SwingUtilities.invokeLater(() -> {
				AnalysisWizard wizard = new AnalysisWizard("Analysis", project);
				AnalysisWizardWorker worker = new AnalysisWizardWorker(wizard);
				wizard.pack();
				wizard.setSize(1024, 768);
				wizard.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
				wizard.setVisible(true);
				
				worker.execute();
			});
		} else {
			if(graph == null && graphURL != null) {
				try {
					graph = loadAnalysis();
				} catch (IOException e) {
					throw new ProcessingException(null, e);
				}
			}
			final Processor processor = new Processor(graph);
			final OpContext ctx = processor.getContext();
			ctx.put("_window", CommonModuleFrame.getCurrentFrame());
			ctx.put("_project", project);
			ctx.put("_selectedSessions", selectedSessions);
			processor.stepAll();
		}
	}
	
	private class AnalysisWizardWorker extends SwingWorker<OpGraph, Object> {

		NodeWizard wizard;
		
		public AnalysisWizardWorker(NodeWizard wizard) {
			this.wizard = wizard;
		}
		
		@Override
		protected OpGraph doInBackground() throws Exception {
			if(getGraph() == null && graphURL != null) {
				wizard.loadGraph(graphURL);
			} else {
				wizard.loadGraph(getGraph());
			}
			
			return wizard.getGraph();
		}

		@Override
		protected void done() {
			try {
				OpGraph graph = get();
				WizardExtension ext = graph.getExtension(WizardExtension.class);
				if(ext != null)
					wizard.setWindowName("Analysis : " + (ext.getWizardTitle() != null ? ext.getWizardTitle() : "Unknown" ));
				
				final Processor processor = wizard.getProcessor();
				final OpContext ctx = processor.getContext();
				ctx.put("_window", CommonModuleFrame.getCurrentFrame());
				ctx.put("_project", project);
				ctx.put("_selectedSessions", selectedSessions);
				
				wizard.gotoStep(0);
			} catch (InterruptedException | ExecutionException e) {
				LogUtil.warning(e);
			}			
		}

	}	
	
}
