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
import ca.phon.session.spi.TierSPI;

import java.util.*;

/**
 * A tier in a record.  A tier has a name, type and a number
 * of groups.
 * 
 */
public final class Tier<T> extends ExtendableObject implements Iterable<T> {
	
	private final TierSPI<T> tierImpl;
	
	/**
	 * Tier listeners, using a {@link WeakHashMap} so that listeners
	 * are removed when their references are no longer needed.  The second
	 * {@link Boolean} parameter is unused
	 */
	private final Map<TierListener<T>, Boolean> tierListeners;
	
	Tier(TierSPI<T> impl) {
		super();
		this.tierImpl = impl;
		
		final WeakHashMap<TierListener<T>, Boolean> weakHash = 
				new WeakHashMap<TierListener<T>, Boolean>();
		tierListeners = Collections.synchronizedMap(weakHash);
	}
	
	public String getName() {
		return tierImpl.getName();
	}

	public Class<?> getDeclaredType() {
		return tierImpl.getDeclaredType();
	}

	public boolean isGrouped() {
		return tierImpl.isGrouped();
	}

	public int numberOfGroups() {
		return tierImpl.numberOfGroups();
	}

	public T getGroup(int idx) {
		return tierImpl.getGroup(idx);
	}

	public void setGroup(int idx, T val) {
		final T oldVal = (idx < numberOfGroups() ? getGroup(idx) : null);
		tierImpl.setGroup(idx, val);
		fireTierGroupChanged(idx, oldVal, val);
	}

	public void addGroup() {
		tierImpl.addGroup();
		fireTierGroupAdded(numberOfGroups()-1, getGroup(numberOfGroups()-1));
	}

	public void addGroup(int idx) {
		tierImpl.addGroup(idx);
		fireTierGroupAdded(idx, getGroup(idx));
	}

	public void addGroup(T val) {
		tierImpl.addGroup(val);
		fireTierGroupAdded(numberOfGroups()-1, val);
	}

	public void addGroup(int idx, T val) {
		tierImpl.addGroup(idx, val);
		fireTierGroupAdded(idx, val);
	}

	public T removeGroup(int idx) {
		T val = tierImpl.removeGroup(idx);
		fireTierGroupRemoved(idx, val);
		return val;
	}

	public void removeAll() {
		tierImpl.removeAll();
		fireTierGroupsCleared();
	}

	@Override
	public Iterator<T> iterator() {
		return new TierDataIterator();
	}
	
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		
		if(isGrouped()) {
			buffer.append("[");
			for(int i = 0; i < numberOfGroups(); i++) {
				if(i > 0) buffer.append("] [");
				final T grpVal = getGroup(i);
				String grpTxt = (grpVal != null ? grpVal.toString() : "");
				if(grpTxt.length() == 0) {
					// XXX Check for unvalidated values
					if(grpVal instanceof IExtendable) {
						final IExtendable extGrp = (IExtendable)grpVal;
						final UnvalidatedValue uv = extGrp.getExtension(UnvalidatedValue.class);
						if(uv != null) 
							grpTxt = uv.getValue();
					}
				}
				if(grpVal != null)
					buffer.append(grpTxt);
			}
			buffer.append("]");
		} else {
			if(numberOfGroups() > 0) {
				final T grpVal = getGroup(0);
				String grpTxt = (grpVal != null ? grpVal.toString() : "");
				if(grpTxt.length() == 0 && grpVal instanceof IExtendable) {
					final IExtendable extGrp = (IExtendable)grpVal;
					final UnvalidatedValue uv = extGrp.getExtension(UnvalidatedValue.class);
					if(uv != null) 
						grpTxt = uv.getValue();
				}
				buffer.append(grpTxt);
			}
		}
		
		return buffer.toString();
	}
	
	private final class TierDataIterator implements Iterator<T> {

		private int idx = 0;
		
		@Override
		public boolean hasNext() {
			return (idx <  numberOfGroups());
		}

		@Override
		public T next() {
			return getGroup(idx++);
		}

		@Override
		public void remove() {
			removeGroup(idx - 1);
		}
		
	}
	
	/*
	 * Tier Listeners
	 */
	public void addTierListener(TierListener<T> listener) {
		tierListeners.put(listener, Boolean.TRUE);
	}

	public void removeTierListener(TierListener<T> listener) {
		tierListeners.remove(listener);
	}
	
	private void fireTierGroupAdded(int index, T value) {
		for(TierListener<T> listener:tierListeners.keySet()) {
			listener.groupAdded(this, index, value);
		}
	}
	
	private void fireTierGroupRemoved(int index, T value) {
		for(TierListener<T> listener:tierListeners.keySet()) {
			listener.groupRemoved(this, index, value);
		}
	}
	
	private void fireTierGroupChanged(int index, T oldValue, T value) {
		for(TierListener<T> listener:tierListeners.keySet()) {
			listener.groupChanged(this, index, oldValue, value);
		}
	}
	
	private void fireTierGroupsCleared() {
		for(TierListener<T> listener:tierListeners.keySet()) {
			listener.groupsCleared(this);
		}
	}	
	
}
