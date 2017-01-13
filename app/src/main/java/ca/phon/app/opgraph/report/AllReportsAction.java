package ca.phon.app.opgraph.report;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.phon.app.hooks.HookableAction;
import ca.phon.opgraph.OpgraphIO;
import ca.phon.project.Project;
import ca.phon.worker.PhonWorker;

public class AllReportsAction extends HookableAction {
	
	private static final long serialVersionUID = -8063986275087405031L;

	private final static Logger LOGGER = Logger.getLogger(AllReportsAction.class.getName());

	private Project project;
	
	private String queryId;
	
	public AllReportsAction(Project project, String queryId) {
		super();
		
		
		this.project = project;
		this.queryId = queryId;
		
		putValue(HookableAction.NAME, "All Reports...");
	}
	
	private OpGraph createAllReportsGraph() throws IllegalArgumentException, IOException, ItemMissingException, VertexNotFoundException, CycleDetectedException {
		final ReportMerger merger = new ReportMerger(project);
		return merger.createAllReportsGraph();
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final ReportWorker worker = new ReportWorker();
		worker.run();
	}

	private class ReportWorker extends SwingWorker<OpGraph, Object> {

		@Override
		protected OpGraph doInBackground() throws Exception {
			final OpGraph graph = createAllReportsGraph();
			
			// XXX for some reason, the merged report does not have optionals setup
			// in wizard correctly if not serialized first???
			final File tempFile = File.createTempFile("phon", "allreports");
			tempFile.deleteOnExit();
			
			OpgraphIO.write(graph, tempFile);
			
			final OpGraph retVal = OpgraphIO.read(tempFile);
			
			return retVal;
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
