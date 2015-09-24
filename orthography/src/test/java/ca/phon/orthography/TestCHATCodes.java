package ca.phon.orthography;

import java.text.ParseException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import junit.framework.Assert;

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
	
}
