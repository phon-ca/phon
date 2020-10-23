package ca.phon.app.query.actions;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.concurrent.*;

import ca.phon.app.hooks.*;
import ca.phon.app.log.*;
import ca.phon.app.opgraph.analysis.*;
import ca.phon.app.opgraph.editor.*;
import ca.phon.app.query.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.nodes.general.*;
import ca.phon.project.*;
import ca.phon.query.script.*;
import ca.phon.util.icons.*;
import ca.phon.worker.*;

public class SendToAnalysisComposer extends HookableAction {

	private QueryAndReportWizard wizard;
	
	private SimpleEditor analysisComposer;
	
	public SendToAnalysisComposer(QueryAndReportWizard wizard) {
		this(wizard, null);
	}
	
	public SendToAnalysisComposer(QueryAndReportWizard wizard, SimpleEditor analysisComposer) {
		super();
		
		this.wizard = wizard;
		this.analysisComposer = analysisComposer;
		
		putValue(HookableAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/share", IconSize.SMALL));
		
		if(analysisComposer == null) {
			putValue(HookableAction.NAME, "Send to New Analysis Composer");
		} else {
			putValue(HookableAction.NAME, "Send to " + analysisComposer.getTitle());
		}
		
		putValue(HookableAction.SHORT_DESCRIPTION, "Send query and report to analysis composer");
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		if(analysisComposer == null) {
			OpenSimpleAnalysisComposerAction openAnalysisComposerAct = new OpenSimpleAnalysisComposerAction(wizard.getExtension(Project.class));
			openAnalysisComposerAct.actionPerformed(ae);			
			
			PhonWorker worker = PhonWorker.createWorker();
			worker.setFinishWhenQueueEmpty(true);
			
			worker.invokeLater( () -> {
				try {
					var analysisComposer = openAnalysisComposerAct.getEditor().get();
					sendScriptToEditor(analysisComposer);
				} catch (InterruptedException | ExecutionException e) {
					LogUtil.severe(e);
				}
			});
			worker.start();
		} else {
			sendScriptToEditor(analysisComposer);
		}
	}

	public void sendScriptToEditor(SimpleEditor analysisComposer) {		
		QueryScript qs = (QueryScript)wizard.getQueryScript().clone();
		try {
			OpGraph reportGraph = OpgraphIO.roundtrip(wizard.getReportComposer().getGraph());
			MacroNode node = analysisComposer.getEditor().addQuery(qs, reportGraph);
			
			String queryName = wizard.getQueryHistoryPanel().getQueryName();
			if(node != null && queryName != null && queryName.length() > 0) {
				node.setName(queryName);
			}
		} catch (IOException e) {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.severe(e);
		}
	}
}
