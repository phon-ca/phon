package ca.phon.app.opgraph.wizard;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import ca.phon.app.opgraph.report.tree.ReportTree;
import ca.phon.app.opgraph.report.tree.ReportTreeNode;
import ca.phon.project.Project;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Report tree viewer displayed at the end of node wizard dialogs such as the query and analysis wizards.
 */
public class ReportTreeView extends JPanel {

    private CControl control;

    private Project project;

    private final ReportTree reportTree;

    private JTree tree;

    public ReportTreeView(ReportTree reportTree) {
        this(null, reportTree);
    }

    public ReportTreeView(Project project, ReportTree reportTree) {
        super();

        this.project = project;
        this.reportTree = reportTree;

        init();
    }

    public Project getProject() {
        return this.project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ReportTree getReportTree() {
        return this.reportTree;
    }

    private void init() {
        setLayout(new BorderLayout());

        control = new CControl();
        add(control.getContentArea(), BorderLayout.CENTER);

        tree = new JTree(new ReportTreeModel(reportTree));
        tree.setCellRenderer(new ReportTreeCellRenderer());
        tree.setRootVisible(true);
        final JScrollPane treeScroller = new JScrollPane(tree);

        final CWorkingArea work = control.createWorkingArea("work");

        CGrid grid = new CGrid(control);
        grid.add(0, 0, 1, 3, new DefaultSingleCDockable("Report Outline", "Report Outline", treeScroller));
        grid.add( 1, 0, 3, 3, work);
        control.getContentArea().deploy(grid);
    }

    public JTree getTree() {
        return this.tree;
    }

    public ReportTreeModel getTreeModel() {
        return (ReportTreeModel) this.tree.getModel();
    }

    public class ReportTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel retVal = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            retVal.setText(((ReportTreeNode)node.getUserObject()).getTitle());

            return retVal;
        }
    }

    public void updateTree() {
        tree.setModel(new ReportTreeModel(reportTree));
    }

//    public class ReportTreeModel implements TreeModel {
//
//        private final List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
//
//        @Override
//        public Object getRoot() {
//            return reportTree.getRoot();
//        }
//
//        @Override
//        public Object getChild(Object parent, int index) {
//            return ((ReportTreeNode)parent).getChildren().get(index);
//        }
//
//        @Override
//        public int getChildCount(Object parent) {
//            return ((ReportTreeNode)parent).getChildren().size();
//        }
//
//        @Override
//        public boolean isLeaf(Object node) {
//            return ((ReportTreeNode)node).getChildren().size() == 0;
//        }
//
//        @Override
//        public void valueForPathChanged(TreePath path, Object newValue) {
//        }
//
//        @Override
//        public int getIndexOfChild(Object parent, Object child) {
//            return ((ReportTreeNode)parent).getChildren().indexOf(child);
//        }
//
//        public void fireTreeStructureChanged() {
//            for(TreeModelListener listener:listeners) {
//                listener.treeStructureChanged(new TreeModelEvent(tree, reportTree.getRoot().getPath().getPath()));
//            }
//        }
//
//        public void fireReportNodeAdded(ReportTreeNode parent, ReportTreeNode child) {
//            final int[] indicies = new int[]{parent.getChildren().indexOf(child)};
//            final Object[] children = new Object[]{child};
//
//            for(TreeModelListener listener:listeners) {
//                listener.treeNodesInserted(new TreeModelEvent(tree, parent.getPath().getPath(), indicies, children));
//            }
//        }
//
//        @Override
//        public void addTreeModelListener(TreeModelListener l) {
//            if(!listeners.contains(l))
//                listeners.add(l);
//        }
//
//        @Override
//        public void removeTreeModelListener(TreeModelListener l) {
//            listeners.remove(l);
//        }
//
//    }

}
