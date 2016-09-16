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
package ca.phon.syllable.phonex;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.phonex.PhoneMatcher;
import ca.phon.syllable.SyllabificationInfo;

/**
 * Matcher for tone information in {@link SyllabificationInfo}
 *
 */
public class ToneMatcher implements PhoneMatcher {
	
	private FeatureSet toneFeatures;
	
	public ToneMatcher(FeatureSet features) {
		this.toneFeatures = features;
	}

	@Override
	public boolean matches(IPAElement p) {
		boolean retVal = false;
		
		final SyllabificationInfo info = p.getExtension(SyllabificationInfo.class);
		if(info != null) {
			FeatureSet intersection = FeatureSet.intersect(toneFeatures, info.getToneFeatures());
			retVal = (intersection.size() > 0);
		}
		
		return retVal;
	}

	@Override
	public boolean matchesAnything() {
		final FeatureSet allToneFeatures = FeatureSet.fromArray(new String[] { "tone1", "tone2",
				"tone3", "tone4", "tone5", "tone6", "tone7", "tone8", "tone9" });
		return toneFeatures.equals(allToneFeatures);
	}

}
