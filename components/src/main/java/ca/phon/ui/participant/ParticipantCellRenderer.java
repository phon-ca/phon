/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ui.participant;

import java.awt.Component;
import java.time.*;
import java.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

import ca.phon.session.*;

public class ParticipantCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -7028859808797433102L;

	private final DateTimeFormatter dateFormatter = 
			DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel retVal = 
			(JLabel)super.getTableCellRendererComponent(table, value, 
					isSelected, hasFocus, row, column);
		
		if(column == 1) {
			if(row == ParticipantTableField.Sex.ordinal()) {
				Sex sex = (ca.phon.session.Sex)value;
				if(sex == Sex.MALE)
					retVal.setText("Male");
				else
					retVal.setText("Female");
			} else if (row == ParticipantTableField.Birthday.ordinal()) {
				final LocalDate bday = (LocalDate)value;
				retVal.setText(dateFormatter.format(bday));
			} else if (row == ParticipantTableField.Age.ordinal()) {
				final Period age = (Period)value;
				retVal.setText(AgeFormatter.ageToString(age));
			}
		}
		
		return retVal;
	}

}
