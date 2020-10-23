package ca.phon.app.opgraph.library;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.io.*;

import ca.phon.opgraph.library.*;
import ca.phon.opgraph.library.handlers.*;
import ca.phon.opgraph.nodes.general.*;

/**
 * Load macros from classpath (used in {@link PhonNodeLibrary})
 *
 */
public class ClasspathMacroURIHandler implements URIHandler<List<NodeData>> {

	@Override
	public boolean handlesURI(URI uri) {
		return "classpath".equals(uri.getScheme()) && ClassLoader.getSystemResource(uri.getSchemeSpecificPart()) != null;
	}

	@Override
	public List<NodeData> load(URI uri) throws IOException {
		String path = uri.getSchemeSpecificPart();
		Enumeration<URL> resourceURLs = ClassLoader.getSystemResources(path);
		
		List<NodeData> retVal = new ArrayList<>();
		
		while(resourceURLs.hasMoreElements()) {
			URL url = resourceURLs.nextElement();
			String name = URLDecoder.decode(url.getPath(), "UTF-8");
			name = FilenameUtils.getBaseName(name);
			
			MacroNodeData nodeData = new MacroNodeData(url, uri, name, "", "Macro", new MacroNodeInstantiator(), false);
			retVal.add(nodeData);
		}
		
		return retVal;
	}

}
