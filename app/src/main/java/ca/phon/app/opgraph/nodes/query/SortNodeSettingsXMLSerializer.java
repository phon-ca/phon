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
import ca.phon.app.opgraph.nodes.query.SortNodeSettings.FeatureFamily;
import ca.phon.app.opgraph.nodes.query.SortNodeSettings.IPASortColumn;
import ca.phon.app.opgraph.nodes.query.SortNodeSettings.PlainSortColumn;
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
		
		if(settings.getGroupBy() != null && settings.getGroupBy().trim().length() > 0) {
			final Element groupByEle = 
					doc.createElement("groupBy");
			groupByEle.setTextContent(settings.getGroupBy());
			settingsEle.appendChild(groupByEle);
		}
		
		for(SortColumn sc:settings.getSorting()) {
			final Element scEle = doc.createElement("sortBy");
			scEle.setAttribute("column", sc.getColumn());
			scEle.setAttribute("type", sc.getType().toString().toLowerCase());
			if(sc.getType() == SortType.PLAIN) {
				scEle.setAttribute("order", 
						((PlainSortColumn)sc).getOrder().toString().toLowerCase());
			} else if(sc.getType() == SortType.IPA) {
				final IPASortColumn ipaSc = (IPASortColumn)sc;
				for(FeatureFamily family:ipaSc.getOrder()) {
					final Element orderEle = doc.createElement("order");
					orderEle.setTextContent(family.toString().toLowerCase());
					scEle.appendChild(orderEle);
				}
			}
			settingsEle.appendChild(scEle);
		}
		parentElem.appendChild(settingsEle);
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory, OpGraph graph, Object parent, Document doc, Element elem)
			throws IOException {
		if(!QNAME.equals(XMLSerializerFactory.getQName(elem)))
			throw new IOException("Incorrect element");
		
		final SortNodeSettings retVal = new SortNodeSettings();
		
		NodeList groupByList = elem.getElementsByTagName("groupBy");
		if(groupByList.getLength() > 0) {
			final Node groupByEle = groupByList.item(0);
			final String groupByColumn = groupByEle.getNodeValue();
			retVal.setGroupBy(groupByColumn);
		}
		
		NodeList sortByList = elem.getElementsByTagName("sortBy");
		for(int i = 0; i < sortByList.getLength(); i++) {
			final Node sortByNode = sortByList.item(i);
			final NamedNodeMap attrs = sortByNode.getAttributes();
			final String type = 
					attrs.getNamedItem("type").getNodeValue();
			if(SortType.PLAIN.toString().equalsIgnoreCase(type)) {
				final PlainSortColumn plainSortBy = new PlainSortColumn();
				plainSortBy.setColumn(attrs.getNamedItem("column").getNodeValue());
				if(attrs.getNamedItem("order") != null) {
					final SortOrder order = 
							SortOrder.fromString(attrs.getNamedItem("order").getNodeValue());
					plainSortBy.setOrder(order);
				}
			} else if(SortType.IPA.toString().equalsIgnoreCase(type)) {
				final IPASortColumn ipaSortBy = new IPASortColumn();
				ipaSortBy.setColumn(attrs.getNamedItem("column").getNodeValue());
				
				NodeList orderList = sortByNode.getChildNodes();
				FeatureFamily[] order = new FeatureFamily[orderList.getLength()];
				for(int j = 0; j < orderList.getLength(); j++) {
					final Node orderNode = orderList.item(j);
					final FeatureFamily family = 
							FeatureFamily.fromString(orderNode.getNodeValue());
					order[j] = family;
				}
				ipaSortBy.setOrder(order);
			}
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
