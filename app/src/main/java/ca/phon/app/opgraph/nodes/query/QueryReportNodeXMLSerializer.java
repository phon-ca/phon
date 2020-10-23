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
package ca.phon.app.opgraph.nodes.query;

import static ca.phon.opgraph.io.xml.XMLSerializerFactory.*;

import java.io.*;

import javax.xml.namespace.*;

import org.w3c.dom.*;

import ca.phon.opgraph.*;
import ca.phon.opgraph.extensions.*;
import ca.phon.opgraph.io.xml.XMLSerializer;
import ca.phon.opgraph.io.xml.XMLSerializerFactory;
import ca.phon.xml.*;

public class QueryReportNodeXMLSerializer implements XMLSerializer {
	
	static final String NAMESPACE = "https://phon.ca/ns/opgraph_query";
	static final String PREFIX = "opqry";
	
	static final QName REPORT_NODE_QNAME = new QName(NAMESPACE, "queryReportNode", PREFIX);
	
	static final QName GRAPH_QNAME = new QName(DEFAULT_NAMESPACE, "graph", XMLConstants.DEFAULT_NS_PREFIX);
	
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
		
		retVal.setId(ele.getAttribute("id"));
		
		final NodeList children = ele.getChildNodes();
		// ele should have a single element: url or graph
		for(int childIndex = 0; childIndex < children.getLength(); ++childIndex) {
			final Node cnode = children.item(childIndex);
			if(cnode instanceof Element) {
				final Element childElem = (Element)cnode;
				final QName nodeName = XMLSerializerFactory.getQName(childElem);
				if(GRAPH_QNAME.equals(nodeName)) {
					final XMLSerializer graphSerializer = factory.getHandler(OpGraph.class);
					if(graphSerializer == null)
						throw new IOException("No handler for graph");
					final Object objRead = graphSerializer.read(factory, graph, retVal, doc, (Element)cnode);
					retVal.setReportGraph((OpGraph)objRead);
				} else {
					// Get a handler for the element
					final XMLSerializer serializer = factory.getHandler(nodeName);
					if(serializer == null)
						throw new IOException("Could not get handler for element: " + nodeName);

					// Published fields and extensions all take care of adding
					// themselves to the passed in object
					//
					serializer.read(factory, graph, retVal, doc, childElem);
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
		
		final XMLSerializer graphSerializer = factory.getHandler(OpGraph.class);
		if(graphSerializer == null)
			throw new IOException("No handler for graph");

		graphSerializer.write(factory, doc, reportElem, queryReportNode.getReportGraph());
		
		if(queryReportNode.getExtensionClasses().size() > 0) {
			final XMLSerializer serializer = factory.getHandler(Extendable.class);
			if(serializer == null)
				throw new IOException("No XML serializer for extensions");

			serializer.write(factory, doc, reportElem, queryReportNode);
		}
		
		ele.appendChild(reportElem);
	}

}
