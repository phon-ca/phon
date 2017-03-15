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
package ca.phon.app.opgraph.analysis;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import ca.gedge.opgraph.OpGraph;
import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.report.ReportAction;
import ca.phon.opgraph.OpgraphIO;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;
import ca.phon.worker.PhonWorker;

public class AnalysisAction extends HookableAction {

	private static final long serialVersionUID = 7095649504101466591L;

	private final static Logger LOGGER = Logger.getLogger(ReportAction.class.getName());

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
		worker.run();
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}


	}

}
