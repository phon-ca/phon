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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.editor.SimpleEditor;
import ca.phon.app.opgraph.nodes.AnalysisNodeInstantiator;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.dag.CycleDetectedException;
import ca.phon.opgraph.dag.VertexNotFoundException;
import ca.phon.opgraph.exceptions.ItemMissingException;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;

public class OpenSimpleAnalysisComposerAction extends HookableAction {
	
	private static final long serialVersionUID = 3781206121705628643L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(OpenSimpleAnalysisComposerAction.class.getName());
	
	private final static String TXT = "Analysis Composer...";
	
	private final Project project;
	
	private final OpGraph analysisGraph;
	
	private SimpleEditor editor;
	
	public OpenSimpleAnalysisComposerAction(Project project) {
		this(project, null);
	}
	
	public OpenSimpleAnalysisComposerAction(Project project, OpGraph analysisGraph) {
		super();
		
		putValue(NAME, TXT);
		
		this.project = project;
		this.analysisGraph = analysisGraph;
	}
	
	public SimpleEditor getEditor() {
		return this.editor;
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		editor =
			new SimpleEditor(project,
					new AnalysisLibrary(), new AnalysisEditorModelInstantiator(), new AnalysisNodeInstantiator(),
					(qs, reportGraph) ->  {
						try {
							return AnalysisLibrary.analysisFromQuery(qs, reportGraph);
						} catch (IOException | IllegalArgumentException | ItemMissingException | VertexNotFoundException | CycleDetectedException | InstantiationException | URISyntaxException e) {
							LOGGER.error( e.getLocalizedMessage(), e);
							final MessageDialogProperties props = new MessageDialogProperties();
							props.setTitle("Composer (simple)");
							props.setHeader("Unable to create analysis from query");
							props.setMessage(e.getLocalizedMessage());
							props.setOptions(MessageDialogProperties.okOptions);
							props.setRunAsync(true);
							props.setParentWindow(CommonModuleFrame.getCurrentFrame());
							NativeDialogs.showMessageDialog(props);
						}
						return new MacroNode();
					} ,
					AnalysisRunner::new );
		editor.getEditor().setIncludeQueries(true);

		if(analysisGraph != null) {
			editor.getEditor().addGraph(analysisGraph);
		}
		
		editor.pack();
		editor.setSize(new Dimension(1024, 768));
		editor.centerWindow();
		editor.setVisible(true);
	}

}
