package ca.phon.app.opgraph.wizard;

import ca.phon.app.opgraph.report.ReportTableView;
import ca.phon.project.Project;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

/**
 * Open session editor with result highlighting when double-clicking result rows in a table.
 */
public class ResultTableMouseAdapter extends MouseInputAdapter {

    private final ReportTableView tableView;

    public ResultTableMouseAdapter(ReportTableView tableView) {
        super();
        this.tableView = tableView;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        final JTable table = tableView.getTable();
        if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
            final int selectedIndex = table.getSelectedRow();
            final int modelRow = table.convertRowIndexToModel(selectedIndex);
            ResultTableUtil.openEditorAtResult(tableView.getTableNode().getTable(), modelRow);
        }
    }

}
