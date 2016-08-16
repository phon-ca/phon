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

import java.text.ParseException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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
