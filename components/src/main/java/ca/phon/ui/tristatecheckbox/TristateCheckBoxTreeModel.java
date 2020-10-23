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

import javax.swing.event.*;
import javax.swing.tree.*;

public class TristateCheckBoxTreeModel extends DefaultTreeModel {
	
	public static enum CheckingMode {
		SINGLE,
		SINGLE_PATH,
		MULTIPLE
	};
	
	private CheckingMode checkingMode = CheckingMode.MULTIPLE;

	public TristateCheckBoxTreeModel(TreeNode root) {
		super(root);
	}
	
	public CheckingMode getCheckingMode() {
		return this.checkingMode;
	}
	
	public void setCheckingMode(CheckingMode mode) {
		this.checkingMode = mode;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		if(newValue instanceof TristateCheckBoxState) {
			final TristateCheckBoxState state = (TristateCheckBoxState)newValue;
			
			final Object lastComp = path.getLastPathComponent();
			if(lastComp instanceof TristateCheckBoxTreeNode) {
				final TristateCheckBoxTreeNode node = (TristateCheckBoxTreeNode)lastComp;
				node.setCheckingState(state);
				
				if(getCheckingMode() == CheckingMode.SINGLE) {
					// un-check any checked nodes
					clearCheckedNodes((TreeNode)getRoot());
				} else if(getCheckingMode() == CheckingMode.SINGLE_PATH) {
					// un-check all siblings to this node
					TreePath parentPath = path.getParentPath();
					while(parentPath != null) {
						final TreeNode parentNode = (TreeNode)parentPath.getLastPathComponent();
						for(int i = 0; i < parentNode.getChildCount(); i++) {
							final TreeNode childNode = parentNode.getChildAt(i);
							clearCheckedNodes(childNode);
						}
						parentPath = parentPath.getParentPath();
					}
				}

				nodeCheckingChanged(node, state);
				TreePath parentPath = path.getParentPath();
				while(parentPath != null) {
					if(parentPath.getLastPathComponent() instanceof TristateCheckBoxTreeNode) {
						final TristateCheckBoxTreeNode parentNode = (TristateCheckBoxTreeNode)parentPath.getLastPathComponent();
						nodeChanged(parentNode);
					}
					parentPath = parentPath.getParentPath();
				}
			}
		} else {
			super.valueForPathChanged(path, newValue);
		}
	}
	
	private void nodeCheckingChanged(TreeNode node, TristateCheckBoxState state) {
		// copied from DefaultTreeModel
		TreeNode parent = node.getParent();

        if(parent != null) {
            int anIndex = parent.getIndex(node);
            if(anIndex != -1) {
                int[] cIndexs = new int[1];

                cIndexs[0] = anIndex;
                nodeCheckingChanged(parent, cIndexs, state);
            }
        } else if (node == getRoot()) {
            nodeCheckingChanged(node, null, state);
        }
	}
	
	private void nodeCheckingChanged(TreeNode node, int[] childIndices, TristateCheckBoxState state) {
		if (node != null) {
			if (childIndices != null) {
				int cCount = childIndices.length;

				if (cCount > 0) {
					Object[] cChildren = new Object[cCount];

					for (int counter = 0; counter < cCount; counter++)
						cChildren[counter] = node.getChildAt(childIndices[counter]);
					
					fireTreeNodeCheckingChanged(this, getPathToRoot(node), childIndices, cChildren, state);
				}
			} else if (node == getRoot()) {
				fireTreeNodeCheckingChanged(this, getPathToRoot(node), null, null, state);
			}
		}
	}
	
	public void fireTreeNodeCheckingChanged(Object source, Object[] path, int[] childIndices, Object[] children, TristateCheckBoxState state) {
		final TristateCheckBoxTreeModelEvent evt =
				new TristateCheckBoxTreeModelEvent(source, path, childIndices, children, state);
		for(TreeModelListener listener:super.getTreeModelListeners()) {
			listener.treeNodesChanged(evt);
		}
	}
	
	public void clearCheckedNodes(TreeNode root) {
		
	}
	
}
