/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.ipa.features.FeatureSet;
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
