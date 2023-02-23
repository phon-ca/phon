package ca.phon.app.opgraph.wizard;

import ca.phon.app.log.LogUtil;
import ca.phon.app.opgraph.report.tree.ReportTree;
import ca.phon.app.opgraph.report.tree.ReportTreeNode;
import ca.phon.app.opgraph.report.tree.SectionHeaderNode;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxState;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeNode;
import ca.phon.util.PrefHelper;
import ca.phon.worker.PhonWorker;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ReportTreeToHTMLDialog extends CommonModuleFrame {

    private DialogHeader header;

    private final static String OPEN_AFTER_EXPORT_PROP = ReportTreeToHTMLDialog.class.getName() + ".openAfterExport";
    private final static boolean DEFAULT_OPEN_AFTER_EXPORT = Boolean.TRUE;
    private boolean openAfterExport = PrefHelper.getBoolean(OPEN_AFTER_EXPORT_PROP, DEFAULT_OPEN_AFTER_EXPORT);

    private JCheckBox openAfterExportBox;

    private ReportTableCheckboxTree reportTableCheckboxTree;

    private JXBusyLabel busyLabel;
    private ReportTree reportTree;

    private JButton exportButton;

    private JButton cancelButton;

    private NodeWizard nodeWizard;

    public ReportTreeToHTMLDialog(NodeWizard nodeWizard, ReportTree reportTree) {
        super("Generate HTML Report");

        this.nodeWizard = nodeWizard;
        this.reportTree = reportTree;

        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        header = new DialogHeader("Generate HTML Report", "Use checkboxes to select sections for inclusion in report");
        add(header, BorderLayout.NORTH);

        reportTableCheckboxTree = new ReportTableCheckboxTree(reportTree, true);
        reportTableCheckboxTree.expandAll();

        final JScrollPane scroller = new JScrollPane(reportTableCheckboxTree);

        busyLabel = new JXBusyLabel(new Dimension(16, 16));

        add(scroller, BorderLayout.CENTER);

        final PhonUIAction<Void> cancelAct = PhonUIAction.runnable(this::onCancel);
        cancelAct.putValue(PhonUIAction.NAME, "Close");
        cancelAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Cancel export and close window");
        cancelButton = new JButton(cancelAct);

        final PhonUIAction<Void> exportAct = PhonUIAction.runnable(this::onExport);
        exportAct.putValue(PhonUIAction.NAME, "Generate HTML");
        exportAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Generate HTML Report");
        exportButton = new JButton(exportAct);

        openAfterExportBox = new JCheckBox("Open after saving");
        openAfterExportBox.setSelected(openAfterExport);
        openAfterExportBox.addActionListener( (e) -> {
            openAfterExport = openAfterExportBox.isSelected();
            PrefHelper.getUserPreferences().putBoolean(OPEN_AFTER_EXPORT_PROP, openAfterExport);
        } );

        final JComponent exportBtnPanel = new JPanel(new HorizontalLayout(3));
        exportBtnPanel.add(busyLabel);
        exportBtnPanel.add(exportButton);
        final JComponent buttonBar = ButtonBarBuilder.buildOkCancelBar(exportBtnPanel, cancelButton);
        final JPanel bottomPanel = new JPanel(new VerticalLayout());
        bottomPanel.add(openAfterExportBox);
        bottomPanel.add(buttonBar);

        add(bottomPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(exportButton);
    }

    private void onCancel() {
        this.setVisible(false);
        this.dispose();
    }

    private ReportTree getSelectedReportTree() {
        final ReportTree retVal = this.reportTree.createFilteredTree((treeNode) -> {
            final TreePath treePath = reportTableCheckboxTree.userPathToTreePath(treeNode.getPath().getPath());
            if(treePath == null) return false;
            final TristateCheckBoxTreeNode tristateCheckBoxTreeNode = (TristateCheckBoxTreeNode) treePath.getLastPathComponent();
            if(tristateCheckBoxTreeNode == null) return false;
            if(tristateCheckBoxTreeNode.getCheckingState() == TristateCheckBoxState.CHECKED || tristateCheckBoxTreeNode.getCheckingState() == TristateCheckBoxState.PARTIALLY_CHECKED)
                return true;
            else
                return false;
        });
        return retVal;
    }

    private void onExport() {
        if(this.nodeWizard.htmlReportAvailable()) {
//            this.nodeWizard.closeHTMlReport();
        }
        final ReportTree selectedReportTree = getSelectedReportTree();
        PhonWorker.invokeOnNewWorker(() -> {
            try {
                final File tempFile = this.nodeWizard.generateHTMLReport(selectedReportTree);
                this.nodeWizard.loadHTMLReport(tempFile);
            } catch (NodeWizardReportException | IOException e) {
                LogUtil.severe(e);
                Toolkit.getDefaultToolkit().beep();
            }
        });
    }

    public void showDialog() {
        pack();
        setSize(512, 384);
        setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
        setVisible(true);
    }

}
