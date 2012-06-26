package ca.phon.ipa;

import java.text.ParseException;

import junit.framework.TestCase;

/**
 * Test methods involving phonex in the IPATranscript class.
 *
 */
public class TestIPATranscriptPhonex extends TestCase {
	
	public void testSplit() {
		final String ipa = "hello";
		try {
			final IPATranscript transcript = IPATranscript.parseTranscript(ipa);
			
			final IPATranscript[] splitVals = transcript.split("\\v");
			assertEquals(2, splitVals.length);
			assertEquals(splitVals[0], new IPATranscript(transcript.subList(0,1)));
			assertEquals(splitVals[1], new IPATranscript(transcript.subList(2,4)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
