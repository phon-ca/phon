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

import javax.swing.*;
import javax.swing.tree.*;

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
