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
package ca.phon.ipa;

import ca.phon.ipa.features.*;
import ca.phon.syllable.SyllableConstituentType;

/**
 * Character used to indicate alignment.
 */
public class AlignmentMarker extends IPAElement {
	
	public final static char ALIGNMENT_CHAR = '\u2194';
	
	public AlignmentMarker() {
		super();
		setScType(SyllableConstituentType.UNKNOWN);
	}

	@Override
	protected FeatureSet _getFeatureSet() {
		return FeatureMatrix.getInstance().getFeatureSet(ALIGNMENT_CHAR);
	}

	@Override
	public String getText() {
		return "" + ALIGNMENT_CHAR;
	}

}
