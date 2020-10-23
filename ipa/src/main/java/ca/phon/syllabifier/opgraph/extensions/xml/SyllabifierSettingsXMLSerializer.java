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
package ca.phon.syllabifier.opgraph.extensions.xml;

import java.io.*;

import javax.xml.namespace.*;

import org.w3c.dom.*;

import ca.phon.opgraph.*;
import ca.phon.opgraph.io.xml.XMLSerializer;
import ca.phon.opgraph.io.xml.XMLSerializerFactory;
import ca.phon.syllabifier.opgraph.extensions.*;
import ca.phon.util.*;
import ca.phon.xml.*;

public class SyllabifierSettingsXMLSerializer implements XMLSerializer {
	
	static final String NAMESPACE = "https://www.phon.ca/ns/syllabifier";
	static final String PREFIX = "ops";
	
	static final QName SETTINGS_QNAME = new QName(NAMESPACE, "settings", PREFIX);
	
	@Override
	public void write(XMLSerializerFactory serializerFactory, Document doc,
			Element parentElem, Object obj) throws IOException {
		if(obj == null)
			throw new IOException("Null object given to serializer");
		
		if(!(obj instanceof SyllabifierSettings))
			throw new IOException(SyllabifierSettingsXMLSerializer.class.getName() + " cannot write objects of type " + obj.getClass().getName());
		
		// setup namespace for document
		final Element rootEle = doc.getDocumentElement();
		rootEle.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, 
				XMLConstants.XMLNS_ATTRIBUTE + ":" + PREFIX, NAMESPACE);
		
		// Create metadata element
		final SyllabifierSettings meta = (SyllabifierSettings)obj;
		final Element metaElem = doc.createElementNS(NAMESPACE, PREFIX + ":settings");
		metaElem.setAttribute("name", meta.getName());
		metaElem.setAttribute("lang", meta.getLanguage().toString());
		
		parentElem.appendChild(metaElem);
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory,
			OpGraph graph, Object parent, Document doc, Element elem)
			throws IOException {
		if(graph != parent) {
			throw new IOException("SyllabifierSettings can only exist once per graph.");
		}
		if(SETTINGS_QNAME.equals(XMLSerializerFactory.getQName(elem))) {
			final SyllabifierSettings settings = new SyllabifierSettings();

			final String name = elem.getAttribute("name");
			settings.setName(name);
			
			final String lang = elem.getAttribute("lang");
			settings.setLanguage(Language.parseLanguage(lang));
			
			graph.putExtension(SyllabifierSettings.class, settings);
		}
		
		return null;
	}

	@Override
	public boolean handles(Class<?> cls) {
		return (cls == SyllabifierSettings.class);
	}

	@Override
	public boolean handles(QName name) {
		return SETTINGS_QNAME.equals(name);
	}

}
