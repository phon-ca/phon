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
