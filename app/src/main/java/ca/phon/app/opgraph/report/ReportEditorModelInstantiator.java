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

import java.io.*;
import java.util.*;

import ca.phon.app.opgraph.editor.*;
import ca.phon.app.opgraph.editor.EditorModelInstantiator.*;
import ca.phon.app.opgraph.nodes.query.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.dag.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.opgraph.nodes.reflect.*;
import ca.phon.plugin.*;
import ca.phon.project.*;

@EditorModelInstantiatorMenuInfo(
		name="Query Report",
		tooltip="New query report...",
		modelType=ReportOpGraphEditorModel.class)
public class ReportEditorModelInstantiator implements EditorModelInstantiator, IPluginExtensionPoint<EditorModelInstantiator> {

	@Override
	public ReportOpGraphEditorModel createModel(OpGraph graph) {
		if(graph.getVertices().size() == 0) {
			setupGraph(graph);
		}
		return new ReportOpGraphEditorModel(graph);
	}
	
	private void setupGraph(OpGraph graph) {
		final ObjectNode projectNode = new ObjectNode(Project.class);
		projectNode.setName("Project");
		projectNode.setContextKey("_project");
		graph.add(projectNode);
		
		final ObjectNode queryIdNode = new ObjectNode(String.class);
		queryIdNode.setName("Query ID");
		queryIdNode.setContextKey("_queryId");
		graph.add(queryIdNode);
		
		final ObjectNode sessionListNode = new ObjectNode(ArrayList.class);
		sessionListNode.setContextKey("_selectedSessions");
		sessionListNode.setName("Selected Results");
		graph.add(sessionListNode);
		
		final QueryHistoryNode historyNode = new QueryHistoryNode();
		graph.add(historyNode);
		
		try {
			final OpLink projectLink = 
					new OpLink(projectNode, projectNode.getOutputFieldWithKey("obj"), historyNode, historyNode.getInputFieldWithKey("project"));
			graph.add(projectLink);
			
			final OpLink idLink = 
					new OpLink(queryIdNode, queryIdNode.getOutputFieldWithKey("obj"), historyNode, historyNode.getInputFieldWithKey("queryId"));
			graph.add(idLink);
			
			final OpLink sessionLink = 
					new OpLink(sessionListNode, sessionListNode.getOutputFieldWithKey("obj"), historyNode, historyNode.getInputFieldWithKey("selectedResults"));
			graph.add(sessionLink);
		} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException | InvalidEdgeException e) {
		}
	}

	@Override
	public Class<?> getExtensionType() {
		return EditorModelInstantiator.class;
	}

	@Override
	public IPluginExtensionFactory<EditorModelInstantiator> getFactory() {
		return (args) -> this;
	}

	@Override
	public OpGraph defaultTemplate() throws IOException {
		OpGraph graph = new OpGraph();
		setupGraph(graph);
		return graph;
	}

}
