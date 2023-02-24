package ca.phon.app.opgraph.wizard.actions;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.LogUtil;
import ca.phon.app.opgraph.report.TableExporter;
import ca.phon.app.opgraph.report.tree.TableNode;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class SaveTableAsAction extends HookableAction {

    private final TableNode tableNode;

    private final TableExporter.TableExportType exportType;

    private final String initialFilename;

    public SaveTableAsAction(TableNode tableNode, String initialFilename, TableExporter.TableExportType exportType) {
        super();
        this.tableNode = tableNode;
        this.initialFilename = initialFilename;
        this.exportType = exportType;

        putValue(PhonUIAction.NAME, "Save table as " + (exportType == TableExporter.TableExportType.CSV ? "CSV" : "XLS"));
        final String iconName = exportType == TableExporter.TableExportType.CSV ? "mimetypes/text-x-generic" : "mimetypes/x-office-spreadsheet";
        final ImageIcon icn = IconManager.getInstance().getIcon(iconName, IconSize.SMALL);
        putValue(PhonUIAction.SMALL_ICON, icn);
    }

    @Override
    public void hookableActionPerformed(ActionEvent ae) {
        final SaveDialogProperties props = new SaveDialogProperties();
        props.setParentWindow(CommonModuleFrame.getCurrentFrame());
        props.setFileFilter(exportType == TableExporter.TableExportType.CSV ? FileFilter.csvFilter : FileFilter.excelFilter);
        String filename = initialFilename != null ? initialFilename : "untitled";
        filename += "." + (exportType == TableExporter.TableExportType.CSV ? "csv" : "xls");
        props.setInitialFile(filename);
        props.setCanCreateDirectories(true);
        props.setRunAsync(true);
        props.setListener((e) -> {
            if(e.getDialogResult() == NativeDialogEvent.OK_OPTION) {
                try {
                    TableExporter.writeTableToFile((DefaultTableDataSource) tableNode.getTable(), new File(e.getDialogData().toString()), exportType, "UTF-8", true);
                } catch (IOException ex) {
                    Toolkit.getDefaultToolkit().beep();
                    LogUtil.severe(ex);
                    CommonModuleFrame.getCurrentFrame().showErrorMessage("Unable to export table: " + ex.getMessage());
                }
            }
        });
        NativeDialogs.showSaveDialog(props);
    }

}
