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
package ca.phon.ui.tristatecheckbox;

import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

/**
 * Tree component with checkbox controls.
 * 
 */
public class TristateCheckBoxTree extends JTree {

	private static final long serialVersionUID = 1360119840510903927L;
	
	public TristateCheckBoxTree() {
		this(new TristateCheckBoxTreeNode());
	}
	
	public TristateCheckBoxTree(TristateCheckBoxTreeNode root) {
		this(new TristateCheckBoxTreeModel(root));
	}
	
	public TristateCheckBoxTree(TristateCheckBoxTreeModel model) {
		super(model);
		
		setCellRenderer(new TristateCheckBoxTreeCellRenderer());
		setCellEditor(new TristateCheckBoxTreeCellEditor(this));
		
		setEditable(true);
		setShowsRootHandles(true);
	}
	
	/**
	 * Set the checking state for given path.
	 * 
	 * @param path
	 * @param state
	 * 
	 */
	public void setCheckingStateForPath(TreePath path, TristateCheckBoxState state) {
		getModel().valueForPathChanged(path, state);
	}
	
	/**
	 * Set state for multiple paths
	 * 
	 * @param pathItr
	 * @param state
	 */
	public void setCheckingStateForPaths(Iterable<TreePath> pathItr, TristateCheckBoxState state) {
		final Iterator<TreePath> iterator = pathItr.iterator();
		while(iterator.hasNext()) {
			final TreePath path = iterator.next();
			setCheckingStateForPath(path, state);
		}
	}
	
	/**
	 * Get a list of all nodes with a checked state.
	 * 
	 * @return list of all checked nodes
	 */
	public List<TreePath> getCheckedPaths() {
		return getPathsWithState(TristateCheckBoxState.CHECKED);
	}
	
	/**
	 * Get a list of all nodes which are partially checked.
	 * 
	 * @return list of all partially checked paths
	 */
	public List<TreePath> getPartiallyCheckedPaths() {
		return getPathsWithState(TristateCheckBoxState.PARTIALLY_CHECKED);
	}
	
	/**
	 * Get a list of all unchecked nodes
	 * 
	 * @return list of all unchecked nodes
	 */
	public List<TreePath> getUncheckedPaths() {
		return getPathsWithState(TristateCheckBoxState.UNCHECKED);
	}
	
	/**
	 * Get a list of all nodes with the specified state.
	 * 
	 * @param state
	 * @return list of all nodes with specified state
	 */
	public List<TreePath> getPathsWithState(TristateCheckBoxState state) {
		final TreePath path = new TreePath(getRoot());
		return getPathsWithState(path, state);
	}
	
	/**
	 * Get a list of all nodes with the specified state.
	 * 
	 * @param path
	 * @param state
	 * @return list of all nodes with specified state
	 */
	public List<TreePath> getPathsWithState(TreePath path, TristateCheckBoxState state) {
		List<TreePath> retVal = new ArrayList<>();
		
		final Object pathObj = path.getLastPathComponent();
		if(pathObj instanceof TristateCheckBoxTreeNode) {
			final TristateCheckBoxTreeNode node = (TristateCheckBoxTreeNode)pathObj;
			if(node.getCheckingState() == state) {
				retVal.add(path);
			}
			if(node.getChildCount() > 0) {
				// check children
				for(int i = 0; i < node.getChildCount(); i++) {
					final TreeNode childNode = node.getChildAt(i);
					if(!(childNode instanceof TristateCheckBoxTreeNode)) continue;
					final TristateCheckBoxTreeNode childCheckboxNode = (TristateCheckBoxTreeNode)childNode;
					final TreePath childPath = path.pathByAddingChild(childCheckboxNode);
					retVal.addAll(getPathsWithState(childPath, state));
				}
			}
		}
		
		return retVal;
	}
	
	/**
	 * Is the path checked.
	 * 
	 * @param path
	 * @return boolean
	 */
	public boolean isPathChecked(TreePath path) {
		return (getCheckingStateForPath(path) == TristateCheckBoxState.CHECKED);
	}
	
	/**
	 * Is the path partially checked
	 * 
	 * @param path
	 * @return boolean
	 */
	public boolean isPathPartiallyChecked(TreePath path) {
		return (getCheckingStateForPath(path) == TristateCheckBoxState.PARTIALLY_CHECKED);
	}
	
	/**
	 * Is the path unchecked
	 * 
	 * @param path
	 * @return boolean
	 */
	public boolean isPathUnchecked(TreePath path) {
		return (getCheckingStateForPath(path) == TristateCheckBoxState.UNCHECKED);
	}
	
	/**
	 * Return path checkbox state.
	 * 
	 * @param path
	 * @return checkbox state for path or {@link TristateCheckBoxState.UNCHECKED} if not
	 *  a checkbox tree node
	 */
	public TristateCheckBoxState getCheckingStateForPath(TreePath path) {
		TristateCheckBoxState retVal = TristateCheckBoxState.UNCHECKED;
		final Object lastComp = path.getLastPathComponent();
		if(lastComp instanceof TristateCheckBoxTreeNode) {
			final TristateCheckBoxTreeNode node = (TristateCheckBoxTreeNode)lastComp;
			retVal = node.getCheckingState();
		}
		return retVal;
	}
	
	/**
	 * Find the tree path, if any, which contains the give
	 * user object path.
	 * 
	 * @param userPath
	 * @return the tree path if found, <code>null</code> otherwise
	 */
	public TreePath userPathToTreePath(Object[] userPath) {
		int pathLength = userPath.length;
		TreePath treePath = null;
		
		final Object rootObj = getRoot();
		if(pathLength > 0 && rootObj instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)rootObj;
			
			if(currentNode.getUserObject().equals(userPath[0])) {
				treePath = new TreePath(currentNode);
				
				for(int i = 1; i < userPath.length; i++) {
					TreeNode nextNode = findNodeWithUserObject(currentNode, userPath[i]);
					if(nextNode == null) {
						treePath = null;
						break;
					}
					treePath = treePath.pathByAddingChild(nextNode);
					currentNode = (DefaultMutableTreeNode)nextNode;
				}
			}
			
		}
		
		return treePath;
	}
	
	public TreeNode findNodeWithUserObject(TreeNode rootNode, Object userObject) {
		TreeNode retVal = null;
		for(int i = 0; i < rootNode.getChildCount(); i++) {
			final TreeNode childNode = rootNode.getChildAt(i);
			if(childNode instanceof DefaultMutableTreeNode) {
				final DefaultMutableTreeNode mutableNode = (DefaultMutableTreeNode)childNode;
				
				if(mutableNode.getUserObject().equals(userObject)) {
					retVal = childNode;
					break;
				}
			}
		}
		return retVal;
	}
	
	/**
	 * Get root of tree.
	 * 
	 * @return root node
	 */
	public TreeNode getRoot() {
		return (TreeNode)getModel().getRoot();
	}
	
	public void expandAll() {
		expandAll(new TreePath(getRoot()));
	}
	
	public void expandAll(TreePath path) {
		if(isCollapsed(path)) {
			expandPath(path);
		}
		if(path.getLastPathComponent() instanceof TreeNode) {
			final TreeNode parentNode = (TreeNode)path.getLastPathComponent();
			if(parentNode.getChildCount() > 0) {
				for(int i = 0; i < parentNode.getChildCount(); i++) {
					final TreePath childPath = path.pathByAddingChild(parentNode.getChildAt(i));
					expandAll(childPath);
				}
			}
		}
	}
}
