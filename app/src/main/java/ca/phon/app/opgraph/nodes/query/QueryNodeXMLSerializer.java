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

import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.extensions.Extendable;
import ca.phon.opgraph.io.xml.*;
import ca.phon.query.db.*;
import ca.phon.query.db.xml.XMLQuery;
import ca.phon.query.db.xml.io.query.*;
import ca.phon.query.script.*;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.*;
import ca.phon.xml.XMLConstants;
import jakarta.xml.bind.*;
import org.w3c.dom.Element;
import org.w3c.dom.*;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.*;

public class QueryNodeXMLSerializer implements XMLSerializer {

	static final String NAMESPACE = "https://phon.ca/ns/opgraph_query";
	static final String PREFIX = "opqry";
	
	static final String QUERY_NAMESPACE = "http://phon.ling.mun.ca/ns/query";
	static final String QUERY_PREFIX = "qry";
	
	// qualified names
	static final QName QUERY_NODE_QNAME = new QName(NAMESPACE, "queryNode", PREFIX);
	
	@Override
	public void write(XMLSerializerFactory serializerFactory, Document doc,
			Element parentElem, Object obj) throws IOException {
		if(obj == null)
			throw new IOException("Null object given to serializer");

		if(!(obj instanceof QueryNode))
			throw new IOException(QueryNodeXMLSerializer.class.getName() + " cannot write objects of type " + obj.getClass().getName());

		// setup namespace for document
		final Element rootEle = doc.getDocumentElement();
		rootEle.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				XMLConstants.XMLNS_ATTRIBUTE + ":" + PREFIX, NAMESPACE);
		rootEle.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE + ":" + QUERY_PREFIX, QUERY_NAMESPACE);
		
		// Create node element
		final QueryNode queryNode = (QueryNode)obj;
		final Element queryNodeElem = doc.createElementNS(NAMESPACE, PREFIX + ":" + QUERY_NODE_QNAME.getLocalPart());
		
		queryNodeElem.setAttribute("id", queryNode.getId());
		queryNodeElem.setAttribute("type", obj.getClass().getName());
		
		if(!queryNode.getName().equals(queryNode.getDefaultName()))
			queryNodeElem.setAttribute("name", queryNode.getName());
		
		
		// write query elements
		final QueryScript queryScript = queryNode.getQueryScript();
		final QueryName qn = 
				(queryScript.getExtension(QueryName.class) != null ? queryScript.getExtension(QueryName.class) : new QueryName(queryNode.getName()));
				
		// create a query object
		final QueryManager qm = QueryManager.getSharedInstance();
		final QueryFactory qf = qm.createQueryFactory();
		
		final Query q = qm.createQueryFactory().createQuery();
		q.setName(qn.getName());
		
		final Script s = qf.createScript();
		final ScriptLibrary scriptLibrary = qn.getScriptLibrary();
		if(scriptLibrary == ScriptLibrary.STOCK) {
			s.setUrl(new ScriptURL(qn.getName(), scriptLibrary));
		} else {
			s.setSource(queryScript.getScript());
		}

		final Map<String, String> paramMap = new TreeMap<String, String>();
		try {
			final ScriptParameters scriptParams = 
					queryScript.getContext().getScriptParameters(queryScript.getContext().getEvaluatedScope());
			for(ScriptParam scriptParam:scriptParams) {
				if(scriptParam.hasChanged()) {
					for(String paramId:scriptParam.getParamIds()) {
						final Object v = scriptParam.getValue(paramId);
						if(v != null) {
							paramMap.put(paramId, v.toString());
						}
					}
				}
			}
		} catch (PhonScriptException e) {
			throw new IOException(e);
		}
		s.setParameters(paramMap);
		q.setScript(s);
		
		// use jaxb to save to element
		try {
			QName queryQName = new QName(QUERY_NAMESPACE, "query", QUERY_PREFIX);
			JAXBContext ctx = JAXBContext.newInstance(ca.phon.query.db.xml.io.query.ObjectFactory.class);
			Marshaller marshaller = ctx.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			final QueryType qt = ((XMLQuery)q).getXMLObject();
			JAXBElement<QueryType> queryEle = new JAXBElement<QueryType>(queryQName, QueryType.class, qt);
			marshaller.marshal(queryEle, queryNodeElem);
			queryNodeElem.getChildNodes().item(0).setPrefix(QUERY_PREFIX);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
		
		// Extensions last
		if(queryNode.getExtensionClasses().size() > 0) {
			final XMLSerializer serializer = serializerFactory.getHandler(Extendable.class);
			if(serializer == null)
				throw new IOException("No XML serializer for extensions");

			serializer.write(serializerFactory, doc, queryNodeElem, queryNode);
		}
		
		parentElem.appendChild(queryNodeElem);
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory, OpGraph graph,
			Object parent, Document doc, Element elem) throws IOException {
		if(!QUERY_NODE_QNAME.equals(XMLSerializerFactory.getQName(elem)))
			throw new IOException("Incorrect element");
		
		QueryNode retVal = new QueryNode();
		
		NodeList children = elem.getElementsByTagNameNS(QUERY_NAMESPACE, "query");
		if(children.getLength() > 0) {
			Node queryNode = children.item(0);
			
			if(queryNode.getNodeName().equals(QUERY_PREFIX + ":query")
					&& queryNode.getNamespaceURI().equals(QUERY_NAMESPACE)) {
				try {
					JAXBContext ctx = JAXBContext.newInstance(ObjectFactory.class);
					Unmarshaller unmarshaller = ctx.createUnmarshaller();
					JAXBElement<QueryType> queryTypeEle = unmarshaller.unmarshal(queryNode, QueryType.class);

					final Query query = new XMLQuery(queryTypeEle.getValue(), queryTypeEle.getValue().getName());
					
					final QueryScript queryScript = new QueryScript(query.getScript().getSource());
					final QueryName queryName = new QueryName(query.getName());
					queryScript.putExtension(QueryName.class, queryName);
					
					if(query.getScript().getUrl() != null) {
						queryName.setScriptLibrary(query.getScript().getUrl().getLibrary());
					}
					
					// setup saved parameters
					ScriptParameters params = queryScript.getContext().getScriptParameters(
							queryScript.getContext().getEvaluatedScope());
					for(ScriptParam sp:params) {
						for(String id:sp.getParamIds()) {
							Object v = query.getScript().getParameters().get(id);
							if(v != null) {
								sp.setValue(id, v);
							}
						}
					}
					
					retVal = new QueryNode(queryScript);
				} catch (JAXBException | PhonScriptException e) {
					throw new IOException(e);
				}
			}
		}
		
		// setup id and other attributes
		if(elem.hasAttribute("id"))
			retVal.setId(elem.getAttribute("id"));

		if(elem.hasAttribute("name"))
			retVal.setName(elem.getAttribute("name"));
		
		// read extensions
		NodeList extensionNodes = elem.getElementsByTagName("extensions");
		if(extensionNodes.getLength() > 0) {
			Element extensionNode = (Element)extensionNodes.item(0);
			QName name = XMLSerializerFactory.getQName(extensionNode);
			final XMLSerializer serializer = serializerFactory.getHandler(name);
			if(serializer == null)
				throw new IOException("Could not get handler for element: " + name);

			// Published fields and extensions all take care of adding
			// themselves to the passed in object
			//
			serializer.read(serializerFactory, graph, retVal, doc, extensionNode);
		}
		
		return retVal;
	}
	
	@Override
	public boolean handles(Class<?> cls) {
		return (cls == QueryNode.class);
	}

	@Override
	public boolean handles(QName name) {
		return QUERY_NODE_QNAME.equals(name);
	}

}
