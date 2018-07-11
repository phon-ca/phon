package ca.phon.app.opgraph.wizard;

import java.awt.Component;

import javax.swing.JTree;

import ca.phon.app.opgraph.report.tree.ReportTree;
import ca.phon.app.opgraph.report.tree.ReportTreeNode;
import ca.phon.app.opgraph.report.tree.SectionHeaderNode;
import ca.phon.app.opgraph.report.tree.TableNode;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxState;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTree;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeCellEditor;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeCellRenderer;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeModel;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeNode;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class ReportTableCheckboxTree extends TristateCheckBoxTree {
	
	public static TristateCheckBoxTreeModel createModel(ReportTree reportTree) {
		final TristateCheckBoxTreeNode root = new TristateCheckBoxTreeNode(reportTree.getRoot());
		root.setEnablePartialCheck(false);
		
		scanTree(reportTree.getRoot(), root);
		
		return new TristateCheckBoxTreeModel(root);
	}
	
	private static void scanTree(ReportTreeNode reportTreeNode, TristateCheckBoxTreeNode treeNode) {
		for(ReportTreeNode childNode:reportTreeNode) {
			if(childNode instanceof SectionHeaderNode) {
				final TristateCheckBoxTreeNode childTreeNode = new TristateCheckBoxTreeNode(childNode);
				childTreeNode.setCheckingState(TristateCheckBoxState.CHECKED);
				childTreeNode.setEnablePartialCheck(false);
				treeNode.add(childTreeNode);
				
				scanTree(childNode, childTreeNode);
			} else if(childNode instanceof TableNode) {
				final TristateCheckBoxTreeNode childTreeNode = new TristateCheckBoxTreeNode(childNode);
				childTreeNode.setCheckingState(TristateCheckBoxState.CHECKED);
				childTreeNode.setEnablePartialCheck(false);
				
				treeNode.add(childTreeNode);
			}
		}
	}
	
	public ReportTableCheckboxTree(ReportTree reportTree) {
		super(createModel(reportTree));
		
		init();
	}
	
	private void init() {
		setRootVisible(false);
		
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
