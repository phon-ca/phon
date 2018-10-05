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
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import junit.framework.Assert;

@RunWith(JUnit4.class)
public class TestCVPattern {

	@Test
	public void testContains() throws ParseException {
		final String txt = "ˈk:Oə:Nn:Cˌs:Lt:Oɹ:Oe:Dɪ:Dn:Ct:Cs:R";
		final String pattern = "CV+";
		
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		Assert.assertEquals(true, ipa.containsCVPattern(pattern));
	}
	
	@Test
	public void testMatches() throws ParseException {
		final String txt = "ˈk:Oə:Nn:Cˌs:Lt:Oɹ:Oe:Dɪ:Dn:Ct:Cs:R";
		final String pattern = "CA+C";
		
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		Assert.assertEquals(true, ipa.matchesCVPattern(pattern));
	}

	@Test
	public void testFind() throws ParseException {
		final String txt = "ˈk:Oə:Nn:Cˌs:Lt:Oɹ:Oe:Dɪ:Dn:Ct:Cs:R";
		final String pattern = "CV+";
		final String[] expected = { "kə", "ɹeɪ" };
		
		final IPATranscript ipa = IPATranscript.parseIPATranscript(txt);
		final List<IPATranscript> found = ipa.findCVPattern(pattern);
		
		Assert.assertEquals(expected.length, found.size());
		for(int i = 0; i < expected.length; i++) {
			Assert.assertEquals(expected[i], found.get(i).toString());
		}
	}
	
}
