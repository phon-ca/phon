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

import ca.phon.session.TierString;
import ca.phon.session.spi.TierDescriptionSPI;

/**
 * Basic description of a tier.
 */
public class TierDescriptionImpl implements TierDescriptionSPI {

	private final boolean isGrouped;
	
	private final String name;
	
	private final Class<?> declaredType;
	
	TierDescriptionImpl(String name, boolean grouped) {
		this(name, grouped, TierString.class);
	}
	
	TierDescriptionImpl(String name, boolean grouped, Class<?> declaredType) {
		super();
		this.isGrouped = grouped;
		this.name = name;
		this.declaredType = declaredType;
	}
	
	@Override
	public boolean isGrouped() {
		return isGrouped;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getDeclaredType() {
		return declaredType;
	}

}
