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
package ca.phon.syllable.phonex;

import java.util.List;

import ca.phon.ipa.features.FeatureSet;
import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.PhonexPlugin;
import ca.phon.phonex.PluginProvider;

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