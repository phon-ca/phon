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
