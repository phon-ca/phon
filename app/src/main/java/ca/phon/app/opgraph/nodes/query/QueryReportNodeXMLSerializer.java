package ca.phon.app.opgraph.nodes.query;

import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.io.xml.XMLSerializer;
import ca.phon.opgraph.io.xml.XMLSerializerFactory;

public class QueryReportNodeXMLSerializer implements XMLSerializer {
	
	static final String NAMESPACE = "https://phon.ca/ns/opgraph_query";
	static final String PREFIX = "opqry";
	
	static final QName REPORT_NODE_QNAME = new QName(NAMESPACE, "queryReportNode", PREFIX);
	
	@Override
	public boolean handles(Class<?> arg0) {
		return (arg0 == QueryReportNode.class);
	}

	@Override
	public boolean handles(QName arg0) {
		return REPORT_NODE_QNAME.equals(arg0);
	}

	@Override
	public Object read(XMLSerializerFactory factory, OpGraph graph, Object parent, Document doc, Element ele)
			throws IOException {
		if(!REPORT_NODE_QNAME.equals(XMLSerializerFactory.getQName(ele)))
			throw new IOException("Incorrect element");
		
		QueryReportNode retVal = new QueryReportNode();
		final NodeList children = ele.getChildNodes();
		// ele should have a single element: url or graph
		for(int childIndex = 0; childIndex < children.getLength(); ++childIndex) {
			final Node cnode = children.item(childIndex);
			if(cnode instanceof Element) {
				if(cnode.getNodeName().equals(PREFIX + ":url")) {
					// load graph from url
					URL url = new URL(cnode.getTextContent());
					retVal.setReportGraphURL(url);
				} else if(cnode.getNodeName().equals(PREFIX + ":graph")) {
					final XMLSerializer graphSerializer = factory.getHandler(OpGraph.class);
					if(graphSerializer == null)
						throw new IOException("No handler for graph");
					final Object objRead = graphSerializer.read(factory, graph, retVal, doc, (Element)cnode);
					retVal.setReportGraph((OpGraph)objRead);
				}
			}
		}
		
		return retVal;
	}

	@Override
	public void write(XMLSerializerFactory factory, Document doc, Element ele, Object obj) throws IOException {
		if(obj == null)
			throw new IOException("Null object given to serializer");

		if(!(obj instanceof QueryReportNode))
			throw new IOException(QueryReportNodeXMLSerializer.class.getName() + " cannot write objects of type " + obj.getClass().getName());

		final QueryReportNode queryReportNode = (QueryReportNode)obj;
		final Element reportElem = doc.createElementNS(NAMESPACE, PREFIX + ":queryReportNode");
		reportElem.setAttribute("id", queryReportNode.getId());
		reportElem.setAttribute("type", obj.getClass().getName());
		
		if(queryReportNode.getName().equals(queryReportNode.getDefaultName())) {
			reportElem.setAttribute("name", queryReportNode.getName());
		}
		
		if(!queryReportNode.getDescription().equals(queryReportNode.getDefaultDescription())) {
			final Element descriptionElem = doc.createElementNS(NAMESPACE, PREFIX + ":description");
			descriptionElem.setTextContent(queryReportNode.getDescription());
			reportElem.appendChild(descriptionElem);
		}
		
		if(queryReportNode.getReportGraphURL() != null) {
			final Element urlEle = doc.createElementNS(NAMESPACE, PREFIX + ":url");
			urlEle.setTextContent(queryReportNode.getReportGraphURL().toString());
			reportElem.appendChild(urlEle);
		} else if(queryReportNode.getReportGraph() != null) {
			final XMLSerializer graphSerializer = factory.getHandler(OpGraph.class);
			if(graphSerializer == null)
				throw new IOException("No handler for graph");

			graphSerializer.write(factory, doc, reportElem, queryReportNode.getReportGraph());
		}
		
		ele.appendChild(reportElem);
	}

}
