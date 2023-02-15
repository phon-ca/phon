package ca.phon.app.opgraph.report;

import ca.phon.app.opgraph.report.tree.TableNode;
import ca.phon.query.report.datasource.DefaultTableDataSource;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class TableNodeTableModel extends AbstractTableModel {

    private final TableNode tableNode;

    public TableNodeTableModel(TableNode tableNode) {
        super();

        this.tableNode = tableNode;
    }

    @Override
    public int getRowCount() {
        return tableNode.getTable().getRowCount();
    }

    @Override
    public int getColumnCount() {
        return tableNode.getTable().getColumnCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return tableNode.getTable().getValueAt(rowIndex, columnIndex);
    }

    @Override
    public String getColumnName(int column) {
        return tableNode.getTable().getColumnTitle(column);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        Class<?> retVal = null;
        for(int i = 0; i < getRowCount(); i++) {
            Object val = tableNode.getTable().getValueAt(0, columnIndex);
            if(val != null) {
                retVal = val.getClass();
                break;
            }
        }
        if(retVal == null)
            retVal = String.class;

        return retVal;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

}
