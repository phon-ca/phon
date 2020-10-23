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
package ca.phon.app.opgraph.nodes.table;

import java.io.*;

import javax.xml.namespace.*;

import org.w3c.dom.*;

import ca.phon.app.opgraph.nodes.table.SortNodeSettings.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.extensions.*;
import ca.phon.opgraph.io.xml.XMLSerializer;
import ca.phon.opgraph.io.xml.XMLSerializerFactory;
import ca.phon.xml.*;

public class SortNodeSettingsXMLSerializer implements XMLSerializer {
	
	static final String NAMESPACE = "https://phon.ca/ns/opgraph_query";
	static final String PREFIX = "opqry";
	
	static final QName QNAME = new QName(NAMESPACE, "sortoptions", PREFIX);

	@Override
	public void write(XMLSerializerFactory serializerFactory, Document doc, Element parentElem, Object obj)
			throws IOException {
		if(obj == null)
			throw new IOException("Null object given to serializer");
		
		if(!(obj instanceof SortNodeSettings))
			throw new IOException(getClass().getName() + " cannot write objects of type " + obj.getClass());
		
		final Element rootEle = doc.getDocumentElement();
		rootEle.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				XMLConstants.XMLNS_ATTRIBUTE + ":" + PREFIX, NAMESPACE);
		
		final SortNodeSettings settings = (SortNodeSettings)obj;
		final Element settingsEle = 
				doc.createElementNS(NAMESPACE, PREFIX + ":" + QNAME.getLocalPart());
		
		settingsEle.setAttribute("configureAutomatically", Boolean.toString(settings.isConfigureAutomatically()));
		settingsEle.setAttribute("autoSortOrder", settings.getAutoSortOrder().toString().toLowerCase());
		if(settings.isLikeOnTop()) {
			settingsEle.setAttribute("likeOnTop", Boolean.toString(settings.isLikeOnTop()));
		}
		
		for(SortColumn sc:settings.getSorting()) {
			final Element scEle = doc.createElementNS(NAMESPACE, PREFIX + ":sortBy");
			writeSortColumn(doc, scEle, sc);
			settingsEle.appendChild(scEle);
		}
		parentElem.appendChild(settingsEle);
	}
	
	private void writeSortColumn(Document doc, Element scEle, SortColumn sc) {
		scEle.setAttribute("column", sc.getColumn());
		scEle.setAttribute("type", sc.getType().toString().toLowerCase());
		scEle.setAttribute("order", sc.getOrder().toString().toLowerCase());
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory, OpGraph graph, Object parent, Document doc, Element elem)
			throws IOException {
		if(!QNAME.equals(XMLSerializerFactory.getQName(elem)))
			throw new IOException("Incorrect element");
		
		final SortNodeSettings retVal = new SortNodeSettings();
		retVal.getSorting().clear();
		
		NamedNodeMap attrs = elem.getAttributes();
		Node autoConfigureNode = attrs.getNamedItem("configureAutomatically");
		if(autoConfigureNode != null) {
			retVal.setConfigureAutomatically(Boolean.parseBoolean(autoConfigureNode.getNodeValue()));
		} else {
			retVal.setConfigureAutomatically(false);
		}
		
		Node autoSortOrderNode = attrs.getNamedItem("autoSortOrder");
		if(autoSortOrderNode != null) {
			retVal.setAutoSortOrder(SortOrder.fromString(autoSortOrderNode.getNodeValue()));
		}
		
		Node likeOnTopNode = attrs.getNamedItem("likeOnTop");
		if(likeOnTopNode != null) {
			retVal.setLikeOnTop(Boolean.parseBoolean(likeOnTopNode.getNodeValue()));
		}
		
		NodeList childNodes = elem.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			final Node childNode = childNodes.item(i);
			if(childNode.getNamespaceURI() != null && childNode.getNamespaceURI().equals(NAMESPACE)
					&& childNode.getLocalName().equals("sortBy")) {
				final SortColumn sortBy = readSortColumn(childNode);
				retVal.getSorting().add(sortBy);
			}
		}
		
		// attach to parent object
		if(parent instanceof Extendable) {
			((Extendable)parent).putExtension(SortNodeSettings.class, retVal);
		}
		
		
		return retVal;
	}
	
	private SortColumn readSortColumn(Node sortColumnNode) {
		SortColumn retVal = new SortColumn();
		final NamedNodeMap attrs = sortColumnNode.getAttributes();
		retVal.setColumn(attrs.getNamedItem("column").getNodeValue());
		
		final String type = 
				attrs.getNamedItem("type").getNodeValue();
		final SortType sortType = SortType.valueOf(SortType.class, type.toUpperCase());
		retVal.setType(sortType);
		
		if(attrs.getNamedItem("order") != null) {
			final SortOrder order = 
					SortOrder.fromString(attrs.getNamedItem("order").getNodeValue());
			retVal.setOrder(order);
		}
		
		return retVal;
	}

	@Override
	public boolean handles(Class<?> cls) {
		return (cls == SortNodeSettings.class);
	}

	@Override
	public boolean handles(QName name) {
		return QNAME.equals(name);
	}

}
