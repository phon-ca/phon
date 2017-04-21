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
package ca.phon.app.opgraph.report;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import ca.gedge.opgraph.OpGraph;
import ca.phon.app.hooks.HookableAction;
import ca.phon.opgraph.OpgraphIO;
import ca.phon.project.Project;
import ca.phon.worker.PhonWorker;

public class ReportAction extends HookableAction {
	
	private final static Logger LOGGER = Logger.getLogger(ReportAction.class.getName());
	
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
		
	}
	
}
