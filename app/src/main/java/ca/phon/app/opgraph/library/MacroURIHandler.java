/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
