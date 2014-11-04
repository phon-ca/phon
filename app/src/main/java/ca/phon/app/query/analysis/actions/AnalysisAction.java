package ca.phon.app.query.analysis.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.query.ScriptPanel;
import ca.phon.app.query.analysis.QueryAnalysisWizard;
import ca.phon.project.Project;
import ca.phon.query.analysis.QueryAnalysis;
import ca.phon.query.analysis.QueryStep;
import ca.phon.query.analysis.ScriptReportStep;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.script.BasicScript;
import ca.phon.script.PhonScript;
import ca.phon.ui.CommonModuleFrame;

public class AnalysisAction extends HookableAction {
	
	private static final Logger LOGGER = Logger
			.getLogger(AnalysisAction.class.getName());

	private static final long serialVersionUID = -3097792087697477945L;
	
	private QueryScript queryScript;
	
	private PhonScript reportScript;
	
	private CommonModuleFrame projectFrame;
	
	public AnalysisAction(CommonModuleFrame projectFrame, QueryScript queryScript, PhonScript reportScript) {
		super();
		this.projectFrame = projectFrame;
		this.queryScript = queryScript;
		this.reportScript = reportScript;
	}
	
	public AnalysisAction(CommonModuleFrame projectFrame, String queryScript, String reportScript) {
		super();
		this.projectFrame = projectFrame;
		this.queryScript = loadQueryScript(queryScript);
		this.reportScript = loadReportScript(reportScript);
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final Project project = projectFrame.getExtension(Project.class);
		if(project == null) return;
		
		final QueryName queryName = queryScript.getExtension(QueryName.class);
		final String title = (queryName != null ? queryName.getName() : "");
		
		final QueryAnalysisWizard wizard = new QueryAnalysisWizard(title, project, 
				queryScript, reportScript);
		wizard.showWizard();
	}

	private QueryScript loadQueryScript(String queryScript) {
		return new QueryScript(getClass().getClassLoader().getResource(queryScript));
	}
	
	private PhonScript loadReportScript(String reportScript) {
		final StringBuffer buffer = new StringBuffer();
		try {
			final InputStream is = getClass().getClassLoader().getResourceAsStream(reportScript);
			if(is != null) {
				final InputStreamReader reader = new InputStreamReader(is, "UTF-8");
				final char[] buf = new char[1024];
				int read = -1;
				while((read = reader.read(buf)) > 0) {
					buffer.append(buf, 0, read);
				}
				reader.close();
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return new BasicScript(buffer.toString());
	}
	
}
