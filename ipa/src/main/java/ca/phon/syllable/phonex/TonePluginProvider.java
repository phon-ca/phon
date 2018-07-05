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

import java.util.List;

import ca.phon.ipa.features.FeatureSet;
import ca.phon.phonex.*;

/**
 * Tone plug-in provider. This matcher allows for matching one of the
 * <code>toneX</code> features across elements in syllable.
 *
 */
@PhonexPlugin(name="tone", description="Match tone", arguments= {"tone"})
public class TonePluginProvider implements PluginProvider {

	@Override
	public PhoneMatcher createMatcher(List<String> args) throws IllegalArgumentException {
		if(args.size() > 1) 
			throw new IllegalArgumentException("Too many arguments");
		if(args.size() == 0)
			throw new IllegalArgumentException("Not enough arguments");

		final FeatureSet allToneFeatures = FeatureSet.fromArray(new String[] { "tone1", "tone2",
				"tone3", "tone4", "tone5", "tone6", "tone7", "tone8", "tone9" });
		FeatureSet toneFeatures = new FeatureSet();

		final String txt = args.get(0);
		final String[] tones = txt.split("\\|");
		if(tones.length == 0) {
			throw new IllegalArgumentException("No tones given");
		} else {
			for(String tone:tones) {
				if(tone.length() > 2) {
					throw new IllegalArgumentException("Invalid tone number " + tone);
				}
				if(tone.equals("*")) {
					// any tone, add all to tone features
					toneFeatures = FeatureSet.union(toneFeatures, allToneFeatures);
				} else {
					try {
						int toneNum = Integer.parseInt(tone);
						String toneFeature = "tone" + toneNum;
						toneFeatures = FeatureSet.singleonFeature(toneFeature);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException(e);
					}
				}
			}
		}
		
		return new ToneMatcher(toneFeatures);
	}

}
