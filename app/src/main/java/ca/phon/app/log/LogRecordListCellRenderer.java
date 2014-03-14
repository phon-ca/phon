package ca.phon.app.log;

import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;

import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * List renderer for {@link LogRecord}s
 */
public class LogRecordListCellRenderer extends DefaultListCellRenderer {
	
	private static final long serialVersionUID = 6634832505309910589L;

	/**
	 * Icons
	 */
	enum LogRecordIcon {
		INFO(""),
		WARNING(""),
		SEVERE("");
		
		private String iconName;
		
		private LogRecordIcon(String iconName) {
			this.iconName = iconName;
		}
		
		public ImageIcon getIcon() {
			return IconManager.getInstance().getIcon(iconName, IconSize.SMALL);
		}
		
		public static LogRecordIcon iconForLevel(Level level) {
			LogRecordIcon retVal = LogRecordIcon.INFO;
			
			if(level == Level.WARNING) {
				retVal = LogRecordIcon.WARNING;
			} else if(level == Level.SEVERE) {
				retVal = LogRecordIcon.SEVERE;
			}
			
			return retVal;
		}
	}
	
	private final LogFormatter formatter = new LogFormatter();
	
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		final JLabel retVal = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		
		if(value instanceof LogRecord) {
			final LogRecord record = (LogRecord)value;
			retVal.setIcon(LogRecordIcon.iconForLevel(record.getLevel()).getIcon());
			retVal.setText(formatter.format(record));
		}
		
		return retVal;
	}
	
}
