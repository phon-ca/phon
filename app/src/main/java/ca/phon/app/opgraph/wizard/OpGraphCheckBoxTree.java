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

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.tree.*;

import ca.phon.opgraph.*;
import ca.phon.opgraph.nodes.general.*;
import ca.phon.ui.tristatecheckbox.*;

public class OpGraphCheckBoxTree extends TristateCheckBoxTree {

	public static TristateCheckBoxTreeModel createModel(OpGraph graph, boolean propogateState) {
		final TristateCheckBoxTreeNode root = new TristateCheckBoxTreeNode(graph);
		root.setEnablePartialCheck(false);
		root.setPropogateState(propogateState);
		setupTree(root, graph, propogateState);
		
		return new TristateCheckBoxTreeModel(root);
	}
	
	private static void setupTree(TristateCheckBoxTreeNode parent, OpGraph graph, boolean propogateState) {
		final Iterator<OpNode> nodeItr = graph.iterator();
		while(nodeItr.hasNext()) {
			final OpNode node = nodeItr.next();
			final TristateCheckBoxTreeNode treeNode = new TristateCheckBoxTreeNode(node);
			treeNode.setEnablePartialCheck(false);
			treeNode.setPropogateState(propogateState);
			parent.add(treeNode);
			
			if(node instanceof MacroNode) {
				final MacroNode macroNode = (MacroNode)node;
				setupTree(treeNode, macroNode.getGraph(), propogateState);
			}
		}
	}
	
	public OpGraphCheckBoxTree(OpGraph graph) {
		this(graph, true);
	}
	
	public OpGraphCheckBoxTree(OpGraph graph, boolean propogateState) {
		super(createModel(graph, propogateState));
		
		setCellRenderer(new OpGraphTreeRenderer());
		setCellEditor(new OpGraphTreeEditor(this));
	}
	
	/**
	 * Return path or null if node not found
	 * 
	 * @param node
	 * @return
	 */
	public TreePath treePathForNode(OpNode node) {
		final OpGraph graph = (OpGraph)((TristateCheckBoxTreeNode)getRoot()).getUserObject();
		
		final List<OpNode> path = graph.getNodePath(node.getId());
		
		TreeNode currentNode = getRoot();
		TreePath treePath = new TreePath(getRoot());
		
		for(OpNode childNode:path) {
			for(int i = 0; i < currentNode.getChildCount(); i++) {
				final TreeNode childTreeNode = currentNode.getChildAt(i);
				if(childTreeNode instanceof DefaultMutableTreeNode) {
					final DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode)childTreeNode;
					if(mutableTreeNode.getUserObject() == childNode) {
						treePath = treePath.pathByAddingChild(childTreeNode);
						currentNode = childTreeNode;
						break;
					}
				}
			}
		}
		
		return treePath;
	}
	
	private class OpGraphTreeRenderer extends TristateCheckBoxTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			Component retVal = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			
			final String text = 
					(value == getRoot() ? "root" : ((OpNode)((TristateCheckBoxTreeNode)value).getUserObject()).getName());
			if(retVal instanceof TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel) {
				((TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel)retVal).getLabel().setText(text);
			}
			
			return retVal;
		}
		
	}
	
	private class OpGraphTreeEditor extends TristateCheckBoxTreeCellEditor {

		public OpGraphTreeEditor(JTree tree) {
			super(tree);
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
				boolean leaf, int row) {
			Component retVal = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
			
			final String text = 
					(value == getRoot() ? "root" : ((OpNode)((TristateCheckBoxTreeNode)value).getUserObject()).getName());
			if(retVal instanceof TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel) {
				((TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel)retVal).getLabel().setText(text);
			}
			
			return retVal;
		}
		
	}
	
}
