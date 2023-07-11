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

import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.session.usertier.UserTierData;
import ca.phon.session.spi.TierDescriptionSPI;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Basic description of a tier.
 */
public class TierDescriptionImpl implements TierDescriptionSPI {

	private final String name;
	
	private final Class<?> declaredType;

	private final TierAlignmentRules tierAlignmentRules;

	private final Map<String, String> tierParams;

	TierDescriptionImpl(String name) {
		this(name, UserTierData.class, new TierAlignmentRules());
	}
	
	TierDescriptionImpl(String name, TierAlignmentRules tierAlignmentRules) {
		this(name, UserTierData.class, new HashMap<>(), tierAlignmentRules);
	}

	TierDescriptionImpl(String name, Class<?> declaredType) {
		this(name, declaredType, new HashMap<>(), new TierAlignmentRules());
	}

	TierDescriptionImpl(String name, Class<?> declaredType, TierAlignmentRules alignmentRules) {
		this(name, declaredType, new HashMap<>(), alignmentRules);
	}
	
	TierDescriptionImpl(String name, Class<?> declaredType, Map<String, String> tierParams, TierAlignmentRules tierAlignmentRules) {
		super();
		this.name = name;
		this.declaredType = declaredType;
		this.tierAlignmentRules = tierAlignmentRules;
		this.tierParams = new LinkedHashMap<>(tierParams);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getDeclaredType() {
		return declaredType;
	}

	@Override
	public Map<String, String> getTierParameters() {
		return Collections.unmodifiableMap(this.tierParams);
	}

	@Override
	public TierAlignmentRules getTierAlignmentRules() {
		return this.tierAlignmentRules;
	}

}
