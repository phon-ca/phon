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

import java.awt.Component;
import java.util.*;

import javax.swing.JTree;
import javax.swing.tree.*;

import ca.gedge.opgraph.*;
import ca.gedge.opgraph.nodes.general.MacroNode;
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
