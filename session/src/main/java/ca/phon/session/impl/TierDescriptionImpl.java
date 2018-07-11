/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.session.impl;

import ca.phon.session.TierDescription;
import ca.phon.session.TierString;

/**
 * Basic description of a tier.
 */
public class TierDescriptionImpl implements TierDescription {

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
