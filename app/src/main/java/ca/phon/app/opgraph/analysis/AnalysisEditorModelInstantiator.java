/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.opgraph.analysis;

import java.util.ArrayList;

import javax.swing.JComponent;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.app.extensions.*;
import ca.gedge.opgraph.extensions.NodeMetadata;
import ca.gedge.opgraph.nodes.reflect.ObjectNode;
import ca.phon.app.opgraph.editor.EditorModelInstantiator;
import ca.phon.app.opgraph.editor.EditorModelInstantiator.EditorModelInstantiatorMenuInfo;
import ca.phon.plugin.*;
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
