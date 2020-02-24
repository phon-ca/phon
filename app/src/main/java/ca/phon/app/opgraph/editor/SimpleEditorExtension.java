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
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.io.xml.XMLSerializer;
import ca.phon.opgraph.io.xml.XMLSerializerFactory;
import ca.phon.opgraph.nodes.general.MacroNode;

/**
 * Extension for {@link OpGraph}s created using the {@link SimpleEditorPanel}.
 * Retains a map of names -> {@link MacroNode}s used to re-open a document
 * created by the {@link SimpleEditorPanel} and allow continued editing.
 *
 */
public class SimpleEditorExtension implements XMLSerializer {
	
	static final String NAMESPACE = "https://phon.ca/ns/opgraph_query";
	static final String PREFIX = "opqry";
	
	static final QName QNAME = new QName(NAMESPACE, "simplecomposer", PREFIX);

	private final List<MacroNode> macroNodes;
	
	public SimpleEditorExtension() {
		this(new ArrayList<>());
	}
	
	public SimpleEditorExtension(List<MacroNode> macroNodes) {
		this.macroNodes = macroNodes;
	}
	
	public List<MacroNode> getMacroNodes() {
		return macroNodes;
	}

	@Override
	public void write(XMLSerializerFactory serializerFactory, Document doc, Element parentElem, Object obj)
			throws IOException {
		if(obj == null)
			throw new IOException("Null object given to serializer");
		
		if(!(obj instanceof SimpleEditorExtension))
			throw new IOException(getClass().getName() + " cannot write objects of type " + obj.getClass());
		
		final Element rootEle = doc.getDocumentElement();
		rootEle.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				XMLConstants.XMLNS_ATTRIBUTE + ":" + PREFIX, NAMESPACE);
		
		final Element settingsEle =
				doc.createElementNS(NAMESPACE, PREFIX + ":" + QNAME.getLocalPart());
		parentElem.appendChild(settingsEle);
		
		SimpleEditorExtension ext = (SimpleEditorExtension)obj;
		for(MacroNode mn:ext.getMacroNodes()) {
			final Element mnEle = doc.createElementNS(NAMESPACE, PREFIX + ":node");
			mnEle.setAttribute("ref", mn.getId());
			settingsEle.appendChild(mnEle);
		}
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory, OpGraph graph, Object parent, Document doc, Element elem)
			throws IOException {
		final List<MacroNode> macroNodes = new ArrayList<>();
		
		final NodeList childNodes = elem.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if(childNode.getNodeName().equals(PREFIX + ":node")) {
				final String nodeId = childNode.getAttributes().getNamedItem("ref").getNodeValue();
				final OpNode graphNode = graph.getNodeById(nodeId, false);
				if(graphNode != null && graphNode instanceof MacroNode) {
					macroNodes.add((MacroNode)graphNode);
				}
			}
		}
		
		final SimpleEditorExtension retVal = new SimpleEditorExtension(macroNodes);
		graph.putExtension(SimpleEditorExtension.class, retVal);
		
		return retVal;
	}

	@Override
	public boolean handles(Class<?> cls) {
		return cls == SimpleEditorExtension.class;
	}

	@Override
	public boolean handles(QName name) {
		return QNAME.equals(name);
	}
	
}
