package ca.phon.app.opgraph.report;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		putValue(NAME, asFile.getName());
		putValue(SHORT_DESCRIPTION, reportURL.getPath());
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		try {
			final OpGraph graph = loadReport();
			
			final ReportRunner reportRunner = new ReportRunner(graph, project, queryId);
			PhonWorker.getInstance().invokeLater(reportRunner);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	private OpGraph loadReport() throws IOException {
		return OpgraphIO.read(reportURL.openStream());
	}
	
}
