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
package ca.phon.app.query;

import ca.phon.app.actions.XMLOpenHandler;
import ca.phon.plugin.*;

import javax.xml.stream.events.StartElement;
import java.io.*;
import java.util.*;

@PhonPlugin(name="Open")
public class QueryXMLFileOpenHandler implements XMLOpenHandler, IPluginExtensionPoint<XMLOpenHandler> {

	@Override
	public Class<?> getExtensionType() {
		return XMLOpenHandler.class;
	}

	@Override
	public IPluginExtensionFactory<XMLOpenHandler> getFactory() {
		return (args) -> this;
	}

	@Override
	public Set<String> supportedExtensions() {
		return Set.of("xml");
	}

	@Override
	public boolean canRead(StartElement startEle) {
		return (startEle.getName().getNamespaceURI().equals("http://phon.ling.mun.ca/ns/query")
					 && startEle.getName().getLocalPart().equals("query"));
	}

	@Override
	public void openXMLFile(File file, Map<String, Object> args) throws IOException {
		
	}

}
