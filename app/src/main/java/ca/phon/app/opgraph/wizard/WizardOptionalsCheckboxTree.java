package ca.phon.app.opgraph.wizard;

import java.util.List;
import java.util.Stack;

import javax.swing.tree.TreePath;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.phon.ui.CheckedTreeNode;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;

public class WizardOptionalsCheckboxTree extends CheckboxTree {
	
	private static final long serialVersionUID = 1768403681110021388L;

	private WizardExtension wizardExtension;

	public WizardOptionalsCheckboxTree() {
		this(new WizardExtension(new OpGraph()));
	}
	
	public WizardOptionalsCheckboxTree(WizardExtension wizardExtension) {
		super(new CheckedTreeNode());
		this.wizardExtension = wizardExtension;
		
		init();
	}
	
	private void init() {
		((CheckedTreeNode)getModel().getRoot()).setUserObject(wizardExtension.getWizardTitle());
		setupTree();
		
		expandRow(0);
	}
	
	private void setupTree() {
		final OpGraph graph = wizardExtension.getGraph();
		for(OpNode optionalNode:wizardExtension.getOptionalNodes()) {
			final List<OpNode> nodePath = graph.getNodePath(optionalNode.getId());
			
			CheckedTreeNode parentTreeNode = (CheckedTreeNode)getModel().getRoot();
			for(OpNode node:nodePath) {
				CheckedOpNode childNode = null;
				// try to find a current tree node
				for(int i = 0; i < parentTreeNode.getChildCount(); i++) {
					CheckedOpNode childTreeNode = (CheckedOpNode)parentTreeNode.getChildAt(i);
					if(childTreeNode.node == node) {
						childNode = childTreeNode;
						break;
					}
				}
				if(childNode == null) {
					// create a new node
					childNode = new CheckedOpNode(node);
					parentTreeNode.add(childNode);
				}
				parentTreeNode = childNode;
			}
		}
	}
	
	public TreePath getNodePath(OpNode node) {
		CheckedOpNode treeNode = findTreeNode(node);
		if(treeNode == null) return null;
		
		TreePath retVal = new TreePath(getModel().getRoot());
		final Stack<CheckedOpNode> nodeStack = new Stack<>();
		while(treeNode != null) {
			nodeStack.push(treeNode);
			if(treeNode.getParent() != getModel().getRoot())
				treeNode = (CheckedOpNode)treeNode.getParent();
			else
				treeNode = null;
		}
		while(!nodeStack.isEmpty()) {
			retVal = retVal.pathByAddingChild(nodeStack.pop());
		}
		return retVal;
	}
	
	public void checkNode(OpNode node) {
		final TreePath checkPath = getNodePath(node);
		
		if(checkPath != null) {
			super.getCheckingModel().addCheckingPath(checkPath);
			super.expandPath(checkPath.getParentPath());
		}
	}
	
	private CheckedOpNode findTreeNode(OpNode node) {
		CheckedTreeNode currentNode = (CheckedTreeNode)getModel().getRoot();
		return findTreeNode(node, currentNode);
	}
	
	private CheckedOpNode findTreeNode(OpNode node, CheckedTreeNode parent) {
		CheckedOpNode retVal = null;
		
		for(int i = 0; i < parent.getChildCount(); i++) {
			CheckedOpNode childNode = (CheckedOpNode)parent.getChildAt(i);
			if(childNode.node == node) {
				retVal = childNode;
				break;
			} else if(childNode.getChildCount() > 0) {
				retVal = findTreeNode(node, childNode);
				if(retVal != null) break;
			}
		}
		
		return retVal;
	}

	public static class CheckedOpNode extends CheckedTreeNode {
		
		private static final long serialVersionUID = 7282780647003827544L;

		private OpNode node;
		
		public CheckedOpNode(OpNode node) {
			super();
			this.node = node;
			setUserObject(node.getName());
		}
		
		public OpNode getNode() { return node; }
		
	}
	
}
