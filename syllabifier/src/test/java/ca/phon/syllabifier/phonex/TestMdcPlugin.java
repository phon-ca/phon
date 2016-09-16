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
package ca.phon.syllabifier.phonex;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.IPATranscript;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;
import junit.framework.Assert;

/**
 * Test sonority distance phonex plug-ins.
 *
 */
@RunWith(JUnit4.class)
public class TestMdcPlugin {

	@Test
	public void testMDCPlugin() throws Exception {
		final String text = "ktki";
		final String phonex = "(\\c)(\\c:mdc(\"0\",\"true\"))";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		Assert.assertEquals(true, matcher.find());
		Assert.assertEquals(ipa.subsection(0, 2).toList(), matcher.group());
	}
	
}
