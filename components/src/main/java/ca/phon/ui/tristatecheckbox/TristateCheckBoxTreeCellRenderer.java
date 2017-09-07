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

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;

public class TristateCheckBoxTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 924635241010609152L;
	
	protected TristateCheckBoxTreeNodePanel renderer;
	
	private final Color selectionForeground, selectionBackground;
	private final Color textForeground, textBackground;

	public TristateCheckBoxTreeCellRenderer() {
		super();
		
		createRenderer();
		
		selectionForeground = UIManager.getColor("Tree.selectionForeground");
		selectionBackground = UIManager.getColor("Tree.selectionBackground");
		textForeground = UIManager.getColor("Tree.textForeground");
		textBackground = UIManager.getColor("Tree.textBackground");
	}
	
	private void createRenderer() {
		this.renderer = new TristateCheckBoxTreeNodePanel();
		
		final Font fontValue = UIManager.getFont("Tree.font");
		if (fontValue != null) renderer.label.setFont(fontValue);
		
		final Boolean focusPainted =
				(Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon");
			renderer.checkBox.setFocusPainted(focusPainted != null && focusPainted);
	}
	
	public TristateCheckBoxTreeNodePanel getPanel() {
		return renderer;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		if(value instanceof TristateCheckBoxTreeNode) {
			final TristateCheckBoxTreeNode node = (TristateCheckBoxTreeNode)value;
			
			renderer.checkBox.setSelectionState(node.getCheckingState());
			renderer.checkBox.setEnablePartialCheck(node.isEnablePartialCheck());
			renderer.setEnabled(tree.isEnabled());

			if (sel) {
				renderer.setForeground(selectionForeground);
				renderer.setBackground(selectionBackground);
				renderer.label.setForeground(selectionForeground);
				renderer.label.setBackground(selectionBackground);
				renderer.checkBox.setBackground(selectionBackground);
			}
			else {
				renderer.setForeground(textForeground);
				renderer.setBackground(textBackground);
				renderer.label.setForeground(textForeground);
				renderer.label.setBackground(textBackground);
				renderer.checkBox.setBackground(textBackground);
			}
			
			final String txt = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);
			renderer.label.setText(txt);
			
			if(leaf) {
				renderer.label.setIcon(getLeafIcon());
			} else {
				if(tree.isExpanded(row)) {
					renderer.label.setIcon(getOpenIcon());
				} else {
					renderer.label.setIcon(getClosedIcon());
				}
			}
			
			if(!tree.isEnabled()) {
				renderer.label.setIcon(getDisabledIcon());
			}
			
			return renderer;
		} else {
			return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		}
	}
	
	public class TristateCheckBoxTreeNodePanel extends JPanel {
		
		private static final long serialVersionUID = 4953913375953950072L;

		TristateCheckBox checkBox;
		JLabel label;
		
		public TristateCheckBoxTreeNodePanel() {
			super();
			
			init();
		}
		
		public TristateCheckBox getCheckBox() {
			return this.checkBox;
		}
		
		public JLabel getLabel() {
			return this.label;
		}
		
		private void init() {
			setLayout(new BorderLayout());
			
			this.checkBox = new TristateCheckBox();
			this.checkBox.setMargin(new Insets(0, 0, 0, 0));
			this.label = new JLabel();

			add(this.checkBox, BorderLayout.WEST);
			add(this.label, BorderLayout.CENTER);
		}
	}
	
}
