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
package ca.phon.session;

import java.util.*;

import ca.phon.extensions.*;
import ca.phon.visitor.*;

/**
 * Iterator/vistor access for {@link Session} {@link Transcriber}s
 *
 */
public final class Transcribers extends ExtendableObject implements Iterable<Transcriber>, Visitable<Transcriber> {

	private final Session session;
	
	Transcribers(Session session) {
		super();
		this.session = session;
	}
	
	public Session getSession() {
		return this.session;
	}
	
	@Override
	public Iterator<Transcriber> iterator() {
		return new TranscriberIterator();
	}
	
	
	@Override
	public void accept(Visitor<Transcriber> visitor) {
		iterator().forEachRemaining(visitor::visit);
	}

	private final class TranscriberIterator implements Iterator<Transcriber> {

		private int idx = 0;
		
		@Override
		public boolean hasNext() {
			return (idx < session.getTranscriberCount());
		}

		@Override
		public Transcriber next() {
			return session.getTranscriber(idx++);
		}

		@Override
		public void remove() {
			session.removeTranscriber(idx - 1);
		}
		
	}
	
}
