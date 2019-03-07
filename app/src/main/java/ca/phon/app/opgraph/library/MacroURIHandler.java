package ca.phon.app.opgraph.library;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import ca.phon.opgraph.library.NodeData;
import ca.phon.opgraph.library.handlers.URIHandler;
import ca.phon.opgraph.nodes.general.MacroNodeData;
import ca.phon.opgraph.nodes.general.MacroNodeInstantiator;

public class MacroURIHandler implements URIHandler<List<NodeData>> {

	@Override
	public List<NodeData> load(URI uri) throws IOException {
		String path = uri.getSchemeSpecificPart();
		String name = URLDecoder.decode(path, "UTF-8");
		name = FilenameUtils.getBaseName(name);
		
		MacroNodeData nodeData = new MacroNodeData(uri.toURL(), uri, name, "", "Macro", new MacroNodeInstantiator(), false);
		return List.of(nodeData);
	}

	@Override
	public boolean handlesURI(URI uri) {
		return "file".equals(uri.getScheme()) && uri.getSchemeSpecificPart() != null;
	}

}
