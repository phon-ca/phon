package ca.phon.app.opgraph.library;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.io.*;

import ca.phon.opgraph.library.*;
import ca.phon.opgraph.library.handlers.*;
import ca.phon.opgraph.nodes.general.*;

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
