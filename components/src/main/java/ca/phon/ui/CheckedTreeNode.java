/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ui;

import javax.swing.tree.DefaultMutableTreeNode;

public class CheckedTreeNode extends DefaultMutableTreeNode {
	
	private static final long serialVersionUID = 1244320402759867128L;

	public static enum SelectionMode {
		Single,
		DigIn
	};
	
	/** The selection Mode */
	protected SelectionMode selectionMode;
	/** Are we selected? */
	protected boolean isSelected;
	
	public CheckedTreeNode() {
		this(null);
	}
	
	public CheckedTreeNode(Object obj) {
		this(obj, true, false);
	}
	
	public CheckedTreeNode(Object obj, boolean allowsChildren, boolean isSelected) {
		super(obj, allowsChildren);
		this.isSelected = isSelected;
		setSelectionMode(SelectionMode.DigIn);
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
		
		if(selectionMode == SelectionMode.DigIn
				&& super.children != null) {
			for(int i = 0; i < children.size(); i++) {
				// get the child node
				CheckedTreeNode currentNode = 
					(CheckedTreeNode)children.get(i);
				currentNode.setSelected(isSelected);
			}
		}
	}

	public SelectionMode getSelectionMode() {
		return selectionMode;
	}

	public void setSelectionMode(SelectionMode selectionMode) {
		this.selectionMode = selectionMode;
	}

}
