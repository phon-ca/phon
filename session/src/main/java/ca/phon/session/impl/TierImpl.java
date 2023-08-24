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
package ca.phon.session.impl;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.session.spi.TierSPI;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class TierImpl<T> implements TierSPI<T> {
	
	/**
	 * Declared type
	 */
	private final Class<T> declaredType;
	
	/**
	 * name
	 */
	private final String tierName;

	private final Map<String, String> tierParameters;

	private final boolean excludeFromAlignment;

	private final boolean blind;

	private final List<String> subtypeDelim;

	private final String subtypeExpr;

	private final Map<String, T> blindTranscriptions = new LinkedHashMap<>();

	/**
	 * Group data
	 */
	private final AtomicReference<T> valueRef = new AtomicReference<>();

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param type
	 * @param tierParameters
	 * @param excludeFromAlignment
	 * @param subtypeDelim
	 * @param subtypeExpr
	 */
	TierImpl(String name, Class<T> type, Map<String, String> tierParameters, boolean excludeFromAlignment, boolean blind, List<String> subtypeDelim, String subtypeExpr) {
		super();
		this.tierName = name;
		this.declaredType = type;
		this.tierParameters = tierParameters;
		this.excludeFromAlignment = excludeFromAlignment;
		this.blind = blind;
		this.subtypeDelim = subtypeDelim;
		this.subtypeExpr = subtypeExpr;
	}

	@Override
	public String getName() {
		return tierName;
	}

	@Override
	public Class<T> getDeclaredType() {
		return declaredType;
	}

	@Override
	public Map<String, String> getTierParameters() {
		return Collections.unmodifiableMap(this.tierParameters);
	}

	@Override
	public boolean isExcludeFromAlignment() {
		return this.excludeFromAlignment;
	}

	@Override
	public boolean isBlind() {
		return blind;
	}

	@Override
	public List<String> getSubtypeDelim() {
		return Collections.unmodifiableList(subtypeDelim);
	}

	@Override
	public String getSubtypeExpr() {
		return subtypeExpr;
	}

	@Override
	public void setBlindTranscription(String transcriberId, T value) {
		if(!isBlind()) return;
		blindTranscriptions.put(transcriberId, value);
	}

	@Override
	public T getBlindTranscription(String transcriberId) {
		if(!isBlind()) return null;
		return blindTranscriptions.get(transcriberId);
	}

	@Override
	public List<String> getTranscribers() {
		return blindTranscriptions.keySet().stream().toList();
	}

	@Override
	public T parse(String text) throws ParseException {
		final Formatter<T> factory = FormatterFactory.createFormatter(getDeclaredType());
		if(factory != null) {
			return factory.parse(text);
		} else {
			throw new ParseException("No parser available for type " + getDeclaredType(), 0);
		}
	}

	@Override
	public T getValue() {
		return this.valueRef.get();
	}

	@Override
	public void setValue(T value) {
		this.valueRef.set(value);
	}
}
