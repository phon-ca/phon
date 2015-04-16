/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

import ca.phon.session.Transcriber;
import ca.phon.session.Transcribers;

public class TranscribersImpl extends Transcribers {

	private final SessionImpl session;
	
	TranscribersImpl(SessionImpl session) {
		super();
		this.session = session;
	}
	
	@Override
	public Iterator<Transcriber> iterator() {
		return new TranscriberIterator();
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
