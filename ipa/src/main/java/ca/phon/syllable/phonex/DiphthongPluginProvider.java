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

import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.PhonexPlugin;
import ca.phon.phonex.PluginProvider;

/**
 * Provides the 'diphthong' phonex plug-in matcher. 
 */
@PhonexPlugin(name="diphthong")
public class DiphthongPluginProvider implements PluginProvider {

	@Override
	public PhoneMatcher createMatcher(List<String> args)
			throws IllegalArgumentException {
		boolean diphthong = true;
		if(args.size() == 1) {
			diphthong = Boolean.parseBoolean(args.get(0));
		}
		return new DiphthongMatcher(diphthong);
	}

}
