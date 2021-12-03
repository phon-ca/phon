/*
 * Copyright (C) 2012-2020 Gregory Hedlund <https://www.phon.ca>
 * Copyright (C) 2012 Jason Gedge <http://www.gedge.ca>
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
package ca.phon.app.opgraph.editor;

import java.util.*;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.tree.*;

import ca.phon.app.opgraph.nodes.ScriptNode;
import ca.phon.opgraph.*;
import ca.phon.opgraph.extensions.*;

/**
 * Tree model for {@link OpGraph} outline.
 */
public class ScriptEditorTreeModel extends DefaultTreeModel {

	private Map<OpGraph, OpNode> compositeNodeMap = new HashMap<>();

	private final JTree tree;

	public ScriptEditorTreeModel(JTree tree, OpGraph graph) {
		super(new DefaultMutableTreeNode(graph));
		this.tree = tree;
		setupTree((DefaultMutableTreeNode)getRoot(), graph);
		graph.addGraphListener(graphListener);
	}

	private void setupTree(DefaultMutableTreeNode node, OpGraph graph) {
		for(OpNode opnode:graph.getVertices()) {
			final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(opnode);
			if(opnode instanceof ScriptNode) {
				node.add(childNode);
			} else if(childNode.getUserObject() instanceof CompositeNode) {
				final OpGraph childGraph = ((CompositeNode)childNode.getUserObject()).getGraph();
				setupTree(childNode, childGraph);
				node.add(childNode);
			}
			opnode.addNodeListener(nodeListener);
		}
	}

	public OpGraph getGraph() {
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode)getRoot();
		return (OpGraph)root.getUserObject();
	}

	protected DefaultMutableTreeNode getMutableNode(OpGraph graph) {
		if(graph == getGraph()) {
			return (DefaultMutableTreeNode)getRoot();
		} else {
			return findMutableNode((DefaultMutableTreeNode)getRoot(), graph);
		}
	}

	protected DefaultMutableTreeNode findMutableNode(DefaultMutableTreeNode parent, OpGraph graph) {
		DefaultMutableTreeNode retVal = null;
		for(int i = 0; i < parent.getChildCount(); i++) {
			final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)parent.getChildAt(i);
			if(childNode.getUserObject() instanceof CompositeNode) {
				final OpGraph childGraph = ((CompositeNode)childNode.getUserObject()).getGraph();
				if(childGraph == graph) {
					retVal = childNode;
					break;
				} else {
					retVal = findMutableNode(childNode, graph);
					if(retVal != null) break;
				}
			}
		}
		return retVal;
	}

	protected DefaultMutableTreeNode getMutableNode(OpNode node) {
		return findMutableNode((DefaultMutableTreeNode)getRoot(), node);
	}

	protected DefaultMutableTreeNode findMutableNode(DefaultMutableTreeNode parent, OpNode node) {
		DefaultMutableTreeNode retVal = null;
		for(int i = 0; i < parent.getChildCount(); i++) {
			final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)parent.getChildAt(i);
			if(childNode.getUserObject() == node) {
				return childNode;
			}
			if(childNode.getUserObject() instanceof CompositeNode) {
				retVal = findMutableNode(childNode, node);
				if(retVal != null) break;
			}
		}
		return retVal;
	}

	public void nodeWasRemoved(OpGraph graph, OpNode node) {
		final DefaultMutableTreeNode treeNode = getMutableNode(node);
		if(treeNode != null) {
			super.removeNodeFromParent(treeNode);
		}
	}

	public void nodeWasAdded(OpGraph graph, OpNode node) {
		final int nodeIdx = graph.getVertices()
				.stream().filter((v) -> v instanceof ScriptNode || v instanceof CompositeNode)
				.collect(Collectors.toList()).indexOf(node);
		if(nodeIdx < 0) return;

		final DefaultMutableTreeNode parentNode = getMutableNode(graph);
		final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(node);

		if(node instanceof ScriptNode || node instanceof  CompositeNode) {
			node.addNodeListener(nodeListener);

			if (node instanceof CompositeNode) {
				final OpGraph childGraph = ((CompositeNode) node).getGraph();
				childGraph.addGraphListener(graphListener);
				setupTree(childNode, childGraph);
			}

			parentNode.insert(childNode, nodeIdx);
			if (parentNode.getChildCount() == 1) {
				// notify structure has changed
				super.nodeStructureChanged(parentNode);
			}
			super.nodesWereInserted(parentNode, new int[]{nodeIdx});
		}
	}

	public void nodeChanged(OpNode node) {
		final DefaultMutableTreeNode treeNode = getMutableNode(node);
		super.nodeChanged(treeNode);
	}

	public void updateChildOrder(DefaultMutableTreeNode treeNode, OpGraph graph) {
		final TreeNode[] nodePath = super.getPathToRoot(treeNode);
		final TreePath treePath = new TreePath(nodePath);
		final Enumeration<TreePath> expandedPaths = tree.getExpandedDescendants(treePath);

		final Map<OpNode, DefaultMutableTreeNode> nodeMap = new HashMap<>();
		for(int i = 0; i < treeNode.getChildCount(); i++) {
			final DefaultMutableTreeNode childTreeNode = (DefaultMutableTreeNode)treeNode.getChildAt(i);
			final OpNode opNode = (OpNode)childTreeNode.getUserObject();
			nodeMap.put(opNode, childTreeNode);
		}

		treeNode.removeAllChildren();
		for(OpNode opNode:graph.getVertices()) {
			final DefaultMutableTreeNode childTreeNode = nodeMap.get(opNode);
			if(childTreeNode != null)
				treeNode.add(childTreeNode);
		}
		nodeStructureChanged(treeNode);

		while(expandedPaths != null && expandedPaths.hasMoreElements()) {
			tree.expandPath(expandedPaths.nextElement());
		}
	}

	private final OpNodeListener nodeListener = new OpNodeListener() {

		@Override
		public void nodePropertyChanged(OpNode node, String propertyName, Object oldValue, Object newValue) {
			if(propertyName.equals(OpNode.NAME_PROPERTY))
				nodeChanged(node);
		}

		@Override
		public void fieldAdded(OpNode node, InputField field) {
		}

		@Override
		public void fieldRemoved(OpNode node, InputField field) {
		}

		@Override
		public void fieldAdded(OpNode node, OutputField field) {
		}

		@Override
		public void fieldRemoved(OpNode node, OutputField field) {
		}

		@Override
		public void fieldRenamed(OpNode opNode, ContextualItem contextualItem) {

		}

	};

	private final OpGraphListener graphListener = new OpGraphListener() {

		@Override
		public void nodeRemoved(OpGraph graph, OpNode node) {
			nodeWasRemoved(graph, node);
		}

		@Override
		public void nodeAdded(OpGraph graph, OpNode node) {
			nodeWasAdded(graph, node);
		}

		@Override
		public void linkRemoved(OpGraph graph, OpLink link) {
			if(!graph.contains(link.getSource())
					|| !graph.contains(link.getDestination())) return;
			final DefaultMutableTreeNode treeNode = getMutableNode(graph);
			if(treeNode != null)
				updateChildOrder(treeNode, graph);
		}

		@Override
		public void linkAdded(OpGraph graph, OpLink link) {
			final DefaultMutableTreeNode treeNode = getMutableNode(graph);
			if(treeNode != null)
				updateChildOrder(treeNode, graph);
		}

		@Override
		public void nodeSwapped(OpGraph graph, OpNode oldNode, OpNode newNode) {

		}

	};

}
