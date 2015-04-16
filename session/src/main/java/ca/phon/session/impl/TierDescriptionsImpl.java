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
