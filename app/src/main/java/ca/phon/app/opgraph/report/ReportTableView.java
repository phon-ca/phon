package ca.phon.app.opgraph.report;

import ca.phon.app.opgraph.report.tree.TableNode;
import ca.phon.app.opgraph.wizard.ReportTreeView;
import ca.phon.ipamap2.IPAMap;
import ca.phon.ui.fonts.FontPreferences;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ReportTableView extends JPanel {

    private JToolBar toolbar;

    private JXTable table;

    private final TableNode tableNode;

    public ReportTableView(TableNode tableNode) {
        super();

        this.tableNode = tableNode;

        init();
    }

    private void init() {
        setLayout(new BorderLayout());

        final TableNodeTableModel tableModel = new TableNodeTableModel(tableNode);
        table = new JXTable(tableModel);
        table.setColumnControlVisible(true);

        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            final String colName = tableModel.getColumnName(i);
            var tblColExt = table.getColumnExt(table.convertColumnIndexToView(i));
            if (tableNode.isIncludeColumns()) {
                tblColExt.setVisible(tableNode.getColumns().size() == 0 || tableNode.getColumns().contains(colName));
            } else {
                tblColExt.setVisible(!tableNode.getColumns().contains(colName));
            }
        }
        SwingUtilities.invokeLater(table::packAll);

        JScrollPane scroller = new JScrollPane(table);
        add(scroller, BorderLayout.CENTER);
    }

}
