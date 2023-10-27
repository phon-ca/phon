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
import ca.phon.formatter.Formatter;
import ca.phon.opgraph.extensions.Extendable;
import ca.phon.session.spi.TierSPI;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;

/**
 * A tier in a record with name, parameters, alignment rules and value.
 * 
 */
public final class Tier<T> implements IExtendable {
	
	private final TierSPI<T> tierImpl;

	private final List<TierListener<T>> tierListeners = Collections.synchronizedList(new ArrayList<>());

	private final ExtensionSupport extensionSupport = new ExtensionSupport(Tier.class, this);

	Tier(TierSPI<T> impl) {
		super();
		this.tierImpl = impl;
		extensionSupport.initExtensions();
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
	public Class<T> getDeclaredType() {
		return (Class<T>) tierImpl.getDeclaredType();
	}

	/**
	 * Tier parameters as setup in the session {@link TierDescription}
	 * @return tier parameters
	 */
	public Map<String, String> getTierParameters() {
		return tierImpl.getTierParameters();
	}

	/**
	 * Is tier excluded from cross tier alignment
	 *
	 * @return true if tier is not part of cross tier alignment
	 */
	public boolean isExcludeFromAlignment() {
		return tierImpl.isExcludeFromAlignment();
	}

	/**
	 * Is tier a blind tier?
	 *
	 * @return true if tier is included in blind transcription
	 */
	public boolean isBlind() { return tierImpl.isBlind(); }

	/**
	 * Set blind status of this tier
	 *
	 * @param blind
	 */
	public void setBlind(boolean blind) {
		this.tierImpl.setBlind(blind);
	}

	/**
	 * Return true if this tier include subtype delimiters
	 *
	 * @return true if the number of subtype delimiters > 0
	 */
	public boolean hasSubtypeDelim() {
		return getSubtypeDelim() != null && !getSubtypeDelim().isEmpty();
	}

	/**
	 * Return true if a subtype expression has been specified.
	 *
	 * @return true if this tier specified a subtype expression
	 */
	public boolean hasSubtypeExpr() {
		return getSubtypeExpr() != null && !getSubtypeExpr().isEmpty();
	}

	/**
	 * Get subtype delimiters (if any)
	 *
	 * @return list of subtype delimiters
	 */
	public List<String> getSubtypeDelim() {
		return tierImpl.getSubtypeDelim();
	}

	/**
	 * Get subtype regex. Groups in the expression will be used to
	 * identify part of the expression to use
	 *
	 * @return subtype expr or null if not set
	 */
	public String getSubtypeExpr() {
		return tierImpl.getSubtypeExpr();
	}

	/**
	 * Does this tier have a blind transcription for given transcriber
	 *
	 * @param transcriberId
	 * @return true if getBlindTranscription(transcriberId) != null
	 */
	public boolean hasBlindTranscription(String transcriberId) {
		return getBlindTranscription(transcriberId) != null;
	}

	/**
	 * Is the blind transcription for the given transcriber unvalidated
	 *
	 * @param transcriberId
	 * @return true if the blind transcription has a parse error
	 */
	public boolean isBlindTranscriptionUnvalidated(String transcriberId) {
		final T value = getBlindTranscription(transcriberId);
		if(value == null) return false;
		if(value instanceof IExtendable extendable) {
			return extendable.getExtension(UnvalidatedValue.class) != null;
		} else {
			return false;
		}
	}

	/**
	 * Get the unvalidated value for the given blind transcriber (if any)
	 *
	 * @return unvalidated value for transcriber or null if isBlindTranscription(transcriberId) returns false
	 */
	public UnvalidatedValue getBlindUnvalidatedValue(String transcriberId) {
		final T value = getBlindTranscription(transcriberId);
		if(value == null) return null;
		if(value instanceof IExtendable extendable) {
			return extendable.getExtension(UnvalidatedValue.class);
		} else {
			return null;
		}
	}

	/**
	 * Set blind transcription
	 *
	 * @param transcriberId
	 * @param value
	 */
	public void setBlindTranscription(String transcriberId, T value) {
		tierImpl.setBlindTranscription(transcriberId, value);
	}

	/**
	 * Set blind transcription using text
	 *
	 * @param transcriberId
	 * @param text
	 * @return true if successful, false if text has a parse error
	 */
	public boolean setBlindTranscription(String transcriberId, String text) {
		try {
			final T obj = tierImpl.parse(text);
			setBlindTranscription(transcriberId, obj);
			return true;
		} catch (ParseException pe) {
			final UnvalidatedValue uv = new UnvalidatedValue(text, pe);
			if(IExtendable.class.isAssignableFrom(getDeclaredType())) {
				try {
					IExtendable obj = (IExtendable) getDeclaredType().getDeclaredConstructor().newInstance();
					obj.putExtension(UnvalidatedValue.class, uv);
					setBlindTranscription(transcriberId, (T)obj);
				} catch (InvocationTargetException | InstantiationException | IllegalAccessException |
						 NoSuchMethodException e) {
				}
			}
			return false;
		}
	}

	/**
	 * Get blind transcription for given transcriber
	 *
	 * @param transcriberId
	 * @return blind transcription for transcriber or null if none or isBlind() is false
	 */
	public T getBlindTranscription(String transcriberId) {
		return tierImpl.getBlindTranscription(transcriberId);
	}

	/**
	 * Return list of blind transcribers for this tier
	 *
	 * @return list of transcriberIds
	 */
	public List<String> getTranscribers() {
		return tierImpl.getTranscribers();
	}

	/**
	 * Return formatter used by setText and toString for converting between objects and text
	 *
	 * @return formatter, if no formatter is manually set will use the result of FormatterFactory.createFormatter,
	 *  null if no formatter is set or found
	 */
	public Formatter<T> getFormatter() {
		return tierImpl.getFormatter();
	}

	/**
	 * Set formatter used by parse and toString for converting between objects and text
	 *
	 * @param formatter, use null to use default (if any)
	 */
	public void setFormatter(Formatter<T> formatter) {
		tierImpl.setFormatter(formatter);
	}

	/**
	 * Set the tier text. If the text can be parsed into the appropriate object type
	 * the tier value will be updated with the new value. If the text cannot be parsed
	 * the tier value will be cleared and the tier become unvalidated.
	 *
	 * @param text
	 * @return true if successful, false if text has a parse error
	 */
	public boolean setText(String text) {
		try {
			final T obj = tierImpl.parse(text);
			setValue(obj);
			return true;
		} catch (ParseException pe) {
			final UnvalidatedValue uv = new UnvalidatedValue(text, pe);
			if(IExtendable.class.isAssignableFrom(getDeclaredType())) {
				try {
					IExtendable obj = (IExtendable) getDeclaredType().getDeclaredConstructor().newInstance();
					obj.putExtension(UnvalidatedValue.class, uv);
					setValue((T)obj);
				} catch (InvocationTargetException | InstantiationException | IllegalAccessException |
						 NoSuchMethodException e) {
				}
			} else {
				putExtension(UnvalidatedValue.class, uv);
			}
			return false;
		}
	}

	/**
	 * Is the tier unvalidated, meaning unparseable text has been passed to setText.
	 *
	 * @return boolean
	 */
	public boolean isUnvalidated() {
		if(IExtendable.class.isAssignableFrom(getDeclaredType())) {
			return hasValue() && ((IExtendable)getValue()).getExtension(UnvalidatedValue.class) != null;
		} else {
			return getExtension(UnvalidatedValue.class) != null;
		}
	}

	/**
	 * Return the unvalidated value for this tier (if set)
	 * @return unvalidated value or null
	 */
	public UnvalidatedValue getUnvalidatedValue() {
		if(IExtendable.class.isAssignableFrom(getDeclaredType())) {
			return hasValue() ? ((IExtendable)getValue()).getExtension(UnvalidatedValue.class) : null;
		} else {
			return getExtension(UnvalidatedValue.class);
		}
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
		if(hasValue()) {
			final Formatter<T> formatter = getFormatter();
			if(formatter != null)
				buffer.append(formatter.format(getValue()));
			else
				buffer.append(getValue());
		} else if(isUnvalidated())
			buffer.append(getUnvalidatedValue().getValue());
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

	@Override
	public Set<Class<?>> getExtensions() {
		return extensionSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extensionSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extensionSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extensionSupport.removeExtension(cap);
	}
}
