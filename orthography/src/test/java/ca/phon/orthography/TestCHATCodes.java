/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.orthography;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;

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
