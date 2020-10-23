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
package ca.phon.phonex;

import org.junit.Test;
import org.junit.runner.*;
import org.junit.runners.*;

import ca.phon.ipa.*;
import junit.framework.Assert;

@RunWith(JUnit4.class)
public class TestFlags {

	@Test
	public void testOverlappingMatches() throws Exception {
		final String phonex = "\\c\\v\\c\\v/o";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final String ipa = "badafagabadafa";

		final PhonexMatcher matcher = pattern.matcher(IPATranscript.parseIPATranscript(ipa));
		int numMatches = 0;
		while(matcher.find()) {
			++numMatches;
		}
		Assert.assertEquals(6, numMatches);
	}

	@Test(expected=PhonexPatternException.class)
	public void testInvalidFlag() throws Exception {
		final String phonex = "\\c\\v/z";
		PhonexPattern.compile(phonex);
	}

}
