/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.time.LocalDate;
import java.time.Period;

import javax.swing.table.AbstractTableModel;

import ca.phon.session.Participant;
import ca.phon.session.ParticipantRole;
import ca.phon.session.Sex;

public class ParticipantTableModel extends AbstractTableModel {
	
	/** The participant */
	private Participant participant;
	
	/** The relative session date */
	private LocalDate sessionDate;
	
	/** Short version ? */
	public boolean shortVersion = true;
	
	public ParticipantTableModel(Participant participant, LocalDate sessionDate) {
		super();
		
		this.participant = participant;
		this.sessionDate = sessionDate;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		if(shortVersion)
			return 2;
		else
			return ParticipantTableField.values().length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(rowIndex >= getRowCount() || columnIndex >= getColumnCount()) {
			return new String();
		}
		
		if(columnIndex == 0) {
			return ParticipantTableField.values()[rowIndex].getColumnName();
		} else {
			return getValueForField(ParticipantTableField.values()[rowIndex]);
		}
	}
	
	private Object getValueForField(ParticipantTableField field) {
		if(field == ParticipantTableField.Name) {
			return (
					participant.getName() == null ? new String() : participant.getName());
		} else if(field == ParticipantTableField.Age) {
			return (
					participant.getAge(sessionDate) == null ? Period.ZERO : participant.getAge(sessionDate));
		} else if(field == ParticipantTableField.Birthday) {
			return (
					participant.getBirthDate() == null ? LocalDate.now() : participant.getBirthDate());
		} else if(field == ParticipantTableField.Education) {
			return (
					participant.getEducation() == null ? new String() : participant.getEducation());
		} else if (field == ParticipantTableField.Group) {
			return (
					participant.getGroup() == null ? new String() : participant.getGroup());
		} else if (field == ParticipantTableField.Sex) {
			return (
					participant.getSex() == null ? Sex.MALE : participant.getSex());
		} else if (field == ParticipantTableField.Role) {
			return (
					participant.getRole() == null ? new String() : participant.getRole());
		} else if (field == ParticipantTableField.Language) {
			return (
					participant.getLanguage() == null ? new String() : participant.getLanguage());
		} else {
			return new String();
		}
	}

	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 0)
			return false;
		else {
			if(rowIndex != ParticipantTableField.Age.ordinal()
					)
				return true;
			else
				return false;
		}
	}

	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(columnIndex != 1) return;
		
		if(aValue == null) return;
		
		if(rowIndex == ParticipantTableField.Name.ordinal()) {
			participant.setName(aValue.toString());
		} else if(rowIndex == ParticipantTableField.Sex.ordinal()) {
			if(aValue.toString().toLowerCase().startsWith("m")) {
				participant.setSex(Sex.MALE);
			} else {
				participant.setSex(Sex.FEMALE);
			}
		} else if(rowIndex == ParticipantTableField.Birthday.ordinal()) {
			
//			DateTime bdate = null;
//			try {
//				bdate = 
//					Age.parse(aValue.toString());
//			} catch (Exception e) {}
//			if(bdate == null) return;
//			
//			Calendar bday = Calendar.getInstance();
//			bday.setTime(bdate);
//			participant.setBirthDate(bday);
//			
//			// now update the age
//			if(sessionDate != null) {
//				PhonDuration age = 
//					PhonDuration.getDuration(bday, sessionDate);
//				if(age.getYears() >= 0
//						&& age.getMonths() >= 0
//						&& age.getDays() >= 0)
//					participant.setAge(age);
//				else
//					participant.setAge(new PhonDuration(0,0,0));
//			}
		} else if(rowIndex == ParticipantTableField.Education.ordinal()) {
			participant.setEducation(aValue.toString());
		} else if(rowIndex == ParticipantTableField.Group.ordinal()) {
			participant.setGroup(aValue.toString());
		} else if(rowIndex == ParticipantTableField.Language.ordinal()) {
			participant.setLanguage(aValue.toString());
		} else if(rowIndex == ParticipantTableField.Role.ordinal()) {
			final ParticipantRole role = ParticipantRole.fromString(aValue.toString());
			participant.setRole(role);
		}
		
		super.fireTableCellUpdated(rowIndex, columnIndex);
	}

	public boolean isShortVersion() {
		return shortVersion;
	}

	public void setShortVersion(boolean shortVersion) {
		this.shortVersion = shortVersion;
		this.fireTableDataChanged();
	}

	public Participant getParticipant() {
		return participant;
	}

	public void setParticipant(Participant participant) {
		this.participant = participant;
		super.fireTableDataChanged();
	}

}
