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

import java.util.ArrayList;

import javax.swing.JComponent;

import ca.phon.app.opgraph.editor.EditorModelInstantiator;
import ca.phon.app.opgraph.editor.EditorModelInstantiator.EditorModelInstantiatorMenuInfo;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.app.extensions.Note;
import ca.phon.opgraph.app.extensions.Notes;
import ca.phon.opgraph.extensions.NodeMetadata;
import ca.phon.opgraph.nodes.reflect.ObjectNode;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.project.Project;

@EditorModelInstantiatorMenuInfo(
		name="Analysis",
		tooltip = "New analysis document...",
		modelType = AnalysisOpGraphEditorModel.class)
public class AnalysisEditorModelInstantiator implements EditorModelInstantiator, IPluginExtensionPoint<EditorModelInstantiator> {

	@Override
	public AnalysisOpGraphEditorModel createModel(OpGraph graph) {
		if(graph.getVertices().size() == 0) {
			setupDefaultGraph(graph);
		}
		return new AnalysisOpGraphEditorModel(graph);
	}
	
	private void setupDefaultGraph(OpGraph graph) {
		final ObjectNode projectNode = new ObjectNode(Project.class);
		projectNode.setContextKey("_project");
		final NodeMetadata projectMeta = new NodeMetadata();
		projectMeta.setLocation(51, 52);
		projectNode.putExtension(NodeMetadata.class, projectMeta);
		graph.add(projectNode);
		
		
		final ObjectNode sessionListNode = new ObjectNode(ArrayList.class);
		sessionListNode.setContextKey("_selectedSessions");
		sessionListNode.setName("Selected Sessions");
		final NodeMetadata sessionMeta = new NodeMetadata();
		sessionMeta.setLocation(127, 298);
		sessionListNode.putExtension(NodeMetadata.class, sessionMeta);
		graph.add(sessionListNode);
		
		final ObjectNode participantListNode = new ObjectNode(ArrayList.class);
		participantListNode.setContextKey("_selectedParticipants");
		participantListNode.setName("Selected Participants");
		final NodeMetadata partMeta = new NodeMetadata();
		partMeta.setLocation(146, 418);
		participantListNode.putExtension(NodeMetadata.class, partMeta);
		graph.add(participantListNode);
		
		final Notes notes = new Notes();
		final Note note = new Note("Analysis Inputs", "");
		final JComponent noteComp = note.getExtension(JComponent.class);
		noteComp.setBounds(20, 10, 330, 540);
		
		notes.add(note);
	}

	@Override
	public Class<?> getExtensionType() {
		return EditorModelInstantiator.class;
	}

	@Override
	public IPluginExtensionFactory<EditorModelInstantiator> getFactory() {
		return (Object... args) -> this;
	}

}