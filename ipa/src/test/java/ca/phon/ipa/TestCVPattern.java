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
