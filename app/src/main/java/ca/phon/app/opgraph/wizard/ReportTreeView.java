package ca.phon.app.opgraph.wizard;

import ca.phon.app.opgraph.report.tree.ReportTree;
import ca.phon.app.opgraph.report.tree.ReportTreeNode;
import ca.phon.project.Project;
import org.jdesktop.swingx.JXTree;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Report tree viewer displayed at the end of node wizard dialogs such as the query and analysis wizards.
 */
public class ReportTreeView extends JPanel {

    private final ReportTree reportTree;

    private JXTree tree;

    public ReportTreeView(ReportTree reportTree) {
        super();

        this.reportTree = reportTree;

        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        tree = new JXTree(new ReportTreeModel());
        tree.setRootVisible(true);
        final JScrollPane treeScroller = new JScrollPane(tree);

        add(treeScroller, BorderLayout.WEST);
    }

    private class ReportTreeModel implements TreeModel {

        private final List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();

        @Override
        public Object getRoot() {
            return reportTree.getRoot();
        }

        @Override
        public Object getChild(Object parent, int index) {
            return ((ReportTreeNode)parent).getChildren().get(index);
        }

        @Override
        public int getChildCount(Object parent) {
            return ((ReportTreeNode)parent).getChildren().size();
        }

        @Override
        public boolean isLeaf(Object node) {
            return ((ReportTreeNode)node).getChildren().size() == 0;
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            return 0;
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {
            if(!listeners.contains(l))
                listeners.add(l);
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
            listeners.remove(l);
        }

    }

}
