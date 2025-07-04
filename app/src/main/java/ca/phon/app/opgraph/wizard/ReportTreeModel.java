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
        public TreeNode getParent() {
            final ReportTreeNode thisNode = (ReportTreeNode) getUserObject();
            if(thisNode.getParent() != null) {
                return new UIReportTreeNode(thisNode.getParent());
            } else {
                return null;
            }
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

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof UIReportTreeNode)) return false;
            UIReportTreeNode uiNode = (UIReportTreeNode) obj;
            final ReportTreeNode thisNode = (ReportTreeNode) getUserObject();
            final ReportTreeNode otherNode = (ReportTreeNode) uiNode.getUserObject();
            return thisNode == otherNode;
        }

        @Override
        public int hashCode() {
            return getUserObject().hashCode();
        }
    }

}
