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
import ca.gedge.opgraph.io.xml.XMLSerializer;
import ca.gedge.opgraph.io.xml.XMLSerializerFactory;
import ca.phon.app.opgraph.nodes.query.InventorySettings.ColumnInfo;

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
		for(int i = 0; i < childNodes.getLength(); i++) {
			final Node childNode = childNodes.item(i);
			if(childNode.getNodeName().equals("groupBy")) {
				retVal.setGroupBy(readColumnInfo(childNode));
			} else {
				retVal.getColumns().add(readColumnInfo(childNode));
			}
		}
		
		return retVal;
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
