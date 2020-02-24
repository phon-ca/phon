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
package ca.phon.app.opgraph.analysis;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.concurrent.Future;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.LogUtil;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.opgraph.editor.SimpleEditor;
import ca.phon.opgraph.OpGraph;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;

public class OpenSimpleAnalysisComposerAction extends HookableAction {
	
	private static final long serialVersionUID = 3781206121705628643L;

	private final static String TXT = "Analysis Composer...";
	
	private final Project project;
	
	private final OpGraph analysisGraph;
	
	private Future<SimpleEditor> futureEditor;
	
	public OpenSimpleAnalysisComposerAction(Project project) {
		this(project, null);
	}
	
	public OpenSimpleAnalysisComposerAction(Project project, OpGraph analysisGraph) {
		super();
		
		putValue(NAME, TXT);
		
		this.project = project;
		this.analysisGraph = analysisGraph;
	}
	
	public Future<SimpleEditor> getEditor() {
		return this.futureEditor;
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final EntryPointArgs args = new EntryPointArgs();
		args.put(EntryPointArgs.PROJECT_OBJECT, project);
		if(analysisGraph != null)
			args.put(AnalysisComposerEP.ANALYSIS_GRAPH, analysisGraph);
		
		try {
			AnalysisComposerEP ep = (AnalysisComposerEP)PluginEntryPointRunner.executePlugin(AnalysisComposerEP.EP_NAME, args);
			this.futureEditor = ep.getFutureEditor();
		} catch (PluginException e) {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.severe(e);
		}
	}

}
