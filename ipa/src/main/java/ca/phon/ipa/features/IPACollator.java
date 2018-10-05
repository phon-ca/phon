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
package ca.phon.ipa.features;

import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Text collation for IPA transcriptions.  The ordering used is the same
 * ordering found in the <code>features.xml</code> file.
 */
public class IPACollator extends RuleBasedCollator {

	private static String rules = null;

	private static final String createRules() {
		if(rules == null) {
			final Set<Character> chars = new LinkedHashSet<>();
			final Set<Character> allChars = FeatureMatrix.getInstance().getCharacterSet();
			for(Character c:allChars) {
				// exclude punctuation
				if(c != 0x28 && c!= 0x29 && c != 0x7c && c != 0x20 && c != 0x2a && c != 0x2e
						&& c != '+' && c != '-' && c != '>') {
					chars.add(c);
				}
			}
			final StringBuffer buffer = new StringBuffer();
			chars.forEach( (c) -> {
					if(buffer.length() > 0) buffer.append(" ");
					buffer.append("< ");
					buffer.append(c);
			});
			rules = buffer.toString();
		}
		return rules;
	}

	public IPACollator() throws ParseException {
		super(createRules());
	}

}

