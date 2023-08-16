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

import ca.phon.session.usertier.UserTierData;
import ca.phon.session.spi.TierDescriptionSPI;

import java.util.*;

/**
 * Basic description of a tier.
 */
public class TierDescriptionImpl implements TierDescriptionSPI {

	private final String name;
	
	private final Class<?> declaredType;

	private final Map<String, String> tierParams;

	private final boolean excludeFromAlignment;

	private final List<String> subtypeDelim;

	private final String subtypeExpr;
	
	TierDescriptionImpl(String name) {
		this(name, UserTierData.class);
	}

	TierDescriptionImpl(String name, boolean excludeFromAlignment) {
		this(name, UserTierData.class, excludeFromAlignment);
	}

	TierDescriptionImpl(String name, Class<?> declaredType) {
		this(name, declaredType, new HashMap<>());
	}

	TierDescriptionImpl(String name, Class<?> declaredType, boolean excludeFromAlignment) {
		this(name, declaredType, new HashMap<>(), excludeFromAlignment, new ArrayList<>(), null);
	}

	TierDescriptionImpl(String name, Class<?> declaredType, Map<String, String> tierParams) {
		this(name, declaredType, tierParams, false, new ArrayList<>(), null);
	}

	TierDescriptionImpl(String name, Class<?> declaredType, Map<String, String> tierParams, boolean excludeFromAlignment) {
		this(name, declaredType, tierParams, excludeFromAlignment, new ArrayList<>(), null);
	}

	TierDescriptionImpl(String name, Class<?> declaredType, Map<String, String> tierParams, boolean excludeFromAlignment, List<String> subtypeDelim, String subtypeExpr) {
		super();
		this.name = name;
		this.declaredType = declaredType;
		this.tierParams = new LinkedHashMap<>(tierParams);
		this.excludeFromAlignment = excludeFromAlignment;
		this.subtypeDelim = subtypeDelim;
		this.subtypeExpr = subtypeExpr;
	}


	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Class<?> getDeclaredType() {
		return this.declaredType;
	}

	@Override
	public Map<String, String> getTierParameters() {
		return this.tierParams;
	}

	@Override
	public boolean isExcludeFromAlignment() {
		return this.excludeFromAlignment;
	}

	@Override
	public List<String> getSubtypeDelim() {
		return Collections.unmodifiableList(this.subtypeDelim);
	}

	@Override
	public String getSubtypeExpr() {
		return this.subtypeExpr;
	}

}
