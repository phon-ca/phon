/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

import javax.swing.table.AbstractTableModel;

import org.joda.time.Period;

import ca.phon.session.AgeFormatter;
import ca.phon.session.Participant;
import ca.phon.session.Session;

public class ParticipantsTableModel extends AbstractTableModel {
	
	/** The transcript */
	private Session session;
	
	private enum Columns {
		Name,
		Age;
		
		String[] names = {
				"Participant Name",
				"Age"
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

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Participant p = 
				(session.getParticipantCount() > rowIndex ? session.getParticipant(rowIndex) : null);
		if(p == null) return "";
		
		Columns col = Columns.values()[columnIndex];
		if(col == Columns.Name) {
			return
					(p.getName() != null && p.getName().trim().length() > 0 
						? p.getName() : p.getId() != null 
							? p.getId() : p.getRole() );
		} else if(col == Columns.Age) {
			final Period age = p.getAge(null);
			if(age != null && age.getYears() > 0) {
				return AgeFormatter.ageToString(age);
			} else {
				return "Unknown";
			}
		} else {
			return "";
		}
	}

}
