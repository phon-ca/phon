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
package ca.phon.app.opgraph.report;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.gedge.opgraph.nodes.reflect.ObjectNode;
import ca.phon.app.opgraph.editor.EditorModelInstantiator;
import ca.phon.app.opgraph.editor.EditorModelInstantiator.EditorModelInstantiatorMenuInfo;
import ca.phon.app.opgraph.nodes.query.QueryHistoryNode;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.project.Project;

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
		
		final QueryHistoryNode historyNode = new QueryHistoryNode();
		graph.add(historyNode);
		
		try {
			final OpLink projectLink = 
					new OpLink(projectNode, projectNode.getOutputFieldWithKey("obj"), historyNode, historyNode.getInputFieldWithKey("project"));
			graph.add(projectLink);
			
			final OpLink idLink = 
					new OpLink(queryIdNode, queryIdNode.getOutputFieldWithKey("obj"), historyNode, historyNode.getInputFieldWithKey("queryId"));
			graph.add(idLink);
		} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException e) {
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

}
