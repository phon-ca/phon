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
package ca.phon.session;

import ca.phon.extensions.ExtendableObject;
import ca.phon.visitor.*;

import java.util.Iterator;

/**
 * Iteratable/visitable access for {@link Session} {@link Record}s
 *
 */
public final class Records extends ExtendableObject implements Iterable<Record>, Visitable<Record> {

	private final Session session;
	
	Records(Session session) {
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
	
	@Override
	public void accept(Visitor<Record> visitor) {
		for(Record r:this) {
			visitor.visit(r);
		}
	}
	
}
