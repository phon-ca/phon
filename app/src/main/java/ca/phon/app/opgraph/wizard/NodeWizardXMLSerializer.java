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

import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.io.xml.XMLSerializer;
import ca.phon.opgraph.io.xml.XMLSerializerFactory;

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
		
		final Element wizardInfoEle = doc.createElementNS(NAMESPACE, PREFIX + ":info");
		wizardInfoEle.setAttribute("title", nodeList.getWizardTitle());
		wizardInfoEle.setAttribute("format", nodeList.getWizardMessageFormat().toString().toLowerCase());
		final Element wizardMessageEle = doc.createElementNS(NAMESPACE, PREFIX + ":message");
		wizardMessageEle.setTextContent(nodeList.getWizardMessage());
		wizardInfoEle.appendChild(wizardMessageEle);
		settingsEle.appendChild(wizardInfoEle);
		
		for(OpNode node:nodeList) {
			final Element nodeEle = 
					doc.createElementNS(NAMESPACE, PREFIX + ":node");
			nodeEle.setAttribute("ref", node.getId());
			if(nodeList.isNodeForced(node)) {
				nodeEle.setAttribute("showAsStep", Boolean.toString(nodeList.isNodeForced(node)));
			}
			
			final Element infoEle = doc.createElementNS(NAMESPACE, PREFIX + ":info");
			infoEle.setAttribute("title", nodeList.getNodeTitle(node));
			infoEle.setAttribute("format", nodeList.getNodeMessageFormat(node).toString().toLowerCase());
			final Element messageEle = doc.createElementNS(NAMESPACE, PREFIX + ":message");
			messageEle.setTextContent(nodeList.getNodeMessage(node));
			infoEle.appendChild(messageEle);
			nodeEle.appendChild(infoEle);
			
			settingsEle.appendChild(nodeEle);
		}
		
		for(OpNode node:nodeList.getOptionalNodes()) {
			final Element nodeEle = 
					doc.createElementNS(NAMESPACE, PREFIX + ":optionalNode");
			nodeEle.setAttribute("ref", node.getId());
			nodeEle.setAttribute("enabled", Boolean.toString(nodeList.getOptionalNodeDefault(node)));
			
			settingsEle.appendChild(nodeEle);
		}
		
		for(NodeWizardReportTemplate report:nodeList.getReportTemplates()) {
			final Element reportEle =
					doc.createElementNS(NAMESPACE, PREFIX + ":report-template");
			reportEle.setAttribute("name", report.getName());
			reportEle.setTextContent(report.getTemplate());
			
			settingsEle.appendChild(reportEle);
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
			if(!WizardExtension.class.isAssignableFrom(clz))
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
			if(child.getNodeName().equals(PREFIX + ":info")) {
				final Node titleAttr = child.getAttributes().getNamedItem("title");
				if(titleAttr != null) {
					ext.setWizardTitle(titleAttr.getNodeValue());
				}
				final Node formatAttr = child.getAttributes().getNamedItem("format");
				WizardInfoMessageFormat format = WizardInfoMessageFormat.HTML;
				if(formatAttr != null) {
					format = WizardInfoMessageFormat.valueOf(formatAttr.getNodeValue().toUpperCase());
				}
				final NodeList infoNodes = child.getChildNodes();
				for(int j = 0; j < infoNodes.getLength(); j++) {
					final Node infoNode = infoNodes.item(j);
					if(infoNode.getNodeName().equals(PREFIX + ":message")) {
						ext.setWizardMessage(infoNode.getTextContent(), format);
					}
				}
			} else if(child.getNodeName().equals(PREFIX + ":node")) {
				final String nodeId = child.getAttributes().getNamedItem("ref").getNodeValue();
				
				final OpNode node = graph.getNodeById(nodeId, true);
				if(node != null) {
					ext.addNode(node);
					final boolean isForced = Boolean.parseBoolean(child.getAttributes().getNamedItem("showAsStep").getNodeValue());
					ext.setNodeForced(node, isForced);
					
					final NodeList subNodes = child.getChildNodes();
					for(int j = 0; j < subNodes.getLength(); j++) {
						final Node subNode = subNodes.item(j);
						if(subNode.getNodeName().equals(PREFIX + ":info")) {
							final Node titleAttr = subNode.getAttributes().getNamedItem("title");
							if(titleAttr != null) {
								ext.setNodeTitle(node, titleAttr.getNodeValue());
							}
							final Node formatAttr = subNode.getAttributes().getNamedItem("format");
							WizardInfoMessageFormat format = WizardInfoMessageFormat.HTML;
							if(formatAttr != null) {
								format = WizardInfoMessageFormat.valueOf(formatAttr.getNodeValue().toUpperCase());
							}
							
							final NodeList infoNodes = subNode.getChildNodes();
							for(int k = 0; k < infoNodes.getLength(); k++) {
								final Node infoNode = infoNodes.item(k);
								if(infoNode.getNodeName().equals(PREFIX + ":message")) {
									ext.setNodeMessage(node, infoNode.getTextContent(), format);
								}
							}
						}
					}
				}
			} else if(child.getNodeName().equals(PREFIX + ":optionalNode")) {
				final String nodeId = child.getAttributes().getNamedItem("ref").getNodeValue();
				final OpNode node = graph.getNodeById(nodeId, true);
				if(child.getAttributes().getNamedItem("enabled") != null) {
					ext.setOptionalNodeDefault(node, 
							Boolean.parseBoolean(
									child.getAttributes().getNamedItem("enabled").getNodeValue()));
				}
				if(node != null) 
					ext.addOptionalNode(node);
			} else if(child.getNodeName().equals(PREFIX + ":report-template")) {
				final String reportName = child.getAttributes().getNamedItem("name").getNodeValue();
				final String reportTemplate = child.getTextContent();
				ext.putReportTemplate(reportName, reportTemplate);
			}
		}
		
		graph.putExtension(WizardExtension.class, ext);
		
		return null;
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
