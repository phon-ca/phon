package ca.phon.app.opgraph.nodes.query;

import java.net.URI;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.library.NodeData;
import ca.gedge.opgraph.library.instantiators.Instantiator;
import ca.phon.query.script.QueryScript;

public class QueryNodeData extends NodeData {
	
	private QueryScript queryScript;

	public QueryNodeData(QueryScript queryScript, URI uri, String name, String description,
			String category, Instantiator<? extends OpNode> instantiator) {
		super(uri, name, description, category, instantiator);
		this.queryScript = queryScript;
	}
	
	public QueryScript getQueryScript() {
		return this.queryScript;
	}
	
	public void setQueryScript(QueryScript queryScript) {
		this.queryScript = queryScript;
	}

}
