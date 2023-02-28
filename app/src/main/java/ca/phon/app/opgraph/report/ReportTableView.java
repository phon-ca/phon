package ca.phon.app.opgraph.report;

import ca.phon.app.opgraph.report.tree.TableNode;
import ca.phon.app.opgraph.wizard.ResultTableMouseAdapter;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.Period;

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
        table.setSortable(false);
        table.setDefaultRenderer(Period.class, new AgeCellRenderer());
        table.addMouseListener(new ResultTableMouseAdapter(this));

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

    public JXTable getTable() {
        return table;
    }

    public TableNode getTableNode() {
        return tableNode;
    }

    private class AgeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel retVal = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if(value instanceof Period) {
                final Formatter<Period> ageFormatter = FormatterFactory.createFormatter(Period.class);
                retVal.setText(ageFormatter.format((Period) value));
            }

            return retVal;
        }
    }

}
