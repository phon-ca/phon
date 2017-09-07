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
package ca.phon.ipa;

import ca.phon.ipa.features.*;
import ca.phon.syllable.SyllableConstituentType;

/**
 * A stress marker.  Stress markers can either be
 * PRIMARY or SECONDARY.
 */
public final class StressMarker extends IPAElement {
	
	/**
	 * Stress type
	 */
	private StressType type;
	
	/**
	 * Constructor
	 * 
	 * @param stress
	 */
	StressMarker(StressType stress) {
		this.type = stress;
		
		setScType(SyllableConstituentType.SYLLABLESTRESSMARKER);
	}

	/**
	 * Get the type
	 * 
	 * @return StressType
	 */
	public StressType getType() {
		return this.type;
	}
	
	/**
	 * Set the type
	 * 
	 * @param type
	 */
	public void setType(StressType type) {
		this.type = type;
	}
	
	@Override
	protected FeatureSet _getFeatureSet() {
		return FeatureMatrix.getInstance().getFeatureSet(type.getGlyph());
	}

	@Override
	public String getText() {
		return type.getGlyph() + "";
	}

}
