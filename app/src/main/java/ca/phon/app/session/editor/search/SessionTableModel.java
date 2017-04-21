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
package ca.phon.app.session.editor.search;

import java.util.concurrent.atomic.AtomicReference;

import javax.swing.table.AbstractTableModel;

import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;

public class SessionTableModel extends AbstractTableModel {
	
	private static final long serialVersionUID = 5834409894907219596L;
	
	private static enum Columns {
		RECORD_INDEX("Record #", Integer.class),
		SPEAKER("Speaker", Participant.class),
		ORTHOGRAPHY(SystemTierType.Orthography.getName(), Tier.class),
		IPATARGET(SystemTierType.IPATarget.getName(), Tier.class),
		IPAACTUAL(SystemTierType.IPAActual.getName(), Tier.class),
		SEGMENT(SystemTierType.Segment.getName(), Tier.class),
		NOTES(SystemTierType.Notes.getName(), Tier.class),
		OTHER("", Tier.class);
		
		private String name;
		
		private Class<?> type;
		
		private Columns(String name, Class<?> type) {
			this.name = name;
			this.type = type;
		}
		
		public String getName() {
			return this.name;
		}
		
		public Class<?> getType() {
			return this.type;
		}
	}

	private final AtomicReference<Session> sessionRef
		= new AtomicReference<Session>();
	
	public SessionTableModel() {
	}
	
	public SessionTableModel(Session session) {
		sessionRef.getAndSet(session);
	}
	
	public void setSession(Session session) {
		sessionRef.getAndSet(session);
		fireTableStructureChanged();
	}
	
	public Session getSession() {
		return sessionRef.get();
	}

	@Override
	public int getRowCount() {
		final Session session = getSession();
		return (session != null ? session.getRecordCount() : 0);
	}

	@Override
	public int getColumnCount() {
		int retVal = 0;
		
		final Session session = getSession();
		if(session != null) {
			retVal = Columns.values().length - 1;
			retVal += session.getUserTierCount();
		}
		
		return retVal;
	}
	
	@Override
	public String getColumnName(int column) {
		String retVal = super.getColumnName(column);
		if(column < Columns.OTHER.ordinal()) {
			retVal = Columns.values()[column].getName();
		} else {
			final Session session = getSession();
			int tierIdx = column - Columns.OTHER.ordinal();
			if(tierIdx < session.getUserTierCount()) {
				retVal = session.getUserTier(tierIdx).getName();
			}
		}
		return retVal;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		Class<?> retVal = super.getColumnClass(columnIndex);
		if(columnIndex < Columns.OTHER.ordinal()) {
			retVal = Columns.values()[columnIndex].getType();
		} else {
			retVal = Columns.OTHER.getType();
		}
		return retVal;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object retVal = null;
		
		final Session session = getSession();
		if(session != null && rowIndex < session.getRecordCount()) {
			final Record record = session.getRecord(rowIndex);
			
			if(columnIndex == Columns.RECORD_INDEX.ordinal()) {
				retVal = rowIndex + 1;
			} else if(columnIndex == Columns.SPEAKER.ordinal()) {
				retVal = record.getSpeaker();
			} else {
				final String tierName = getColumnName(columnIndex);
				retVal = record.getTier(tierName);
			}
		}
		
		return retVal;
	}

}
