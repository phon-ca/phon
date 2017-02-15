/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.util.logging.Level;
import java.util.logging.Logger;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.gedge.opgraph.nodes.reflect.ObjectNode;
import ca.phon.app.opgraph.editor.NewDialogPanel;
import ca.phon.app.opgraph.editor.OpgraphEditorModel;
import ca.phon.app.opgraph.nodes.query.QueryHistoryNode;
import ca.phon.project.Project;

public class ReportNewPanel extends NewDialogPanel {

	private final static Logger LOGGER = Logger.getLogger(ReportNewPanel.class.getName());
	
	private static final long serialVersionUID = 3486960052647071379L;

	@Override
	public String getTitle() {
		return "Report";
	}

	@Override
	public OpgraphEditorModel createModel() {
		final OpGraph graph = new OpGraph();
		
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
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		return new ReportOpGraphEditorModel(graph);
	}

}
