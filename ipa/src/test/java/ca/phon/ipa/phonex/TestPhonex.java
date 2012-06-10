package ca.phon.ipa.phonex;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.phone.Phone;
import ca.phon.ipa.phone.phonex.PhonexMatcher;
import ca.phon.ipa.phone.phonex.PhonexPattern;
import ca.phon.util.Range;

import junit.framework.TestCase;

/**
 * Test of various phonex expressions
 *
 */
public class TestPhonex extends TestCase {
	
	public void testGreedyQuantifier() {
		IPATranscript transcript = IPATranscript.parseTranscript("ˈeɪpɹəˌkɑt");
		String phonex = "\\w+?\\w";
		Range[] ranges = {
				new Range(1, 3, false),
				new Range(3, 5, false),
				new Range(7, 9, false)
		};
		performFindTest(phonex, transcript, ranges);
	}
	
	public void testFind() {
		IPATranscript transcript = IPATranscript.parseTranscript("ccaceci");
		String phonex = ".*?\\v";
		Range[] ranges = {
				new Range(0, 3, false),
				new Range(3, 5, false),
				new Range(5, 7, false)
		};
		performFindTest(phonex, transcript, ranges);
		performMatchTest(phonex, transcript, true, new Range[0]);
	}
	
	public void testCompoundPhoneMatcher() {
		IPATranscript transcript = IPATranscript.parseTranscript("t͜ʃemɪarst");
		String phonex = "(t_{fricative}).*(t)";
		Range[] ranges = {
				new Range(0, 1, false),
				new Range(transcript.size()-1, transcript.size(), false)
		};
		performMatchTest(
				phonex, transcript, true, ranges);
		
		transcript = IPATranscript.parseTranscript("stt͜ʃemɪar");
		phonex = "(\\c)*(._.)(.*)";
		ranges = new Range[] {
				new Range(1, 2, false),
				new Range(2, 3, false),
				new Range(3, 8, false)
		};
		performMatchTest(
				phonex, transcript, true, ranges);
	}
	
	private void performMatchTest(
			String phonex,
			List<Phone> ipa,
			boolean matches,
			Range ... ranges) {
		PhonexPattern pattern = PhonexPattern.compile(phonex);
		PhonexMatcher matcher = pattern.matcher(ipa);
		
		boolean m = matcher.matches();
		
		assertEquals(matches, m);
		if(m) {
			// ensure group values are the same
			assertEquals(ranges.length, matcher.groupCount());
			for(int i = 1; i <= matcher.groupCount(); i++) {
				List<Phone> group = matcher.group(i);
				List<Phone> testGroup =
						ipa.subList(ranges[i-1].getStart(), ranges[i-1].getEnd());
				
				assertEquals(group.size(), testGroup.size());
				for(int j = 0; j < group.size(); j++) {
					Phone gPhone = group.get(j);
					Phone tPhone = testGroup.get(j);
					
					assertEquals(gPhone, tPhone);
				}
			}
		}
	}
	
	private void performFindTest(
			String phonex,
			List<Phone> ipa,
			Range ... ranges) {
		PhonexPattern pattern = PhonexPattern.compile(phonex);
		PhonexMatcher matcher = pattern.matcher(ipa);
		
		int numFound = 0;
		
		while(matcher.find()) {
			
			Range r = ranges[numFound++];
			assertEquals(r.getStart(), matcher.start());
			assertEquals(r.getEnd(), matcher.end());
		}
		assertEquals(ranges.length, numFound);
	}

}
