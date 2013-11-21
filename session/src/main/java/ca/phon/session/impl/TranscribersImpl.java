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
