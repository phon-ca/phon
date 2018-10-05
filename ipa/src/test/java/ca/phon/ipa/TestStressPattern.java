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

import java.text.ParseException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import junit.framework.Assert;

@RunWith(JUnit4.class)
public class TestStressPattern {

	@Test
	public void testMatches() throws ParseException {
		final String[] samples = {"ˈk:Oə:Nn:Cˌs:Lt:Oɹ:Oe:Dɪ:Dn:Ct:Cs:R", "ˈb:Oʌ:Nɹ:Cθ:Cˌd:Oe:Dɪ:D", "ˈb:Oʌ:Nt:Aə:Nɹ:Ciː:N"};
		final String sp = "1A*2";
		final boolean[] expected = { true, true, false };
	
		for(int i = 0; i < samples.length; i++) {
			final IPATranscript ipa = IPATranscript.parseIPATranscript(samples[i]);
			Assert.assertEquals(expected[i], ipa.matchesStressPattern(sp));
		}
	}
	
	@Test
	public void testContains() throws ParseException {
		final String txt = "ˈtæktfəl ˈbʌtəɹiː kənˈstɹeɪnts";
		final String sp = "U 1";
		
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		Assert.assertEquals(true, ipa.containsStressPattern(sp));
	}
	
	@Test
	public void testFind() throws ParseException {
		final String txt = "ˈtæktfəl ˈbʌtəɹiː kənˈstɹeɪnts";
		final String sp = "U 1";
		final String expected = "l ˈb";
		
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		Assert.assertEquals(expected, ipa.findStressPattern(sp).get(0).toString());
	}
	
}
