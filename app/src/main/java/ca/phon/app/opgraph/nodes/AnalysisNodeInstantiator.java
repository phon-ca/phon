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

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.util.GraphUtils;
import ca.gedge.opgraph.library.NodeData;
import ca.gedge.opgraph.library.instantiators.Instantiator;
import ca.gedge.opgraph.nodes.general.MacroNode;

/**
 * Node {@link Instantiator} for analysis documents.  The {@link Instantiator}
 * will wrap the analysis document in a macro node and publish
 * fields for project, selectedSessions, and selectedParticipants.
 * 
 */
public class AnalysisNodeInstantiator extends MacroNodeInstantiator {
	
	@Override
	public MacroNode newInstance(Object... params) throws InstantiationException {
		MacroNode node = null;
		OpGraph graph = null;
		if(params.length == 1 && params[0] instanceof OpGraph) {
			graph = (OpGraph)params[0];
			GraphUtils.changeNodeIds(graph);
			node = new MacroNode(graph);
		} else {
			node = super.newInstance(params);
			graph = node.getGraph();
		}
		
		// find input nodes and publish fields
		final OpNode projectNode = graph.getNodesByName("Project").stream().findFirst().orElse(null);
		if(projectNode == null)
			throw new InstantiationException("Project node not found in analysis document");
		final OpNode sessionsNode = graph.getNodesByName("Selected Sessions").stream().findFirst().orElse(null);
		if(sessionsNode == null)
			throw new InstantiationException("Selected Sessions node not found in analysis document");
		final OpNode participantsNode = graph.getNodesByName("Selected Participants").stream().findFirst().orElse(null);
		if(participantsNode == null)
			throw new InstantiationException("Selected Participants node not found in analysis document");

		node.publish("project", projectNode, projectNode.getInputFieldWithKey("obj"));
		node.publish("selectedSessions", sessionsNode, sessionsNode.getInputFieldWithKey("obj"));
		node.publish("selectedParticipants", participantsNode, participantsNode.getInputFieldWithKey("obj"));
		
		return node;
	}
	
}
