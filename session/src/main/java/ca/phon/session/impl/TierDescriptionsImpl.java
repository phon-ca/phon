package ca.phon.session.impl;

import java.util.Iterator;

import ca.phon.session.TierDescription;
import ca.phon.session.TierDescriptions;

public class TierDescriptionsImpl extends TierDescriptions {
	
	/**
	 * Session reference
	 */
	private final SessionImpl session;
	
	TierDescriptionsImpl(SessionImpl session) {
		super();
		this.session = session;
	}

	@Override
	public Iterator<TierDescription> iterator() {
		return new TierDescriptionIterator();
	}

	/**
	 * Iterator
	 */
	private final class TierDescriptionIterator implements Iterator<TierDescription> {

		private int idx = 0;
		
		@Override
		public boolean hasNext() {
			return (idx < session.getUserTierCount());
		}

		@Override
		public TierDescription next() {
			return session.getUserTier(idx++);
		}

		@Override
		public void remove() {
			session.removeUserTier(idx - 1);
		}
		
	}

}
