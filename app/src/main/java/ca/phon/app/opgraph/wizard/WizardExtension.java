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
	
	private String wizardTitle = new String();
	
	private String wizardMessage = new String();
	
	private final List<OpNode> wizardNodes = new ArrayList<>();
	
	private Map<OpNode, String> nodeTitles = new HashMap<>();
	
	private Map<OpNode, String> nodeMessages = new HashMap<>();
	
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
		return wizardNodes.add(e);
	}
	
	public int indexOf(OpNode e) {
		return wizardNodes.indexOf(e);
	}

	public boolean removeNode(Object o) {
		return wizardNodes.remove(o);
	}

	public void clear() {
		wizardNodes.clear();
	}

	public OpNode getNode(int index) {
		return wizardNodes.get(index);
	}

	public void addNode(int index, OpNode element) {
		wizardNodes.add(index, element);
	}

	public OpNode removeNode(int index) {
		return wizardNodes.remove(index);
	}
	
	public void setNodeTitle(OpNode node, String title) {
		nodeTitles.put(node, title);
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
		String retVal = nodeTitles.get(node);
		if(retVal == null || retVal.length() == 0) {
			retVal = node.getName();
		}
		return retVal;
	}
	
	public void setNodeMessage(OpNode node, String message) {
		nodeMessages.put(node, message);
	}
	
	public String getNodeMessage(OpNode node) {
		return nodeMessages.get(node);
	}
	
	public void setWizardTitle(String title) {
		this.wizardTitle = title;
	}
	
	public String getWizardTitle() {
		String retVal = this.wizardTitle;
		if(retVal == null || retVal.length() == 0) {
			retVal = "Introduction";
		}
		return retVal;
	}
	
	public void setWizardMessage(String message) {
		this.wizardMessage = message;
	}
	
	public String getWizardMessage() {
		return this.wizardMessage;
	}
}
