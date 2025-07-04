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
package ca.phon.app.opgraph.report;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.opgraph.editor.SimpleEditor;
import ca.phon.app.opgraph.nodes.ReportNodeInstantiator;
import ca.phon.app.query.OpenQuerySelector;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.plugin.*;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Map;

@PhonPlugin(name=ReportComposerEP.EP_NAME)
public class ReportComposerEP implements IPluginEntryPoint {

	public static final String EP_NAME = "Report Composer";
	
	public final static String REPORT_GRAPH = "graph";
	
	public final static String REPORT_FILE = "file";
	
	public final static String QUERY_ID = "queryId";

	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		final EntryPointArgs epArgs = new EntryPointArgs(args);
		
		final Project project = epArgs.getProject();
		final OpGraph graph = epArgs.containsKey(REPORT_GRAPH) ? (OpGraph)epArgs.get(REPORT_GRAPH) : new OpGraph();
		final String queryId = epArgs.containsKey(QUERY_ID) ? epArgs.get(QUERY_ID).toString() : null;
		final File file = (File)epArgs.get(REPORT_FILE);
		Runnable onEDT = () -> {
			final SimpleEditor editor =
				new SimpleEditor(project, new ReportLibrary(), graph,
						new ReportEditorModelInstantiator(), new ReportNodeInstantiator(),
						(qs, reportGraph) -> new MacroNode(),
						(reportGraph, proj) -> new ReportRunner(reportGraph, proj, (queryId != null ? queryId : selectOpenQueryID(proj)) ) );
			
			if(file != null)
				editor.setCurrentFile(file);
			
			editor.getEditor().setIncludeQueries(true);			
			editor.pack();
			editor.setSize(new Dimension(1024, 768));
			editor.centerWindow();
			editor.setVisible(true);
		};
		
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else
			SwingUtilities.invokeLater(onEDT);
	}
	
	private String selectOpenQueryID(Project project) {
		final JDialog selectQueryDialog = new JDialog(CommonModuleFrame.getCurrentFrame(), "Select Query");
		selectQueryDialog.setLayout(new BorderLayout());
		
		DialogHeader header = new DialogHeader("Select Query", "Select an open query to be used for the 'Run Report' action");
		selectQueryDialog.add(header, BorderLayout.NORTH);
		
		OpenQuerySelector selector = new OpenQuerySelector(project);
		selectQueryDialog.add(selector, BorderLayout.CENTER);
		
		JButton okBtn = new JButton("Ok");
		okBtn.addActionListener( (e) -> selectQueryDialog.setVisible(false) );
		
		selectQueryDialog.setModal(true);
		selectQueryDialog.pack();
		selectQueryDialog.setLocationByPlatform(true);
		selectQueryDialog.setVisible(true);
		
		var selectedQueries = selector.getSelectedQueries();
		if(selectedQueries.size() > 0) {
			return selectedQueries.get(0).getQuery().getUUID().toString();
		} else {
			return null;
		}
	}
	
}
