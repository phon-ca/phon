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
package ca.phon.session.impl;

import java.util.Iterator;

import ca.phon.session.Record;
import ca.phon.session.Records;

public class RecordsImpl extends Records {

	private final SessionImpl session;
	
	RecordsImpl(SessionImpl session) {
		super();
		this.session = session;
	}
	
	@Override
	public Iterator<Record> iterator() {
		return new RecordIterator();
	}
	
	private final class RecordIterator implements Iterator<Record> {

		private int idx = 0;
		
		@Override
		public boolean hasNext() {
			return (idx < session.getRecordCount());
		}

		@Override
		public Record next() {
			return session.getRecord(idx++);
		}

		@Override
		public void remove() {
			session.removeRecord(idx-1);
		}
		
	}

}
