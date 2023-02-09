package ca.phon.app.opgraph.wizard;

import bibliothek.gui.dock.common.*;
import ca.phon.app.opgraph.report.tree.ReportTree;
import ca.phon.app.opgraph.report.tree.ReportTreeNode;
import ca.phon.project.Project;
import ca.phon.ui.decorations.TitledPanel;

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

    private final ReportContentFactory reportContentFactory;

    private JTree tree;

    private JPanel selectedContentPanel;

    public ReportTreeView(ReportTree reportTree, ReportContentFactory reportContentFactory) {
        this(null, reportTree, reportContentFactory);
    }

    public ReportTreeView(Project project, ReportTree reportTree, ReportContentFactory reportContentFactory) {
        super();

        this.project = project;
        this.reportTree = reportTree;
        this.reportContentFactory = reportContentFactory;

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

        selectedContentPanel = new JPanel(new BorderLayout());
        selectedContentPanel.add(this.reportContentFactory.createComponentForNode(this.reportTree.getRoot()), BorderLayout.CENTER);

        tree = new JTree(new ReportTreeModel(reportTree));
        tree.setCellRenderer(new ReportTreeCellRenderer());
        tree.setRootVisible(true);
        final JScrollPane treeScroller = new JScrollPane(tree);

        final CWorkingArea work = control.createWorkingArea("work");
        DefaultMultipleCDockable selectedItemDockable = new DefaultMultipleCDockable(null, selectedContentPanel);
        selectedItemDockable.setTitleText("Log");
        selectedItemDockable.setCloseable(false);

        CGrid grid = new CGrid(control);
        grid.add(0, 0, 1, 3, new DefaultSingleCDockable("Report Outline", "Report Outline", treeScroller));
        grid.add( 1, 0, 3, 3, work);
        control.getContentArea().deploy(grid);

        work.show(selectedItemDockable);
        selectedItemDockable.toFront();
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

            // TODO set icon based on type report node type

            return retVal;
        }
    }

    public interface ReportContentFactory {
        public JComponent createComponentForNode(ReportTreeNode node);
    }

}
