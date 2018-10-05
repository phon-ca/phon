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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class TristateCheckBoxTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -8686708364743866127L;
	
	private TristateCheckBoxState checkingState = TristateCheckBoxState.UNCHECKED;
	
	private boolean enablePartialCheck = false;
	
	private boolean propogateState = true;

	public TristateCheckBoxTreeNode() {
		this(new Object(), TristateCheckBoxState.UNCHECKED);
	}
	
	public TristateCheckBoxTreeNode(Object userObject) {
		this(userObject, TristateCheckBoxState.UNCHECKED);
	}

	public TristateCheckBoxTreeNode(Object userObject, TristateCheckBoxState checkingState) {
		this(userObject, checkingState, true);
	}
	
	public TristateCheckBoxTreeNode(Object userObject, TristateCheckBoxState checkingState, boolean allowsChildren) {
		this(userObject, checkingState, allowsChildren, true);
	}
	
	public TristateCheckBoxTreeNode(Object userObject, TristateCheckBoxState checkingState, boolean allowsChildren, boolean enablePartialCheck) {
		super(userObject, allowsChildren);
		this.checkingState = checkingState;
		this.enablePartialCheck = enablePartialCheck;
	}
	
	public TristateCheckBoxState getCheckingState() {
		return this.checkingState;
	}
	
	public boolean isPropogateState() {
		return this.propogateState;
	}
	
	public void setPropogateState(boolean propogateState) {
		this.propogateState = propogateState;
	}
	
	public void setCheckingState(TristateCheckBoxState checkboxState) {
		this.checkingState = checkboxState;
		
		if((checkboxState == TristateCheckBoxState.CHECKED ||
				checkboxState == TristateCheckBoxState.UNCHECKED) && isPropogateState()) {
			// set all children
			for(int i = 0; i <  getChildCount(); i++) {
				final TreeNode childNode = getChildAt(i);
				if(!(childNode instanceof TristateCheckBoxTreeNode)) continue;
				final TristateCheckBoxTreeNode checkboxNode = (TristateCheckBoxTreeNode)childNode;
				// child nodes must also update their children
				checkboxNode.setCheckingState(checkboxState);
			}
			
			TreeNode parentNode = getParent();
			while(parentNode != null) {
				if(parentNode instanceof TristateCheckBoxTreeNode) {
					final TristateCheckBoxTreeNode parentCheckboxNode = (TristateCheckBoxTreeNode)parentNode;
					final TristateCheckBoxState parentState = calculateParentState(parentCheckboxNode);
					// don't dig-in selection state
					parentCheckboxNode.checkingState = parentState;
				}
				parentNode = parentNode.getParent();
			}
		}
	}
	
	private TristateCheckBoxState calculateParentState(TristateCheckBoxTreeNode parent) {
		int numChecked = 0;
		int numParticallyChecked = 0;
		int numCheckBoxNodes = 0;
		
		for(int i = 0; i < parent.getChildCount(); i++) {
			final TreeNode childNode = parent.getChildAt(i);
			if(!(childNode instanceof TristateCheckBoxTreeNode)) continue;
			++numCheckBoxNodes;
			final TristateCheckBoxTreeNode childCheckboxNode = (TristateCheckBoxTreeNode)childNode;
			final TristateCheckBoxState childState = childCheckboxNode.getCheckingState();
			if(childState == TristateCheckBoxState.CHECKED)
				++numChecked;
			else if(childState == TristateCheckBoxState.PARTIALLY_CHECKED)
				++numParticallyChecked;
		}
		
		return (numChecked == 0 && numParticallyChecked == 0? TristateCheckBoxState.UNCHECKED : 
			(numChecked == numCheckBoxNodes ? TristateCheckBoxState.CHECKED : TristateCheckBoxState.PARTIALLY_CHECKED));
	}

	public boolean isEnablePartialCheck() {
		return enablePartialCheck;
	}

	public void setEnablePartialCheck(boolean enablePartialCheck) {
		this.enablePartialCheck = enablePartialCheck;
	}
	
}
