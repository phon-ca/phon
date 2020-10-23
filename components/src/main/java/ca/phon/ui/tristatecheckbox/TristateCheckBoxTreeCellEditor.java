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

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

public class TristateCheckBoxTreeCellEditor extends AbstractCellEditor implements TreeCellEditor {
	
	private final TreeCellRenderer renderer;

	private JTree tree;
	
	public TristateCheckBoxTreeCellEditor(JTree tree) {
		this(tree, new TristateCheckBoxTreeCellRenderer());
	}
	
	public TristateCheckBoxTreeCellEditor(JTree tree, TreeCellRenderer renderer) {
		super();
		this.tree = tree;
		this.renderer = renderer;
	}
	
	@Override
	public boolean isCellEditable(final EventObject event) {
		if(!(event instanceof MouseEvent)) return false;
		final MouseEvent mouseEvent = (MouseEvent)event;
		
		final TreePath path =
			tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
		if (path == null) return false;

		final Object node = path.getLastPathComponent();
		if (!(node instanceof DefaultMutableTreeNode)) return false;
		final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;

		return treeNode instanceof TristateCheckBoxTreeNode;
	}
	
	@Override
	public Object getCellEditorValue() {
		if(renderer instanceof TristateCheckBoxTreeCellRenderer) {
			final TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel panel = 
					(TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel)((TristateCheckBoxTreeCellRenderer)renderer).getPanel();
			final TristateCheckBoxState state = panel.checkBox.getSelectionState();
			
			switch(state) {
			case UNCHECKED:
				return (panel.checkBox.isEnablePartialCheck() ? TristateCheckBoxState.PARTIALLY_CHECKED : TristateCheckBoxState.CHECKED);
				
			case PARTIALLY_CHECKED:
				return TristateCheckBoxState.CHECKED;
				
			case CHECKED:
				return TristateCheckBoxState.UNCHECKED;
				
			default:
				return state;
			}
		} else {
			return null;
		}
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
			boolean leaf, int row) {
		final Component editor = 
				renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
		
		if(editor instanceof TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel) {
			final TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel panel = 
					(TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel)editor;
			
			final ActionListener actionListener = (e) -> {
				if(stopCellEditing()) {
					fireEditingStopped();
				}
			};
			panel.checkBox.addActionListener(actionListener);
		}
		
		return editor;
	}
	
}
