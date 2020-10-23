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
 * Iterable/visitor access for {@link Session} {@link TierDescription}s.
 */
public final class TierDescriptions implements IExtendable, Iterable<TierDescription>, Visitable<TierDescription> {

	/**
	 * Extension support
	 */
	private final ExtensionSupport extSupport = new ExtensionSupport(TierDescriptions.class, this);
	
	/**
	 * Session reference
	 */
	private final Session session;
	
	TierDescriptions(Session session) {
		super();
		this.session = session;
		
		extSupport.initExtensions();
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

	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}

	@Override
	public void accept(Visitor<TierDescription> visitor) {
		for(TierDescription td:this) {
			visitor.visit(td);
		}
	}
	
}
