package ca.phon.app.opgraph.wizard;

import bibliothek.gui.DockStation;
import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import bibliothek.gui.dock.common.*;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.action.CButton;
import bibliothek.gui.dock.common.action.core.CommonDockAction;
import bibliothek.gui.dock.common.event.CVetoClosingEvent;
import bibliothek.gui.dock.common.event.CVetoClosingListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.action.CDecorateableAction;
import ca.phon.app.opgraph.report.ReportTableView;
import ca.phon.app.opgraph.report.tree.ReportTree;
import ca.phon.app.opgraph.report.tree.ReportTreeNode;
import ca.phon.app.opgraph.report.tree.TableNode;
import ca.phon.app.opgraph.wizard.actions.SaveTablesToFolderAction;
import ca.phon.app.opgraph.wizard.actions.SaveTablesToWorkbookAction;
import ca.phon.project.Project;
import ca.phon.ui.ButtonPopup;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import org.jdesktop.swingx.JXTree;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Report tree viewer displayed at the end of node wizard dialogs such as the query and analysis wizards.
 */
public class ReportTreeView extends JPanel {

    private CControl control;

    private CWorkingArea workingArea;

    private final Map<String, MultipleCDockable> dockables = new LinkedHashMap<>();

    private Project project;

    private final ReportTree reportTree;

    private final ReportContentFactory reportContentFactory;

    private JXTree tree;

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

        tree = new JXTree(new ReportTreeModel(reportTree));
        tree.setCellRenderer(new ReportTreeCellRenderer());
        tree.setRootVisible(true);

        final Icon tblIcn = IconManager.getInstance().getIcon("misc/table", IconSize.SMALL);
        tree.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    final int row = tree.getRowForLocation(e.getX(), e.getY());
                    final TreePath tp = tree.getPathForRow(row);
                    if(tp != null) {
                        DefaultMutableTreeNode lastNode = (DefaultMutableTreeNode) tp.getLastPathComponent();
                        if(lastNode.isLeaf() && lastNode.getUserObject() instanceof TableNode) {
                            final TableNode tblNode = (TableNode) lastNode.getUserObject();
                            openContentInNewTab(tblNode.getPath().toString(), tblIcn, true, new ReportTableView(tblNode));
                        }
                    }
                }
            }
        });
        final JScrollPane treeScroller = new JScrollPane(tree);

        workingArea = control.createWorkingArea("work");

        CGrid grid = new CGrid(control);
        grid.add(0, 0, 1, 3, new DefaultSingleCDockable("Report Outline", "Report Outline", treeScroller));
        grid.add( 1, 0, 3, 3, workingArea);
        control.getContentArea().deploy(grid);
    }

    public void closeTab(String title) {
        var dockable = dockables.get(title);
        if(dockable != null)
            dockable.setVisible(false);
    }

    public void focusTab(String title) {
        var dockable = dockables.get(title);
        if(dockable != null)
            workingArea.show(dockable);
    }

    public DefaultMultipleCDockable openContentInNewTab(String title, Icon icon, boolean isClosable, JComponent component, CAction... actions) {
        DefaultMultipleCDockable dockable = (DefaultMultipleCDockable) dockables.get(title);
        if(dockable == null) {
            dockable = new DefaultMultipleCDockable(null, icon, title, component, actions);
            dockable.setCloseable(isClosable);
            if(isClosable) {
                dockable.addVetoClosingListener(new CVetoClosingListener() {
                    @Override
                    public void closing(CVetoClosingEvent cVetoClosingEvent) {}

                    @Override
                    public void closed(CVetoClosingEvent cVetoClosingEvent) {
                        dockables.remove(title);
                        cVetoClosingEvent.getDockable(0).removeVetoClosingListener(this);
                    }
                });
            }
            dockable.setExternalizable(false);
            workingArea.show(dockable);

            dockables.put(title, dockable);
        }
        dockable.toFront();
        return dockable;
    }

    public JXTree getTree() {
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

            if(node.isLeaf() && node.getUserObject() instanceof TableNode) {
                // TODO set icon
            }

            return retVal;
        }
    }

    public interface ReportContentFactory {
        public JComponent createComponentForNode(ReportTreeNode node);
    }

}
