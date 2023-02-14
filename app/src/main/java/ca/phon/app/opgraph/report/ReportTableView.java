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
//        table.setDefaultRenderer(String.class, new TableNodeCellRenderer());

        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT,
                    IPAMap.class.getClassLoader()
                            .getResourceAsStream("data/fonts/NotoSans-Regular.ttf")).deriveFont(14.0f);

            table.setFont(font);
        } catch (Exception e) {
        }

        JScrollPane scroller = new JScrollPane(table);

        add(scroller, BorderLayout.CENTER);
    }

//    private class TableNodeCellRenderer extends DefaultTableCellRenderer {
//        @Override
//        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//            JLabel retVal = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//
//            retVal.setFont(FontPreferences.getTierFont());
//
//            return retVal;
//        }
//    }

}
