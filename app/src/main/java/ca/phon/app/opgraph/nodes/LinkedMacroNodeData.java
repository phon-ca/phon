package ca.phon.app.opgraph.nodes;

import java.net.URI;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.library.NodeData;
import ca.gedge.opgraph.library.instantiators.Instantiator;

public class LinkedMacroNodeData extends NodeData {

	private URI graphURI;
	
	public LinkedMacroNodeData(URI uri, String name, String description, String category,
			URI graphURI, Instantiator<? extends OpNode> instantiator) {
		super(uri, name, description, category, instantiator);
		
		this.graphURI = graphURI;
	}
	
	public URI getGraphURI() {
		return this.graphURI;
	}
	
	public void setGraphURI(URI uri) {
		this.graphURI = uri;
	}

}
