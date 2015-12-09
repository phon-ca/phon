package ca.phon.app.opgraph.wizard;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.io.xml.XMLSerializer;
import ca.gedge.opgraph.io.xml.XMLSerializerFactory;
import ca.phon.app.opgraph.nodes.query.InventorySettings;

public class NodeWizardXMLSerializer implements XMLSerializer {
	
	static final String NAMESPACE = "https://phon.ca/ns/opgraph_query";
	static final String PREFIX = "opqry";
	
	static final QName QNAME = new QName(NAMESPACE, "nodewizard", PREFIX);

	@Override
	public void write(XMLSerializerFactory serializerFactory, Document doc,
			Element parentElem, Object obj) throws IOException {
		if(obj == null)
			throw new IOException("Null object given to serializer");
		
		if(!(obj instanceof WizardExtension))
			throw new IOException(getClass().getName() + " cannot write objects of type " + obj.getClass());
		
		final Element rootEle = doc.getDocumentElement();
		rootEle.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				XMLConstants.XMLNS_ATTRIBUTE + ":" + PREFIX, NAMESPACE);
		
		final WizardExtension nodeList = (WizardExtension)obj;
		final Element settingsEle =
				doc.createElementNS(NAMESPACE, PREFIX + ":" + QNAME.getLocalPart());
		settingsEle.setAttribute("type", obj.getClass().getName());
		
		for(OpNode node:nodeList) {
			final Element nodeEle = 
					doc.createElementNS(NAMESPACE, PREFIX + ":node");
			nodeEle.setNodeValue(node.getId());
			settingsEle.appendChild(nodeEle);
		}
		
		parentElem.appendChild(settingsEle);
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory, OpGraph graph,
			Object parent, Document doc, Element elem) throws IOException {
		if(!QNAME.equals(XMLSerializerFactory.getQName(elem)))
			throw new IOException("Incorrect element");
		
		final String nodeType = elem.getAttribute("type");
		WizardExtension ext = null;
		try {
			final Class<?> clz = Class.forName(nodeType);
			if(WizardExtension.class.isAssignableFrom(clz))
				throw new IOException(clz.getName() + " is not a subclass of " + WizardExtension.class.getName());
			
			// find constructor
			@SuppressWarnings("unchecked")
			final Constructor<? extends WizardExtension> ctr = 
					(Constructor<? extends WizardExtension>)clz.getConstructor(OpGraph.class);
			ext = ctr.newInstance(graph);
		} catch (ClassNotFoundException | NoSuchMethodException 
				| SecurityException | InstantiationException | IllegalAccessException 
				| IllegalArgumentException | InvocationTargetException e) {
			throw new IOException(e);
		}
		
		final NodeList childNodes = elem.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++) {
			final Node child = childNodes.item(i);
			if(!child.getNodeName().equals("node"))
				throw new IOException("Illegal child node " + child.getNodeName());
			final String nodeId = child.getNodeValue();
			
			final OpNode node = graph.getNodeById(nodeId, true);
			if(node != null) {
				ext.addNode(node);
			}
		}
		
		return ext;
	}

	@Override
	public boolean handles(Class<?> cls) {
		return WizardExtension.class.isAssignableFrom(cls);
	}

	@Override
	public boolean handles(QName name) {
		return QNAME.equals(name);
	}

}
