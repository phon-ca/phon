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
package ca.phon.app.opgraph.nodes;

import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.app.util.GraphUtils;
import ca.phon.opgraph.nodes.general.MacroNode;

/**
 * Add Query Reports as {@link MacroNode}s in the graph.  Fields for project
 * and queryId will be published.
 * 
 */
public class ReportNodeInstantiator extends MacroNodeInstantiator {

	@Override
	public MacroNode newInstance(Object... params) throws InstantiationException {
		final MacroNode node = super.newInstance(params);
		final OpGraph graph = node.getGraph();
		// update graph ids to make them unique
		GraphUtils.changeNodeIds(graph);
		
		final OpNode projectNode = graph.getNodesByName("Project").stream().findFirst().orElse(null);
		if(projectNode == null)
			throw new InstantiationException("Project node not found in report document");
		
		final OpNode queryIdNode = graph.getNodesByName("Query ID").stream().findFirst().orElse(null);
		if(queryIdNode == null)
			throw new InstantiationException("Query ID node not found in report document");
		
		final OpNode selectedResultsNode = graph.getNodesByName("Selected Results").stream().findFirst().orElse(null);
		if(selectedResultsNode == null)
			throw new InstantiationException("Selected Results node not found in report document");
		
		node.publish("project", projectNode, projectNode.getInputFieldWithKey("obj"));
		node.publish("queryId", queryIdNode, queryIdNode.getInputFieldWithKey("obj"));
		node.publish("selectedResults", selectedResultsNode, selectedResultsNode.getInputFieldWithKey("obj"));
		
		return node;
	}

}
