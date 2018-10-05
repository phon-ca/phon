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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.OpgraphIO;
import ca.phon.app.opgraph.report.ReportAction;
import ca.phon.opgraph.OpGraph;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;
import ca.phon.worker.PhonWorker;

public class AnalysisAction extends HookableAction {

	private static final long serialVersionUID = 7095649504101466591L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ReportAction.class.getName());

	private Project project;

	private List<SessionPath> selectedSessions;

	private URL analysisURL;

	private boolean showWizard = true;

	public AnalysisAction(Project project, URL analysisURL) {
		this(project, new ArrayList<>(), analysisURL);
	}

	public AnalysisAction(Project project, List<SessionPath> selectedSessions, URL analysisURL) {
		super();

		this.project = project;
		this.selectedSessions = selectedSessions;
		this.analysisURL = analysisURL;

		@SuppressWarnings("deprecation")
		String name = URLDecoder.decode(analysisURL.getPath());
		if(name.endsWith(".xml")) name = name.substring(0, name.length()-4);
		if(name.endsWith(".opgraph")) name = name.substring(0, name.length()-8);
		final File asFile = new File(name);
		putValue(NAME, asFile.getName() + "...");
		putValue(SHORT_DESCRIPTION, analysisURL.getPath());
	}

	public boolean isShowWizard() {
		return showWizard;
	}

	public void setShowWizard(boolean showWizard) {
		this.showWizard = showWizard;
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final AnalysisWorker worker = new AnalysisWorker();
		worker.execute();
	}

	private OpGraph loadAnalysis() throws IOException {
		return OpgraphIO.read(analysisURL.openStream());
	}

	private class AnalysisWorker extends SwingWorker<OpGraph, Object> {

		@Override
		protected OpGraph doInBackground() throws Exception {
			final OpGraph graph = loadAnalysis();
			return graph;
		}

		@Override
		protected void done() {
			try {
				final AnalysisRunner analysisRunner =
						new AnalysisRunner(get(), project, selectedSessions, showWizard);
				PhonWorker.getInstance().invokeLater(analysisRunner);
			} catch (ExecutionException | InterruptedException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}


	}

}
