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
package ca.phon.app.opgraph.analysis;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.report.ReportAction;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;
import ca.phon.worker.PhonWorker;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.*;
import java.util.*;

public class AnalysisAction extends HookableAction {

	private static final long serialVersionUID = 7095649504101466591L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ReportAction.class.getName());

	private Project project;

	private List<SessionPath> selectedSessions;

	private URL analysisURL;

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

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final AnalysisRunner analysisRunner =
				new AnalysisRunner(analysisURL, project, selectedSessions, true);
		PhonWorker.getInstance().invokeLater(analysisRunner);
	}

}
