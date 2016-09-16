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

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

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
