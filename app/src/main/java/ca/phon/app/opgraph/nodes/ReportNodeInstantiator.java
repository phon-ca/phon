package ca.phon.app.opgraph.nodes;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.util.GraphUtils;
import ca.gedge.opgraph.nodes.general.MacroNode;

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
		
		node.publish("project", projectNode, projectNode.getInputFieldWithKey("obj"));
		node.publish("queryId", queryIdNode, queryIdNode.getInputFieldWithKey("obj"));
		
		return node;
	}

}
