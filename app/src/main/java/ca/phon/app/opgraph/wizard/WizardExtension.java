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

import java.util.*;
import java.util.stream.Collectors;

import ca.phon.app.opgraph.wizard.WizardExtensionEvent.EventType;
import ca.phon.opgraph.*;

/**
 * Provides a wizard for an {@link OpGraph}.  Nodes are
 * selected and ordered in the editor and the settings 
 * panel for each node (if available) will be presented
 * as a step inside the wizard.
 */
public class WizardExtension implements Iterable<OpNode>, Cloneable {
	
	public final static String WIZARDEXT_CTX_KEY = "wizardExtension";
	
	private WizardInfo wizardInfo = new WizardInfo();
	
	private final List<OpNode> wizardNodes = new ArrayList<>();
	
	private Map<OpNode, NodeInfo> nodeInfoMap = new HashMap<>();
	
	private final List<OpNode> optionalNodes = new ArrayList<>();
	
	private final Map<OpNode, Boolean> optionalDefaults = new HashMap<>();
	
	private final List<NodeWizardReportTemplate> reportTemplates = new ArrayList<>();
	
	public final static String OPGRAPH_CTX_KEY = "graph";
	
	private final OpGraph graph;
	
	private final List<WizardExtensionListener> listenerList = 
			Collections.synchronizedList(new ArrayList<>());
	
	public WizardExtension(OpGraph graph) {
		super();
		this.graph = graph;
		addDefaultReportTemplates();
	}
	
	public void addDefaultReportTemplates() {
		if(getReportTemplate("Report Prefix") == null)
			putReportTemplate("Report Prefix", "");
		if(getReportTemplate("Report Suffix") == null)
			putReportTemplate("Report Suffix", "");
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
		
		fireNodeEvent(EventType.NODE_ADDED_TO_SETTINGS, e);
		
		return retVal;
	}
	
	public int indexOf(OpNode e) {
		return wizardNodes.indexOf(e);
	}

	public boolean removeNode(Object o) {
		boolean retVal = wizardNodes.remove(o);
		nodeInfoMap.remove(o);
		
		fireNodeEvent(EventType.NODE_REMOVED_FROM_SETTINGS, (OpNode)o);
		
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
		if(nodeInfo != null) {
			nodeInfo.setSettingsForced(forced);
			
			fireNodeEvent( (forced ? EventType.NODE_MARKED_AS_REQUIRED : EventType.NODE_MAKRED_AS_NOT_REQUIRED), node);
		}
	}
	
	public boolean isNodeForced(OpNode node) {
		final NodeInfo nodeInfo = nodeInfoMap.get(node);
		return (nodeInfo != null ? nodeInfo.isSettingsForced() : false);
	}

	public OpNode removeNode(int index) {
		OpNode retVal = wizardNodes.remove(index);
		
		fireNodeEvent(EventType.NODE_REMOVED_FROM_SETTINGS, retVal);
		
		return retVal;
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
		
		fireNodeEvent(EventType.NODE_MAKRED_AS_OPTIONAL, node);
	}
	
	public OpNode removeOptionalNode(int index) {
		OpNode retVal = optionalNodes.remove(index);
		
		fireNodeEvent(EventType.NODE_MAKRED_AS_NONOPTIONAL, retVal);
		
		return retVal;
	}
	
	public boolean removeOptionalNode(OpNode node) {
		boolean retVal = optionalNodes.remove(node);
		
		if(retVal)
			fireNodeEvent(EventType.NODE_MAKRED_AS_NONOPTIONAL, node);
		
		return retVal;
	}

	public int getOptionalNodeCount() {
		return optionalNodes.size();
	}
	
	public List<OpNode> getOptionalNodes() {
		return Collections.unmodifiableList(optionalNodes);
	}
	
	public Map<OpNode, Boolean> getOptionalNodeDefaults() {
		return Collections.unmodifiableMap(this.optionalDefaults);
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
	
	public boolean isNodeOptional(OpNode node) {
		return getOptionalNodes().contains(node);
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
		String oldTitle = getWizardTitle();
		wizardInfo.setTitle(title);
		fireTitleChangedEvent(oldTitle, title);
	}
	
	public String getWizardTitle() {
		String retVal = wizardInfo.getTitle();
		if(retVal == null || retVal.length() == 0) {
			retVal = "";
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
		return Collections.unmodifiableSet( reportTemplates.stream()
				.map( (rt) -> rt.getName() )
				.collect(Collectors.toSet()) );
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
			retVal.setTemplate(template);
			
			fireReportTemplateEvent(EventType.REPORT_TEMPLATE_ADDED, name, "", template);
		} else {
			final String oldContent = retVal.getTemplate();
			retVal.setTemplate(template);
			
			fireReportTemplateEvent(EventType.REPORT_TEMPLATE_CHANGED, name, oldContent, template);
		}
		return retVal;
	}
	
	public void removeReportTemplate(String name) {
		final Iterator<NodeWizardReportTemplate> itr = reportTemplates.iterator();
		while(itr.hasNext()) {
			final NodeWizardReportTemplate template = itr.next();
			if(template.getName().equals(name)) {
				itr.remove();
				
				fireReportTemplateEvent(EventType.REPORT_TEMPLATE_REMOVED, name, template.getTemplate(), null);
				
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
		context.put(WIZARDEXT_CTX_KEY, this);
		context.put(OPGRAPH_CTX_KEY, getGraph());
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
	
	public void addWizardExtensionListener(WizardExtensionListener listener) {
		if(listenerList.contains(listener))
			listenerList.add(listener);
	}
	
	public void removeWizardExtensionListener(WizardExtensionListener listener) {
		listenerList.remove(listener);
	}
	
	public List<WizardExtensionListener> getWizardExtensionListeners() {
		return Collections.unmodifiableList(listenerList);
	}
	
	public void fireTitleChangedEvent(String oldTitle, String newTitle) {
		final WizardExtensionEvent event = new WizardExtensionEvent(oldTitle, newTitle);
		getWizardExtensionListeners().forEach( (l) -> l.wizardExtensionChanged(event) );
	}
	
	public void fireNodeTitleChangedEvent(OpNode node, String oldTitle, String newTitle) {
		final WizardExtensionEvent event = new WizardExtensionEvent(node, oldTitle, newTitle);
		getWizardExtensionListeners().forEach( (l) -> l.wizardExtensionChanged(event) );
	}
	
	public void fireNodeEvent(EventType eventType, OpNode node) {
		final WizardExtensionEvent event = new WizardExtensionEvent(EventType.NODE_ADDED_TO_SETTINGS, node);
		getWizardExtensionListeners().forEach( (l) -> l.wizardExtensionChanged(event) );
	}
	
	public void fireReportTemplateEvent(EventType eventType, String reportName, String oldContent, String reportContent) {
		final WizardExtensionEvent event = new WizardExtensionEvent(eventType, reportName, oldContent, reportContent);
		getWizardExtensionListeners().forEach( (l) -> l.wizardExtensionChanged(event) );
	}
	
}
