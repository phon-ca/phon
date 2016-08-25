package ca.phon.app.opgraph.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.Processor;

/**
 * Provides a wizard for an {@link OpGraph}.  Nodes are
 * selected and ordered in the editor and the settings 
 * panel for each node (if available) will be presented
 * as a step inside the wizard.
 */
public class WizardExtension implements Iterable<OpNode> {
	
	private WizardInfo wizardInfo = new WizardInfo();
	
	private final List<OpNode> wizardNodes = new ArrayList<>();
	
	private Map<OpNode, NodeInfo> nodeInfoMap = new HashMap<>();
	
	private final List<OpNode> optionalNodes = new ArrayList<>();
	
	private final Map<OpNode, Boolean> optionalDefaults = new HashMap<>();
	
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
		return (nodeInfo != null ? nodeInfo.getMessageHTML() : "");
	}
	
	public WizardInfoMessageFormat getNodeMessageFormat(OpNode node) {
		final NodeInfo nodeInfo = nodeInfoMap.get(node);
		return (nodeInfo != null ? nodeInfo.getFormat() : WizardInfoMessageFormat.HTML);
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
		return wizardInfo.getMessageHTML();
	}
	
	public WizardInfoMessageFormat getWizardMessageFormat() {
		return wizardInfo.getFormat();
	}
}
