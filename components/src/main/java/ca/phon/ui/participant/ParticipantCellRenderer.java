/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.ui.participant;

import ca.phon.session.Sex;
import ca.phon.session.format.AgeFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

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
