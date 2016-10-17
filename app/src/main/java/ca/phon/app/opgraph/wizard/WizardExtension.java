/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.Processor;

/**
 * Provides a wizard for an {@link OpGraph}.  Nodes are
 * selected and ordered in the editor and the settings 
 * panel for each node (if available) will be presented
 * as a step inside the wizard.
 */
public class WizardExtension implements Iterable<OpNode>, Cloneable {
	
	public final static String WIZARDEXT_CTX_NAME = "wizardExtension";
	
	private WizardInfo wizardInfo = new WizardInfo();
	
	private final List<OpNode> wizardNodes = new ArrayList<>();
	
	private Map<OpNode, NodeInfo> nodeInfoMap = new HashMap<>();
	
	private final List<OpNode> optionalNodes = new ArrayList<>();
	
	private final Map<OpNode, Boolean> optionalDefaults = new HashMap<>();
	
	private final List<NodeWizardReportTemplate> reportTemplates = new ArrayList<>();
	
	public final static String OPGRAPH_CTX_NAME = "graph";
	
	private final OpGraph graph;
	
	public WizardExtension(OpGraph graph) {
		super();
		this.graph = graph;
	}
	
	public NodeWizard createWizard(Processor processor) {
		return new NodeWizard("Node Wizard", processor, graph);
	}
	
	public OpGraph getGraph() {
		return this.graph;
	}

	public int size() {
		return wizardNodes.size();
	}

	public boolean containsNode(OpNode o) {
		return wizardNodes.contains(o);
	}

	public Iterator<OpNode> iterator() {
		return wizardNodes.iterator();
	}

	public boolean addNode(OpNode e) {
		boolean retVal = wizardNodes.add(e);
		nodeInfoMap.put(e, new NodeInfo(e));
		return retVal;
	}
	
	public int indexOf(OpNode e) {
		return wizardNodes.indexOf(e);
	}

	public boolean removeNode(Object o) {
		boolean retVal = wizardNodes.remove(o);
		nodeInfoMap.remove(o);
		return retVal;
	}

	public void clear() {
		wizardNodes.clear();
	}

	public OpNode getNode(int index) {
		return wizardNodes.get(index);
	}
	
	public void setNodeForced(OpNode node, boolean forced) {
		final NodeInfo nodeInfo = nodeInfoMap.get(node);
		if(nodeInfo != null) nodeInfo.setSettingsForced(forced);
	}
	
	public boolean isNodeForced(OpNode node) {
		final NodeInfo nodeInfo = nodeInfoMap.get(node);
		return (nodeInfo != null ? nodeInfo.isSettingsForced() : false);
	}

	public void addNode(int index, OpNode element) {
		wizardNodes.add(index, element);
		nodeInfoMap.put(element, new NodeInfo(element));
	}

	public OpNode removeNode(int index) {
		return wizardNodes.remove(index);
	}
	
	public void setNodeTitle(OpNode node, String title) {
		final NodeInfo info = nodeInfoMap.get(node);
		if(info != null)
			info.setTitle(title);
	}
	
	public OpNode getOptionalNode(int index) {
		return optionalNodes.get(index);
	}
	
	public void addOptionalNode(OpNode node) {
		optionalNodes.add(node);
	}
	
	public void addOptionalNode(int index, OpNode node) {
		optionalNodes.add(index, node);
	}
	
	public OpNode removeOptionalNode(int index) {
		return optionalNodes.remove(index);
	}
	
	public boolean removeOptionalNode(OpNode node) {
		return optionalNodes.remove(node);
	}

	public int getOptionalNodeCount() {
		return optionalNodes.size();
	}
	
	public List<OpNode> getOptionalNodes() {
		return optionalNodes;
	}
	
	public Map<OpNode, Boolean> getOptionalNodeDefaults() {
		return this.optionalDefaults;
	}
	
	public void setOptionalNodeDefault(OpNode node, boolean enabled) {
		this.optionalDefaults.put(node, enabled);
	}
	
	public boolean getOptionalNodeDefault(OpNode node) {
		boolean enabled = true;
		if(this.optionalDefaults.containsKey(node))
			enabled = this.optionalDefaults.get(node);
		return enabled;
	}
	
	public String getNodeTitle(OpNode node) {
		final NodeInfo nodeInfo = nodeInfoMap.get(node);
		String title = (nodeInfo != null ? nodeInfo.getTitle() : "");
		if(title.length() == 0) title = node.getName();
		return title;
	}
	
	public void setNodeMessage(OpNode node, String message) {
		setNodeMessage(node, message, WizardInfoMessageFormat.HTML);
	}
	
	public void setNodeMessage(OpNode node, String message, WizardInfoMessageFormat format) {
		final NodeInfo nodeInfo = nodeInfoMap.get(node);
		if(nodeInfo != null) {
			nodeInfo.setMessage(message);
			nodeInfo.setFormat(format);
		}
	}
	
	public String getNodeMessage(OpNode node) {
		final NodeInfo nodeInfo = nodeInfoMap.get(node);
		return (nodeInfo != null ? nodeInfo.getMessage() : "");
	}
	
	public WizardInfoMessageFormat getNodeMessageFormat(OpNode node) {
		final NodeInfo nodeInfo = nodeInfoMap.get(node);
		return (nodeInfo != null ? nodeInfo.getFormat() : WizardInfoMessageFormat.HTML);
	}
	
	public NodeInfo getNodeInfo(OpNode node) {
		return nodeInfoMap.get(node);
	}
	
	public void setWizardTitle(String title) {
		wizardInfo.setTitle(title);
	}
	
	public String getWizardTitle() {
		String retVal = wizardInfo.getTitle();
		if(retVal == null || retVal.length() == 0) {
			retVal = "About";
		}
		return retVal;
	}
	
	public void setWizardMessage(String message) {
		setWizardMessage(message, WizardInfoMessageFormat.HTML);
	}
	
	public void setWizardMessage(String message, WizardInfoMessageFormat format) {
		wizardInfo.setMessage(message);
		wizardInfo.setFormat(format);
	}
	
	public String getWizardMessage() {
		return wizardInfo.getMessage();
	}
	
	public WizardInfoMessageFormat getWizardMessageFormat() {
		return wizardInfo.getFormat();
	}
	
	public WizardInfo getWizardInfo() {
		return this.wizardInfo;
	}
	
	public List<NodeWizardReportTemplate> getReportTemplates() {
		return Collections.unmodifiableList(reportTemplates);
	}
	
	public Set<String> getReportTemplateNames() {
		return reportTemplates.stream()
				.map( (rt) -> rt.getName() )
				.collect(Collectors.toSet());
	}
	
	public NodeWizardReportTemplate getReportTemplate(String name) {
		final Optional<NodeWizardReportTemplate> opt = reportTemplates.stream()
				.filter( (rt) -> rt.getName().equals(name) )
				.findAny();
		return (opt.isPresent() ? opt.get() : null);
	}
	
	/**
	 * Set report template text for the given identifier.
	 * 
	 * @param name
	 * @param template
	 * 
	 * @return the new or modified report template object
	 */
	public NodeWizardReportTemplate putReportTemplate(String name, String template) {
		NodeWizardReportTemplate retVal = getReportTemplate(name);
		if(retVal == null) {
			retVal = new NodeWizardReportTemplate(name, template);
			reportTemplates.add(retVal);
		}
		retVal.setTemplate(template);
		return retVal;
	}
	
	public void removeReportTemplate(String name) {
		final Iterator<NodeWizardReportTemplate> itr = reportTemplates.iterator();
		while(itr.hasNext()) {
			final NodeWizardReportTemplate template = itr.next();
			if(template.getName().equals(name)) {
				itr.remove();
				break;
			}
		}
	}
	
	/**
	 * Setup a map of object which will be added to the
	 * report generator context.
	 * 
	 * @param context
	 */
	public void setupReportContext(NodeWizardReportContext context) {
		context.put(WIZARDEXT_CTX_NAME, this);
		context.put(OPGRAPH_CTX_NAME, getGraph());
	}

	@Override
	protected Object clone() {
		final WizardExtension retVal = new WizardExtension(getGraph());
		
		retVal.setWizardTitle(getWizardTitle());
		retVal.setWizardMessage(getWizardMessage(), getWizardMessageFormat());
		
		for(OpNode node:this) {
			retVal.addNode(node);
			retVal.setNodeTitle(node, getNodeTitle(node));
			retVal.setNodeMessage(node, getNodeMessage(node), getNodeMessageFormat(node));
		}
		
		for(OpNode node:getOptionalNodes()) {
			retVal.addOptionalNode(node);
			retVal.setOptionalNodeDefault(node, getOptionalNodeDefault(node));
		}
		
		for(NodeWizardReportTemplate report:getReportTemplates()) {
			retVal.putReportTemplate(report.getName(), report.getTemplate());
		}
		
		return retVal;
	}
	
}
