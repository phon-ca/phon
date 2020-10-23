/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

import java.time.*;

import javax.swing.table.*;

import ca.phon.session.*;

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
