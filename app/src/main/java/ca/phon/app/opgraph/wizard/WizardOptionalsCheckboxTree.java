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
package ca.phon.app.opgraph.wizard;

import ca.phon.opgraph.*;
import ca.phon.ui.tristatecheckbox.*;
import ca.phon.util.icons.*;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.util.*;

public class WizardOptionalsCheckboxTree extends TristateCheckBoxTree {
	
	private static final long serialVersionUID = 1768403681110021388L;

	private WizardExtension wizardExtension;

	public WizardOptionalsCheckboxTree() {
		this(new WizardExtension(new OpGraph()));
	}
	
	public WizardOptionalsCheckboxTree(WizardExtension wizardExtension) {
		super();
		this.wizardExtension = wizardExtension;
		
		init();
	}
	
	private void init() {
		((TristateCheckBoxTreeNode)getModel().getRoot()).setUserObject(wizardExtension.getWizardTitle());
		
		ImageIcon sessionIcon = IconManager.getInstance().getIcon(
				"mimetypes/text-xml", IconSize.SMALL);
		final ImageIcon folderIcon = IconManager.getInstance().getIcon("places/folder", IconSize.SMALL);
		
		final TristateCheckBoxTreeCellRenderer renderer = new TristateCheckBoxTreeCellRenderer();
		renderer.setLeafIcon(sessionIcon);
		renderer.setClosedIcon(folderIcon);
		renderer.setOpenIcon(folderIcon);
		
		final TristateCheckBoxTreeCellRenderer editorRenderer = new TristateCheckBoxTreeCellRenderer();
		editorRenderer.setLeafIcon(sessionIcon);
		editorRenderer.setClosedIcon(folderIcon);
		editorRenderer.setOpenIcon(folderIcon);
		final TristateCheckBoxTreeCellEditor editor = new TristateCheckBoxTreeCellEditor(this, editorRenderer);
		
		setCellRenderer(renderer);
		setCellEditor(editor);
		
		setupTree();
		
		expandRow(0);
	}
	
	private void setupTree() {
		final OpGraph graph = wizardExtension.getGraph();
		for(OpNode optionalNode:wizardExtension.getOptionalNodes()) {
			final List<OpNode> nodePath = graph.getNodePath(optionalNode.getId());
			
			TristateCheckBoxTreeNode parentTreeNode = (TristateCheckBoxTreeNode)getModel().getRoot();
			parentTreeNode.setEnablePartialCheck(false);
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
			super.setCheckingStateForPath(checkPath, TristateCheckBoxState.CHECKED);
			super.expandPath(checkPath.getParentPath());
		}
	}
	
	public TreePath graphPathToTreePath(List<OpNode> graphPath) {
		TreePath retVal = null;
		
		// graph path does not include root, add it
		TristateCheckBoxTreeNode currentNode = (TristateCheckBoxTreeNode)getRoot();
		retVal = new TreePath(currentNode);
		
		for(int i = 0; i < graphPath.size(); i++) {
			final OpNode opNode = graphPath.get(i);
			final CheckedOpNode treeNode = findTreeNode(opNode, currentNode);
			if(treeNode == null) {
				// not found, return null
				retVal = null;
				break;
			}
			retVal = retVal.pathByAddingChild(treeNode);
			currentNode = treeNode;
		}
		
		return retVal;
	}
	
	private CheckedOpNode findTreeNode(OpNode node) {
		TristateCheckBoxTreeNode currentNode = (TristateCheckBoxTreeNode)getModel().getRoot();
		return findTreeNode(node, currentNode);
	}
	
	private CheckedOpNode findTreeNode(OpNode node, TristateCheckBoxTreeNode parent) {
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

	public static class CheckedOpNode extends TristateCheckBoxTreeNode {
		
		private static final long serialVersionUID = 7282780647003827544L;

		private OpNode node;
		
		public CheckedOpNode(OpNode node) {
			super();
			this.node = node;
			setUserObject(node.getName());
			setEnablePartialCheck(false);
		}
		
		public OpNode getNode() { return node; }
		
	}
	
}
