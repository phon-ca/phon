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

import java.util.List;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.tree.TreePath;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxState;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTree;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeCellEditor;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeCellRenderer;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeNode;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

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
