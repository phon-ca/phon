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

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.editor.SimpleEditor;
import ca.phon.app.opgraph.nodes.ReportNodeInstantiator;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.project.Project;

public class OpenSimpleReportComposerAction extends HookableAction {
	
	private static final long serialVersionUID = 3781206121705628643L;

	private final static String TXT = "Report Composer...";
	
	private final OpGraph reportGraph;
	
	private final Project project;
	
	private final String queryId;
	
	public OpenSimpleReportComposerAction(Project project, String queryId) {
		this(project, queryId, null);
	}
	
	public OpenSimpleReportComposerAction(Project project, String queryId, OpGraph reportGraph) {
		super();
		
		putValue(NAME, TXT);
		
		this.project = project;
		this.queryId = queryId;
		this.reportGraph = reportGraph;
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final SimpleEditor frame =
				new SimpleEditor(project,
						new ReportLibrary(), new ReportEditorModelInstantiator(), new ReportNodeInstantiator(),
						(qs) -> new MacroNode(),
						(graph, project) -> new ReportRunner(graph, project, queryId) );
		frame.getEditor().setIncludeQueries(true);

		if(reportGraph != null) {
			frame.getEditor().addGraph(reportGraph);
		}
		
		frame.pack();
		frame.setSize(new Dimension(700, 500));
		frame.centerWindow();
		frame.setVisible(true);
	}

}
