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
package ca.phon.app.session.editor.search;

import java.util.concurrent.atomic.*;

import javax.swing.table.*;

import ca.phon.session.*;
import ca.phon.session.Record;

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
