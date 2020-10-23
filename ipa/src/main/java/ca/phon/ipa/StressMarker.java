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
package ca.phon.ipa;

import ca.phon.ipa.features.*;
import ca.phon.syllable.*;

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
