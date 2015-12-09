package ca.phon.app.opgraph.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JScrollPane;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.phon.app.log.MultiBufferPanel;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.wizard.WizardFrame;
import ca.phon.ui.wizard.WizardStep;

/**
 * Provides a wizard for an {@link OpGraph}.  Nodes are
 * selected and ordered in the editor and the settings 
 * panel for each node (if available) will be presented
 * as a step inside the wizard.
 */
public class WizardExtension implements Iterable<OpNode> {
	
	private final List<OpNode> wizardNodes = new ArrayList<>();
	
	private final OpGraph graph;
	
	public WizardExtension(OpGraph graph) {
		super();
		this.graph = graph;
	}
	
	public WizardFrame createWizard(Processor processor) {
		return new NodeWizard("Node Wizard", processor, graph);
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
	
}
