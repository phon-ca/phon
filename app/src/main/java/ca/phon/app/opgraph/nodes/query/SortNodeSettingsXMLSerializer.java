/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.opgraph.nodes.query;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.extensions.Extendable;
import ca.gedge.opgraph.io.xml.XMLSerializer;
import ca.gedge.opgraph.io.xml.XMLSerializerFactory;
import ca.phon.app.opgraph.nodes.query.SortNodeSettings.SortColumn;
import ca.phon.app.opgraph.nodes.query.SortNodeSettings.SortOrder;
import ca.phon.app.opgraph.nodes.query.SortNodeSettings.SortType;

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
