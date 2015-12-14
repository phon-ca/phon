package ca.phon.app.opgraph.report;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.opgraph.OpgraphIO;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;

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
		
		String name = reportURL.getPath();
		if(name.endsWith(".xml")) name = name.substring(0, name.length()-4);
		if(name.endsWith(".opgraph")) name = name.substring(0, name.length()-8);
		final File asFile = new File(name);
		putValue(NAME, StringEscapeUtils.unescapeHtml4(asFile.getName()));
		putValue(SHORT_DESCRIPTION, reportURL.getPath());
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		try {
			final OpGraph graph = loadReport();
			
			final Processor processor = new Processor(graph);
			final OpContext ctx = processor.getContext();
			ctx.put("_project", project);
			ctx.put("_queryId", queryId);
			
			final WizardExtension wizardExt = graph.getExtension(WizardExtension.class);
			if(wizardExt != null) {
				final NodeWizard wizard = wizardExt.createWizard(processor);
				wizard.pack();
				wizard.setSize(1024, 768);
				wizard.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
				wizard.setVisible(true);
			} else {
				final Runnable inBg = () -> {
					try {
						processor.stepAll();
					} catch (ProcessingException pe) {
						LOGGER.log(Level.SEVERE, pe.getLocalizedMessage(), pe);
					}
				};
				SwingUtilities.invokeLater(inBg);
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	private OpGraph loadReport() throws IOException {
		return OpgraphIO.read(reportURL.openStream());
	}
	
}
