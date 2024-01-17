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

import ca.phon.extensions.*;
import ca.phon.visitor.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

	/**
	 * Get  tier description by name
	 *
	 * @param tierName
	 * @return tier description or null if given tier name is not a user tier or a system tier
	 */
	public TierDescription get(String tierName) {
		final SystemTierType systemTierType = SystemTierType.tierFromString(tierName);
		if(systemTierType != null) {
			return getSystemTierDescription(systemTierType);
		} else {
			return getUserTierDescription(tierName);
		}
	}

	/**
	 * Return index of given tier description
	 *
	 * @param td
	 * @return index of tier description or -1 if not found
	 */
	public int indexOf(TierDescription td) {
		int retVal = -1;

		for(int i = 0; i < session.getUserTierCount(); i++) {
			if(session.getUserTier(i).equals(td)) {
				retVal = i;
				break;
			}
		}

		return retVal;
	}

	/**
	 * Get tier description for system tier
	 *
	 * @param tierName
	 * @return tier description for system tier, this will include any tier parameters and blind status
	 */
	public TierDescription getSystemTierDescription(String tierName) {
        return getSystemTierDescription(SystemTierType.tierFromString(tierName));
	}

	/**
	 * Get tier description for system tier
	 *
	 * @param systemTierType
	 * @return tier description for system tier, this will include any tier parameters and blind status
	 */
	public TierDescription getSystemTierDescription(SystemTierType systemTierType) {
		if(systemTierType == null) return null;
		final SessionFactory factory = SessionFactory.newFactory();
		final TierDescription td = factory.createTierDescription(systemTierType.getName(), systemTierType.getDeclaredType(),
				new HashMap<>(), false, false);
		return td;
	}

	/**
	 * Get user tier description by name
	 *
	 * @param tierName
	 * @return user tier description of null if given tier name is not a user tier
	 */
	public TierDescription getUserTierDescription(String tierName) {
		Optional<TierDescription> tdOpt =
				stream().filter(td -> td.getName().equals(tierName)).findFirst();
        return tdOpt.orElse(null);
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

	public Stream<TierDescription> stream() {
		return stream(false);
	}

	public Stream<TierDescription> stream(boolean parallel) {
		return StreamSupport.stream(this.spliterator(), parallel);
	}
	
}
