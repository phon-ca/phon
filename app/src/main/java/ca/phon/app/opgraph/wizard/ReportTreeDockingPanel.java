package ca.phon.app.opgraph.wizard;

import bibliothek.gui.dock.common.*;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.action.CButton;
import bibliothek.gui.dock.common.event.CVetoClosingEvent;
import bibliothek.gui.dock.common.event.CVetoClosingListener;
import ca.phon.app.opgraph.report.ReportTableView;
import ca.phon.app.opgraph.report.TableExporter;
import ca.phon.app.opgraph.report.tree.ReportTree;
import ca.phon.app.opgraph.report.tree.ReportTreeNode;
import ca.phon.app.opgraph.report.tree.SectionHeaderNode;
import ca.phon.app.opgraph.report.tree.TableNode;
import ca.phon.app.opgraph.wizard.actions.SaveTableAsAction;
import ca.phon.project.Project;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import org.jdesktop.swingx.JXTree;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Report tree viewer displayed at the end of node wizard dialogs such as the query and analysis wizards.
 */
public class ReportTreeDockingPanel extends JPanel {

    private CControl control;

    private CWorkingArea workingArea;

    private final Map<String, MultipleCDockable> dockables = new LinkedHashMap<>();

    private Project project;

    private final ReportTree reportTree;

    private final ReportContentFactory reportContentFactory;

    private JXTree tree;

    public ReportTreeDockingPanel(ReportTree reportTree, ReportContentFactory reportContentFactory) {
        this(null, reportTree, reportContentFactory);
    }

    public ReportTreeDockingPanel(Project project, ReportTree reportTree, ReportContentFactory reportContentFactory) {
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
                            openContentInNewTab(tblNode.getPath().toString(), tblIcn, true, new ReportTableView(tblNode), new CSaveTableAsButton(tblNode, TableExporter.TableExportType.CSV), new CSaveTableAsButton(tblNode, TableExporter.TableExportType.EXCEL));
                        }
                    }
                }
            }
        });
        final JScrollPane treeScroller = new JScrollPane(tree);

        workingArea = control.createWorkingArea("work");

        CGrid grid = new CGrid(control);
        grid.add(0, 0, 1, 3, new DefaultSingleCDockable("Report Outline", IconManager.getInstance().getIcon("misc/view-list-tree", IconSize.SMALL), "Report Outline", treeScroller));
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
                final ImageIcon tblIcn = IconManager.getInstance().getIcon("misc/table", IconSize.SMALL);
                retVal.setIcon(tblIcn);
            } else if(node.getUserObject() instanceof SectionHeaderNode) {
                final ImageIcon sectionIcn = IconManager.getInstance().createGlyphIcon('\u00a7',
                        FontPreferences.getUIFont().deriveFont(Font.BOLD, 14.0f), Color.black, Color.white);
                retVal.setIcon(sectionIcn);
            }

            return retVal;
        }
    }

    public interface ReportContentFactory {
        public JComponent createComponentForNode(ReportTreeNode node);
    }

    /* Dockable actions for tables */
    private class CSaveTableAsButton extends CButton {

        private final TableNode tableNode;

        private final TableExporter.TableExportType exportType;

        public CSaveTableAsButton(TableNode tableNode, TableExporter.TableExportType exportType) {
            super(exportType == TableExporter.TableExportType.CSV ? "Save table as CSV..." : "Save table as XLS...",
                    IconManager.getInstance().getIcon(exportType == TableExporter.TableExportType.CSV ? "mimetypes/text-x-generic" : "mimetypes/x-office-spreadsheet", IconSize.SMALL));
            this.tableNode = tableNode;
            this.exportType = exportType;
        }

        @Override
        protected void action() {
            final SaveTableAsAction saveTableAsAction = new SaveTableAsAction(tableNode, tableNode.getTitle(), exportType);
            saveTableAsAction.actionPerformed(new ActionEvent(this, -1, "saveTableAs"));
        }

    }

}
