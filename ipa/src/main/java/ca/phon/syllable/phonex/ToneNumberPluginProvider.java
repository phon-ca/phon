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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.PhonexPlugin;
import ca.phon.phonex.PluginProvider;

/**
 * Tone plug-in provider. This matcher allows for matching tone number of an element.
 *
 */
@PhonexPlugin(name="tn", description="Match tone number", arguments= {"(not)? tone number (| tone number ...)"})
public class ToneNumberPluginProvider implements PluginProvider {

	@Override
	public PhoneMatcher createMatcher(List<String> args) throws IllegalArgumentException {
		if(args.size() > 1) 
			throw new IllegalArgumentException("Too many arguments");
		if(args.size() == 0)
			throw new IllegalArgumentException("Not enough arguments");

		Pattern tonePattern = Pattern.compile("(not\\s?)?([\\-0-9][ 0-9]*(\\|[\\-0-9][ 0-9]+)*)");
		Matcher matcher = tonePattern.matcher(args.get(0));
		
		if(matcher.matches()) {
			String[] tones = matcher.group(2).split("\\|");
			List<String> toneList = new ArrayList<String>();
			for(String tone:tones) {
				if(ToneNumberMatcher.NO_TONE.contentEquals(tone)) {
					toneList.add(tone);
				} else {
					try {
						Integer.parseInt(tone);
						toneList.add(tone.strip());
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException(e);
					}
				}
			}
			return new ToneNumberMatcher(toneList, (matcher.group(1) != null && "not".equals(matcher.group(1).strip())) );
		} else {
			throw new IllegalArgumentException("Invalid syntax");
		}
			
	}

}
