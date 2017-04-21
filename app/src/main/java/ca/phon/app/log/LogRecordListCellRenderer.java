/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
		INFO("blank"),
		WARNING("status/dialog-warning"),
		SEVERE("status/dialog-error");
		
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
	public Component getListCellRendererComponent(JList list, Object value,
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
