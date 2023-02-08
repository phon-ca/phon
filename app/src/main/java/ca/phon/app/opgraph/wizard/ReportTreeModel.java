package ca.phon.app.opgraph.wizard;

import ca.phon.app.opgraph.report.tree.ReportTree;
import ca.phon.app.opgraph.report.tree.ReportTreeNode;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;

public class ReportTreeModel extends DefaultTreeModel {
    public ReportTreeModel(ReportTree reportTree) {
        super(new UIReportTreeNode(reportTree.getRoot()));
    }

    public static class UIReportTreeNode extends DefaultMutableTreeNode {

        public UIReportTreeNode(ReportTreeNode reportTreeNode) {
            super(reportTreeNode);
        }

        @Override
        public TreeNode getChildAt(int index) {
            final ReportTreeNode thisNode = (ReportTreeNode) getUserObject();
            final ReportTreeNode childNode = thisNode.getChildren().get(index);
            return new UIReportTreeNode(childNode);
        }

        @Override
        public int getChildCount() {
            return ((ReportTreeNode)getUserObject()).getChildren().size();
        }

        @Override
        public int getIndex(TreeNode aChild) {
            for(int i = 0; i < getChildCount(); i++) {
                final ReportTreeNode childNode = ((ReportTreeNode)getUserObject()).getChildren().get(i);
                if(((DefaultMutableTreeNode)aChild).getUserObject() == childNode)
                    return i;
            }
            return -1;
        }

    }

}
