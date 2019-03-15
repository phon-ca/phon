package ca.phon.app.query.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.LogUtil;
import ca.phon.app.opgraph.analysis.OpenSimpleAnalysisComposerAction;
import ca.phon.app.opgraph.editor.SimpleEditor;
import ca.phon.app.opgraph.editor.SimpleEditorPanel;
import ca.phon.app.query.QueryAndReportWizard;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.app.OpgraphIO;
import ca.phon.opgraph.app.commands.Hook;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.project.Project;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

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
			
			analysisComposer = openAnalysisComposerAct.getEditor();
		}
		
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
