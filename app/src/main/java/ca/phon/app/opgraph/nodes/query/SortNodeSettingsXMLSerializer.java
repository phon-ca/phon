package ca.phon.app.opgraph.nodes.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.print.attribute.standard.MediaSize.NA;
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
import ca.phon.app.opgraph.nodes.query.SortNodeSettings.FeatureFamily;
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
		
		if(settings.getGroupBy() != null && settings.getGroupBy().getColumn() != null &&
				settings.getGroupBy().getColumn().trim().length() > 0) {
			final Element groupByEle = 
					doc.createElementNS(NAMESPACE, PREFIX + ":groupBy");
			writeSortColumn(doc, groupByEle, settings.getGroupBy());
			settingsEle.appendChild(groupByEle);
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
		if(sc.getType() == SortType.PLAIN) {
			scEle.setAttribute("order", 
					sc.getOrder().toString().toLowerCase());
		} else if(sc.getType() == SortType.IPA) {
			for(FeatureFamily family:sc.getFeatureOrder()) {
				final Element orderEle = doc.createElementNS(NAMESPACE, PREFIX + ":featureOrder");
				orderEle.setTextContent(family.toString().toLowerCase());
				scEle.appendChild(orderEle);
			}
		}
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
					&& childNode.getLocalName().equals("groupBy")) {
				final SortColumn groupBy = readSortColumn(childNode);
				retVal.setGroupBy(groupBy);
			} else if(childNode.getNamespaceURI() != null && childNode.getNamespaceURI().equals(NAMESPACE)
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
		final String type = 
				attrs.getNamedItem("type").getNodeValue();
		if(SortType.PLAIN.toString().equalsIgnoreCase(type)) {
			retVal.setColumn(attrs.getNamedItem("column").getNodeValue());
			retVal.setType(SortType.PLAIN);
			if(attrs.getNamedItem("order") != null) {
				final SortOrder order = 
						SortOrder.fromString(attrs.getNamedItem("order").getNodeValue());
				retVal.setOrder(order);
			}
		} else if(SortType.IPA.toString().equalsIgnoreCase(type)) {
			retVal.setColumn(attrs.getNamedItem("column").getNodeValue());
			retVal.setType(SortType.IPA);
			NodeList orderList = sortColumnNode.getChildNodes();
			List<FeatureFamily> order = new ArrayList<>();
			for(int j = 0; j < orderList.getLength(); j++) {
				final Node orderNode = orderList.item(j);
				if(orderNode.getNamespaceURI() != null && orderNode.getNamespaceURI().equals(NAMESPACE)
						&& orderNode.getLocalName().equals("featureOrder")) {
					final FeatureFamily family = 
							FeatureFamily.fromString(orderNode.getTextContent());
					order.add(family);
				}
			}
			retVal.setFeatureOrder(order.toArray(new FeatureFamily[0]));
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
