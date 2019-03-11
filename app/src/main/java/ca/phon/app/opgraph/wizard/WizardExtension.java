/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ca.phon.app.opgraph.wizard.WizardExtensionEvent.EventType;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.OpGraphListener;
import ca.phon.opgraph.OpLink;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.Processor;
import ca.phon.opgraph.nodes.general.LinkedMacroNodeOverrides;

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
	
	public final static String OPGRAPH_CTX_KEY = "graph";
	
	private final OpGraph graph;
	
	private final List<WizardExtensionListener> listenerList = 
			Collections.synchronizedList(new ArrayList<>());
	
	private final OpGraphListener graphListener = new OpGraphListener() {
		
		@Override
		public void nodeSwapped(OpGraph graph, OpNode oldNode, OpNode newNode) {
			if(wizardNodes.contains(oldNode)) {
				int oldIdx = wizardNodes.indexOf(oldNode);
				wizardNodes.remove(oldIdx);
				wizardNodes.add(oldIdx, newNode);
				
				NodeInfo nodeInfo = nodeInfoMap.remove(oldNode);
				nodeInfoMap.put(newNode, nodeInfo);
			}
			
			if(optionalNodes.contains(oldNode)) {
				int oldIdx = optionalNodes.indexOf(oldNode);
				optionalNodes.remove(oldIdx);
				optionalNodes.add(oldIdx, newNode);
				
				boolean optDefault = optionalDefaults.remove(oldNode);
				optionalDefaults.put(newNode, optDefault);
			}
		}
		
		@Override
		public void nodeRemoved(OpGraph graph, OpNode node) {
			if(wizardNodes.contains(node)) {
				wizardNodes.remove(node);
				nodeInfoMap.remove(node);
			}
			if(optionalNodes.contains(node)) {
				optionalNodes.remove(node);
				optionalDefaults.remove(node);
			}
		}
		
		@Override
		public void nodeAdded(OpGraph graph, OpNode node) {
		}
		
		@Override
		public void linkRemoved(OpGraph graph, OpLink link) {
		}
		
		@Override
		public void linkAdded(OpGraph graph, OpLink link) {
		}
	};
	
	public WizardExtension(OpGraph graph) {
		super();
		this.graph = graph;
		this.graph.addGraphListener(graphListener);
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
	
}
