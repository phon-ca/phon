/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.editor;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.io.xml.XMLSerializer;
import ca.phon.opgraph.io.xml.XMLSerializerFactory;
import ca.phon.xml.XMLConstants;

public class NodeEditorSettingsSerializer implements XMLSerializer {

	static final String NAMESPACE = "https://www.phon.ca/ns/node_editor";
	static final String PREFIX = "nes";
	
	static final QName SETTINGS_QNAME = new QName(NAMESPACE, "settings", PREFIX);
	
	@Override
	public void write(XMLSerializerFactory serializerFactory, Document doc, Element parentElem, Object obj)
			throws IOException {
		if(obj == null)
			throw new IOException("Null object given to serializer");
		
		if(!(obj instanceof NodeEditorSettings))
			throw new IOException(NodeEditorSettingsSerializer.class.getName() + " cannot write objects of type " + obj.getClass().getName());
		
		// setup namespace for document
		final Element rootEle = doc.getDocumentElement();
		rootEle.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, 
				XMLConstants.XMLNS_ATTRIBUTE + ":" + PREFIX, NAMESPACE);
		
		// Create metadata element
		final NodeEditorSettings meta = (NodeEditorSettings)obj;
		final Element metaElem = doc.createElementNS(NAMESPACE, PREFIX + ":settings");
		metaElem.setAttribute("type", meta.getModelType());
		
		if(meta.isGenerated()) {
			metaElem.setAttribute("generated", Boolean.toString(meta.isGenerated()));
		}
		
		parentElem.appendChild(metaElem);
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory, OpGraph graph, Object parent, Document doc, Element elem)
			throws IOException {
		if(graph != parent) {
			throw new IOException("SyllabifierSettings can only exist once per graph.");
		}
		if(SETTINGS_QNAME.equals(XMLSerializerFactory.getQName(elem))) {
			final NodeEditorSettings settings = new NodeEditorSettings();

			final String name = elem.getAttribute("type");
			settings.setModelType(name);
			
			if(elem.hasAttribute("generated")) {
				settings.setGenerated(Boolean.parseBoolean(elem.getAttribute("generated")));
			}
			
			graph.putExtension(NodeEditorSettings.class, settings);
		}
		
		return null;
	}

	@Override
	public boolean handles(Class<?> cls) {
		return (cls == NodeEditorSettings.class);
	}

	@Override
	public boolean handles(QName name) {
		return SETTINGS_QNAME.equals(name);
	}

}
