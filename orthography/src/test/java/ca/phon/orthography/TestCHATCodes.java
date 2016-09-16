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
package ca.phon.orthography;

import java.text.ParseException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestCHATCodes {
	
	/**
	 * Test shortenings
	 * 
	 * E.g., hel<l>o
	 * 
	 * @throws ParseException
	 */
//	@Test
//	public void testShortening() throws ParseException {
//		final String testStr = "hel<l>o";
//		Orthography ortho = testString(testStr);
//		
//		Assert.assertEquals(testStr, ortho.toString());
//	}

	@Test
	public void testEvents() throws ParseException {
		final String testStr = "(happening: test)";
		Orthography ortho = testString(testStr);
		
		Assert.assertEquals(testStr, ortho.toString());
	}
	
	/**
	 * Prarse orthography, throw any errors
	 * 
	 * @param str
	 * @return
	 * @throws ParseException
	 */
	private Orthography testString(String str) throws ParseException {
		return Orthography.parseOrthography(str);
	}
	
	@Test
	public void testInnerGroup() throws ParseException {
		final String testStr = "{maman (/) }";
		Orthography orth = testString(testStr);
	}
	
}
