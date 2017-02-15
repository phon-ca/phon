package ca.phon.app.opgraph.nodes;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
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
		final MacroNode node = super.newInstance(params);
		final OpGraph graph = node.getGraph();
		
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
