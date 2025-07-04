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

import ca.phon.session.*;
import ca.phon.session.format.AgeFormatter;

import javax.swing.table.AbstractTableModel;
import java.time.Period;

public class ParticipantsTableModel extends AbstractTableModel {
	
	/** The transcript */
	private Session session;
	
	private boolean showCalculatedAges = true;
	
	private enum Columns {
		Id,
		Role,
		Name,
		Age,
		Sex,
		BirthDate,
		Birthplace,
		Language,
		FirstLanguage,
		Group,
		Education,
		SES,
		Other;

		// column names with spaces in place of snake case
		String[] names = {
				"ID",
				"Role",
				"Name",
				"Age",
				"Sex",
				"Birth Date",
				"Birthplace",
				"Language",
				"First Language",
				"Group",
				"Education",
				"SES",
				"Other"
		};

		public String getName() {
			return names[ordinal()];
		}
	}
	
	public ParticipantsTableModel(Session t) {
		super();
		
		this.session = t;
	}

	@Override
	public int getColumnCount() {
		return Columns.values().length;
	}

	@Override
	public int getRowCount() {
		return session.getParticipantCount();
	}
	
	@Override
	public String getColumnName(int col) {
		return Columns.values()[col].getName();
	}
	
	public boolean isShowCalculatedAges() {
		return showCalculatedAges;
	}
	
	public void setShowCalculatedAges(boolean showCalculatedAges) {
		this.showCalculatedAges = showCalculatedAges;
		fireTableDataChanged();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Participant p = 
				(session.getParticipantCount() > rowIndex ? session.getParticipant(rowIndex) : null);
		if(p == null) return "";
		
		Columns col = Columns.values()[columnIndex];

		switch(col) {
		case Id:
			return p.getId();

		case Name:
			return p.getName() != null ? p.getName() : "";

		case Role:
			return p.getRole();

		case Age:
			final Period age = p.getAge(null);
			if (age != null) {
				return AgeFormatter.ageToString(age);
			} else {
				if (isShowCalculatedAges() && p.getBirthDate() != null && session.getDate() != null
						&& p.getBirthDate().isBefore(session.getDate())) {
					final Period calculatedAge = p.getAge(session.getDate());
					return AgeFormatter.ageToString(calculatedAge);
				} else
					return "Unknown";
			}

		case BirthDate:
			return (p.getBirthDate() != null ? p.getBirthDate().toString() : "");

		case Birthplace:
			return p.getBirthplace() != null ? p.getBirthplace() : "";

		case Education:
			return p.getEducation() != null ? p.getEducation() : "";

		case Group:
			return p.getGroup() != null ? p.getGroup() : "";

		case Language:
			return p.getLanguage() != null ? p.getLanguage() : "";

		case FirstLanguage:
			return p.getFirstLanguage() != null ? p.getFirstLanguage() : "";

		case SES:
			return p.getSES() != null ? p.getSES() : "";

		case Sex:
			return p.getSex() != null ? p.getSex() : "";

		case Other:
			return p.getOther() != null ? p.getOther() : "";

		default:
			return "";
		}
	}

}
