package ca.phon.app.opgraph.nodes;

import java.net.URI;
import java.net.URL;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.library.NodeData;
import ca.gedge.opgraph.library.instantiators.Instantiator;

public class MacroNodeData extends NodeData {
	
	private URL analysisURL;

	public MacroNodeData(URL analysisURL, URI uri, String name, String description, String category,
			Instantiator<? extends OpNode> instantiator) {
		super(uri, name, description, category, instantiator);
		this.analysisURL = analysisURL;
	}

	public URL getAnalysisURL() {
		return this.analysisURL;
	}
	
	public void setAnalysisURL(URL url) {
		this.analysisURL = url;
	}
	
}
