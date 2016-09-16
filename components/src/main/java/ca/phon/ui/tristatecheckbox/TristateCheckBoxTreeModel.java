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
package ca.phon.ui.tristatecheckbox;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class TristateCheckBoxTreeModel extends DefaultTreeModel {

	public TristateCheckBoxTreeModel(TreeNode root) {
		super(root);
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		if(newValue instanceof TristateCheckBoxState) {
			final TristateCheckBoxState state = (TristateCheckBoxState)newValue;
			
			final Object lastComp = path.getLastPathComponent();
			if(lastComp instanceof TristateCheckBoxTreeNode) {
				final TristateCheckBoxTreeNode node = (TristateCheckBoxTreeNode)lastComp;
				node.setCheckingState(state);

				nodeChanged(node);
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
	
}
