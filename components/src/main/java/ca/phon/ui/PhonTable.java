package ca.phon.ui;

import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.GoogleMaterialFonts;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnControlButton;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;

/**
 * Table with some default settings such as column control icon, and default cell renderers for boolean
 * values.
 */
public class PhonTable extends JXTable  {

    public PhonTable() {
        super();

        init();
    }

    public PhonTable(TableModel dm) {
    	super(dm);

    	init();
    }

    private void init() {
        setAutoResizeMode(JXTable.AUTO_RESIZE_ALL_COLUMNS);
        setShowGrid(false);

        final ColumnControlButton columnControlButton = new ColumnControlButton(this,
                IconManager.getInstance().getFontIcon(GoogleMaterialFonts.Round.getFontName(),
                        "more_vert", IconSize.SMALL,
                        UIManager.getColor(FlatButtonUIProps.ICON_COLOR_PROP)));
        columnControlButton.setBorderPainted(false);
        setColumnControl(columnControlButton);

        setDefaultRenderer(Boolean.class, new BooleanCellRenderer());
    }

    private final class BooleanCellRenderer extends DefaultTableCellRenderer {

    	public BooleanCellRenderer() {
    		super();
    	}

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel retVal = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            retVal.setText("");
            retVal.setHorizontalAlignment(SwingConstants.CENTER);
            if(value != null && value instanceof Boolean) {
            	Boolean bVal = (Boolean)value;
                if(bVal) {
                    retVal.setIcon(IconManager.getInstance().getFontIcon(GoogleMaterialFonts.Round.getFontName(), "check_box", IconSize.MEDIUM,
                                                    		retVal.getForeground()));
                } else {
                    retVal.setIcon(IconManager.getInstance().getFontIcon(GoogleMaterialFonts.Round.getFontName(), "check_box_outline_blank", IconSize.MEDIUM,
                            retVal.getForeground()));
                }
            }

            return retVal;
        }
    }

}
