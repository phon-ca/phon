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
 * A tier in a record with name, parameters, alignment rules and value.
 * 
 */
public final class Tier<T> extends ExtendableObject {
	
	private final TierSPI<T> tierImpl;

	private final List<TierListener<T>> tierListeners = Collections.synchronizedList(new ArrayList<>());
	
	Tier(TierSPI<T> impl) {
		super();
		this.tierImpl = impl;
	}

	/**
	 * @return tier name
	 */
	public String getName() {
		return tierImpl.getName();
	}

	/**
	 * @return tier type
	 */
	public Class<?> getDeclaredType() {
		return tierImpl.getDeclaredType();
	}

	/**
	 * Tier parameters as setup in the session {@link TierDescription}
	 * @return tier parameters
	 */
	public Map<String, String> getTierParameters() {
		return tierImpl.getTierParameters();
	}

	/**
	 * Tier alignment rules as setup in the session {@link TierDescription}
	 *
	 * @return tier alignment rules
	 */
	public TierAlignmentRules getTierAlignmentRules() {
		return tierImpl.getTierAlignmentRules();
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

	/**
	 * Return the unvalidated value for this tier (if set)
	 * @return unvalidated value or null
	 */
	public UnvalidatedValue getUnvalidatedValue() {
		return getExtension(UnvalidatedValue.class);
	}

	/**
	 * Get tier value
	 *
	 * @return tier value (may be null)
	 */
	public T getValue() {
		return tierImpl.getValue();
	}

	/**
	 * Has the tier value been set
	 *
	 * @return true if tier value is not null
	 */
	public boolean hasValue() {
		return getValue() != null;
	}

	/**
	 * Clear tier value (set to null)
	 *
	 */
	public void clear() {
		setValue(null);
	}

	/**
	 * Set value for tier, this will trigger a value changed event if necessary
	 *
	 * @param value
	 */
	public void setValue(T value) {
		final T oldValue = getValue();
		tierImpl.setValue(value);
		putExtension(UnvalidatedValue.class, null);
		fireTireValueChanged(oldValue, value);
	}

	/**
	 * Return string representation of tier value
	 *
	 * @return tier string
	 */
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
		tierListeners.add(listener);
	}

	public void removeTierListener(TierListener<T> listener) {
		tierListeners.remove(listener);
	}
	
	private void fireTireValueChanged(T oldValue, T value) {
		for(TierListener<T> listener:tierListeners) {
			listener.tierValueChanged(this, oldValue, value);
		}
	}

}
