package ca.phon.app.opgraph.editor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

import ca.gedge.opgraph.OpGraph;
import ca.phon.app.opgraph.nodes.query.QueryNode;
import ca.phon.app.opgraph.nodes.query.QueryNodeData;
import ca.phon.app.opgraph.nodes.query.QueryNodeInstantiator;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptLibrary;

@OpgraphEditorModelInfo(name="General", description="Empty graph with default context")
public class DefaultOpgraphEditorModel extends OpgraphEditorModel {

	public DefaultOpgraphEditorModel() {
		this(new OpGraph());
	}

	public DefaultOpgraphEditorModel(OpGraph opgraph) {
		super(opgraph);
		
		addQueryNodes();
	}

	private void addQueryNodes() {
		Consumer<QueryScript> addToLibrary = (QueryScript script) -> {
			final QueryName qn = script.getExtension(QueryName.class);
			final String name = (qn != null ? qn.getName() : "<unknown>");
			try {
				final URI queryNodeClassURI = new URI("class", QueryNode.class.getName(), qn.getName());
				final QueryNodeInstantiator instantiator = new QueryNodeInstantiator();
				
				final String description = 
						"Add " + qn.getName() + " query to graph.";
				
				final QueryNodeData nodeData = new QueryNodeData(script, queryNodeClassURI,
						name, description, "Query", instantiator);
				getNodeLibrary().getLibrary().put(nodeData);
			} catch (URISyntaxException e) {
				
			}
		};
		
		final QueryScriptLibrary library = new QueryScriptLibrary();
		library.stockScriptFiles().forEach(addToLibrary);
		library.userScriptFiles().forEach(addToLibrary);
	}
	
}
