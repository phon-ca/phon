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
package ca.phon.app.query.actions;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.LogUtil;
import ca.phon.app.opgraph.analysis.OpenSimpleAnalysisComposerAction;
import ca.phon.app.opgraph.editor.SimpleEditor;
import ca.phon.app.query.QueryAndReportWizard;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.app.OpgraphIO;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.project.Project;
import ca.phon.query.script.QueryScript;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonWorker;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

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
