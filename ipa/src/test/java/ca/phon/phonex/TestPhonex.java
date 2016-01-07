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
package ca.phon.phonex;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.IPATranscript;

@RunWith(JUnit4.class)
public class TestPhonex {

	@Test(expected=NoSuchPluginException.class)
	public void testMissingPlugin() throws Exception {
		final String phonex = "\\v:noplugin()";
		PhonexPattern.compile(phonex);
	}
	
	@Test
	public void testBacktracking() throws Exception {
		final String phonex = "(\\c*\\v+\\c*)*";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final String ipa = "bbaab aba";
		
		final PhonexMatcher matcher = pattern.matcher(IPATranscript.parseIPATranscript(ipa));
		int numMatches = 0;
		while(matcher.find()) {
			++numMatches;
		}
		
		Assert.assertEquals(2, numMatches);
	}
}
