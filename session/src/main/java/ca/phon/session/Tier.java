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

import java.text.ParseException;
import java.util.*;

/**
 * A tier in a record.  A tier has a name, type and a number
 * of groups.
 * 
 */
public final class Tier<T> extends ExtendableObject {
	
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

	/**
	 * Set the tier text. If the text can be parsed into the appropriate object type
	 * the tier value will be updated with the new value. If the text cannot be parsed
	 * the tier value will be cleared and the tier become unvalidated.
	 *
	 * @param text
	 */
	public void setText(String text) {
		try {
			final T obj = tierImpl.parse(text);
			setValue(obj);
		} catch (ParseException pe) {
			final UnvalidatedValue uv = new UnvalidatedValue(text, pe);
			putExtension(UnvalidatedValue.class, uv);
		}
	}

	/**
	 * Is the tier unvalidated, meaning unparseable text has been passed to setText.
	 *
	 * @return boolean
	 */
	public boolean isUnvalidated() {
		return getExtension(UnvalidatedValue.class) != null;
	}

	public UnvalidatedValue getUnvalidatedValue() {
		return getExtension(UnvalidatedValue.class);
	}

	public T getValue() {
		return tierImpl.getValue();
	}

	public boolean hasValue() {
		return getValue() != null;
	}

	public void clear() {
		setValue(null);
	}

	public void setValue(T value) {
		final T oldValue = getValue();
		tierImpl.setValue(value);
		putExtension(UnvalidatedValue.class, null);
		fireTireValueChanged(oldValue, value);
	}

	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		if(hasValue())
			buffer.append(getValue());
		return buffer.toString();
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
	
	private void fireTireValueChanged(T oldValue, T value) {
		for(TierListener<T> listener:tierListeners.keySet()) {
			listener.tierValueChanged(this, oldValue, value);
		}
	}

}
