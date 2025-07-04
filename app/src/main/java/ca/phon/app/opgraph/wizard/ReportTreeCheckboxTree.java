/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.wizard;

import ca.phon.app.opgraph.report.tree.*;
import ca.phon.ui.tristatecheckbox.*;
import ca.phon.util.icons.*;

import javax.swing.*;
import java.awt.*;
import java.util.function.Predicate;

public class ReportTreeCheckboxTree extends TristateCheckBoxTree {
	
	public static TristateCheckBoxTreeModel createModel(ReportTree reportTree, Predicate<ReportTreeNode> nodePredicate) {
		final TristateCheckBoxTreeNode root = new TristateCheckBoxTreeNode(reportTree.getRoot());
		root.setEnablePartialCheck(false);
		root.setCheckingState(TristateCheckBoxState.CHECKED);
		
		scanTree(reportTree.getRoot(), root, nodePredicate);
		
		return new TristateCheckBoxTreeModel(root);
	}
	
	private static void scanTree(ReportTreeNode reportTreeNode, TristateCheckBoxTreeNode treeNode, Predicate<ReportTreeNode> nodePredicate) {
		for(ReportTreeNode childNode:reportTreeNode) {
			if(childNode instanceof SectionHeaderNode) {
				final TristateCheckBoxTreeNode childTreeNode = new TristateCheckBoxTreeNode(childNode);
				childTreeNode.setCheckingState(TristateCheckBoxState.CHECKED);
				childTreeNode.setEnablePartialCheck(false);
				
				scanTree(childNode, childTreeNode, nodePredicate);
				if(childTreeNode.getChildCount() > 0)
					treeNode.add(childTreeNode);
			} else if(nodePredicate.test(childNode)) {
				final TristateCheckBoxTreeNode childTreeNode = new TristateCheckBoxTreeNode(childNode);
				childTreeNode.setCheckingState(TristateCheckBoxState.CHECKED);
				childTreeNode.setEnablePartialCheck(false);
				
				treeNode.add(childTreeNode);
			}
		}
	}
	
	public ReportTreeCheckboxTree(ReportTree reportTree, Predicate<ReportTreeNode> nodePredicate) {
		super(createModel(reportTree, nodePredicate));
		
		init();
	}
	
	private void init() {
		setRootVisible(true);
		
		final ReportTreeNodeRenderer renderer = new ReportTreeNodeRenderer();
		renderer.setLeafIcon(IconManager.getInstance().getIcon("misc/table", IconSize.SMALL));
		
		final ReportTreeNodeRenderer editorRenderer = new ReportTreeNodeRenderer();
		editorRenderer.setLeafIcon(IconManager.getInstance().getIcon("misc/table", IconSize.SMALL));
		final TristateCheckBoxTreeCellEditor editor = new TristateCheckBoxTreeCellEditor(this, editorRenderer);
		
		setCellRenderer(renderer);
		setCellEditor(editor);
	}
	
	private class ReportTreeNodeRenderer extends TristateCheckBoxTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			TristateCheckBoxTreeNodePanel retVal = (TristateCheckBoxTreeNodePanel)super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			
			final TristateCheckBoxTreeNode treeNode = (TristateCheckBoxTreeNode)value;
			final ReportTreeNode reportTreeNode = (ReportTreeNode)treeNode.getUserObject();
			retVal.getLabel().setText(reportTreeNode.getTitle());
			
			return retVal;
		}
		
	}

}
