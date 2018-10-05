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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.OpgraphIO;
import ca.phon.opgraph.OpGraph;
import ca.phon.project.Project;
import ca.phon.worker.PhonWorker;

public class ReportAction extends HookableAction {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ReportAction.class.getName());
	
	private static final long serialVersionUID = -5178033211126700430L;
	
	private Project project;
	
	private String queryId;
	
	private URL reportURL;

	public ReportAction(Project project, String queryId, URL reportURL) {
		super();
		
		this.project = project;
		this.queryId = queryId;
		this.reportURL = reportURL;
		
		@SuppressWarnings("deprecation")
		String name = URLDecoder.decode(reportURL.getPath());
		if(name.endsWith(".xml")) name = name.substring(0, name.length()-4);
		if(name.endsWith(".opgraph")) name = name.substring(0, name.length()-8);
		final File asFile = new File(name);
		putValue(NAME, asFile.getName() + "...");
		putValue(SHORT_DESCRIPTION, reportURL.getPath());
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final ReportWorker worker = new ReportWorker();
		worker.run();
	}
	
	private OpGraph loadReport() throws IOException {
		return OpgraphIO.read(reportURL.openStream());
	}
	
	private class ReportWorker extends SwingWorker<OpGraph, Object> {

		@Override
		protected OpGraph doInBackground() throws Exception {
			final OpGraph graph = loadReport();
			return graph;
		}

		@Override
		protected void done() {
			try {
				final ReportRunner reportRunner = new ReportRunner(get(), project, queryId);
				PhonWorker.getInstance().invokeLater(reportRunner);
			} catch (ExecutionException | InterruptedException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		
		
	}
	
}
