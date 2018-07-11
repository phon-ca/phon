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
package ca.phon.app.opgraph.nodes.table;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.phon.app.opgraph.nodes.table.InventorySettings.ColumnInfo;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.io.xml.XMLSerializer;
import ca.phon.opgraph.io.xml.XMLSerializerFactory;

public class InventorySettingsXMLSerializer implements XMLSerializer {
	
	static final String NAMESPACE = "https://phon.ca/ns/opgraph_query";
	static final String PREFIX = "opqry";
	
	static final QName QNAME = new QName(NAMESPACE, "inventoryoptions", PREFIX);

	@Override
	public void write(XMLSerializerFactory serializerFactory, Document doc, Element parentElem, Object obj)
			throws IOException {
		if(obj == null)
			throw new IOException("Null object given to serializer");
		
		if(!(obj instanceof InventorySettings))
			throw new IOException(getClass().getName() + " cannot write objects of type " + obj.getClass());
		
		final Element rootEle = doc.getDocumentElement();
		rootEle.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				XMLConstants.XMLNS_ATTRIBUTE + ":" + PREFIX, NAMESPACE);
		
		final InventorySettings settings = (InventorySettings)obj;
		final Element settingsEle =
				doc.createElementNS(NAMESPACE, PREFIX + ":" + QNAME.getLocalPart());
		
		// auto config
		final Element autoConfigSettings = doc.createElementNS(NAMESPACE, PREFIX + ":automaticConfiguration");
		autoConfigSettings.setAttribute("configureAutomatically", Boolean.toString(settings.isConfigureAutomatically()));
		
		final Element autoGroupBySettings = doc.createElementNS(NAMESPACE, PREFIX + ":autoGrouping");
		autoGroupBySettings.setAttribute("grouping", Boolean.toString(settings.isAutoGrouping()));
		autoGroupBySettings.setAttribute("column", settings.getAutoGroupingColumn());
		autoConfigSettings.appendChild(autoGroupBySettings);
		
		final Element autoColumnSettings = doc.createElementNS(NAMESPACE, PREFIX + ":autoColumns");
		autoColumnSettings.setAttribute("ignoreDiacritics", Boolean.toString(settings.isIgnoreDiacritics()));
		autoColumnSettings.setAttribute("caseSensitive", Boolean.toString(settings.isCaseSensitive()));
		autoColumnSettings.setAttribute("includeAdditionalGroupData", Boolean.toString(settings.isIncludeAdditionalGroupData()));
		autoColumnSettings.setAttribute("includeAdditionalWordData", Boolean.toString(settings.isIncludeAdditionalWordData()));
		autoColumnSettings.setAttribute("includeMetadata", Boolean.toString(settings.isIncludeMetadata()));
		autoConfigSettings.appendChild(autoColumnSettings);
		
		settingsEle.appendChild(autoConfigSettings);
		
		if(settings.getGroupBy() != null
				&& settings.getGroupBy().getName().trim().length() > 0) {
			final Element groupByEle = 
					doc.createElementNS(NAMESPACE, PREFIX + ":groupBy");
			writeColumnInfo(doc, groupByEle, settings.getGroupBy());
			settingsEle.appendChild(groupByEle);
		}
		for(ColumnInfo info:settings.getColumns()) {
			if(info.getName().trim().length() == 0) continue;
			final Element colele =
					doc.createElementNS(NAMESPACE, PREFIX + ":column");
			writeColumnInfo(doc, colele, info);
			settingsEle.appendChild(colele);
		}
		parentElem.appendChild(settingsEle);
	}
	
	private void writeColumnInfo(Document doc, Element ele, ColumnInfo info) {
		ele.setAttribute("column", info.getName());
		ele.setAttribute("caseSensitive", 
				Boolean.toString(info.isCaseSensitive()));
		ele.setAttribute("ignoreDiacritics", 
				Boolean.toString(info.isIgnoreDiacritics()));
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory, OpGraph graph, Object parent, Document doc, Element elem)
			throws IOException {
		if(!QNAME.equals(XMLSerializerFactory.getQName(elem)))
			throw new IOException("Incorrect element");
		
		final InventorySettings retVal = new InventorySettings();
		retVal.getColumns().clear();
		NodeList childNodes = elem.getChildNodes();
		boolean hasAutoConfig = false;
		for(int i = 0; i < childNodes.getLength(); i++) {
			final Node childNode = childNodes.item(i);
			if(childNode.getNodeName().equals(PREFIX + ":groupBy")) {
				retVal.setGroupBy(readColumnInfo(childNode));
			} else if(childNode.getNodeName().equals(PREFIX + ":column")) {
				retVal.getColumns().add(readColumnInfo(childNode));
			} else if(childNode.getNodeName().equals(PREFIX + ":automaticConfiguration")) {
				hasAutoConfig = true;
				readAutoConfigSettings(retVal, childNode);
			}
		}
		if(!hasAutoConfig) {
			retVal.setConfigureAutomatically(false);
		}
		
		if(parent instanceof OpNode) {
			final OpNode node = (OpNode)parent;
			node.putExtension(InventorySettings.class, retVal);
		}
		
		return retVal;
	}
	
	private boolean checkBooleanAttr(NamedNodeMap attrs, String name) {
		Node n = attrs.getNamedItem(name);
		return (n != null ? Boolean.parseBoolean(n.getNodeValue()) : false);
	}
	
	private void readAutoConfigSettings(InventorySettings settings, Node autoConfigNode) {
		settings.setConfigureAutomatically(checkBooleanAttr(autoConfigNode.getAttributes(), "configureAutomatically"));
		
		NodeList childNodes = autoConfigNode.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			NamedNodeMap attrs = childNode.getAttributes();
			if(childNode.getNodeName().equals(PREFIX + ":autoGrouping")) {
				settings.setAutoGrouping(checkBooleanAttr(attrs, "grouping"));
				
				Node n = attrs.getNamedItem("column");
				settings.setAutoGroupingColumn(n != null ? n.getNodeValue() : "");
			} else if(childNode.getNodeName().equals(PREFIX + ":autoColumns")) {
				settings.setIgnoreDiacritics(checkBooleanAttr(attrs, "ignoreDiacritics"));
				settings.setCaseSensitive(checkBooleanAttr(attrs, "caseSensitive"));
				settings.setIncludeAdditionalGroupData(checkBooleanAttr(attrs, "includeAdditionalGroupData"));
				settings.setIncludeAdditionalWordData(checkBooleanAttr(attrs, "includeAdditionalWordData"));
				settings.setIncludeMetadata(checkBooleanAttr(attrs, "includeMetadata"));
			}
		}
	}
	
	private ColumnInfo readColumnInfo(Node columnInfoNode) {
		final ColumnInfo retVal = new ColumnInfo();
		final NamedNodeMap attrs = columnInfoNode.getAttributes();
		
		final org.w3c.dom.Node nameNode = attrs.getNamedItem("column");
		if(nameNode != null)
			retVal.setName(nameNode.getNodeValue());
		
		final Node csNode = attrs.getNamedItem("caseSensitive");
		if(csNode != null)
			retVal.setCaseSensitive(Boolean.parseBoolean(csNode.getNodeValue()));

		final Node idNode = attrs.getNamedItem("ignoreDiacritics");
		if(idNode != null)
			retVal.setIgnoreDiacritics(Boolean.parseBoolean(idNode.getNodeValue()));
		
		return retVal;
	}

	@Override
	public boolean handles(Class<?> cls) {
		return (cls == InventorySettings.class);
	}

	@Override
	public boolean handles(QName name) {
		return QNAME.equals(name);
	}

}
