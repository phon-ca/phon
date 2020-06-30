package ca.phon.phonex;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.IPATranscript;

@RunWith(JUnit4.class)
public class TestSyllableMatcher extends PhonexTest {

	@Test
	public void testSyllableMatcherWithQuantifier() throws Exception {
		final String text = "b:oa:Nn:Cd:Oa:Nk:Oa:N";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "(\u03c3+)";
		final IPATranscript[][] answers = {
				{ ipa.subsection(0, 7) },
		};
		
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testMultiWord() throws Exception {
		final String text = "b:oa:N d:Oa:Nk:Oa:N";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "(σ+(?=' 'σ+)*)";
		final IPATranscript[][] answers = {
				{ ipa.subsection(0, 7) },
		};
		
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testSyllableRange() throws Exception {
		final String text = "b:oa:Nn:Cd:Oa:Nk:Oa:N";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "(σ/O..N/+)";
		final IPATranscript[][] answers = {
				{ ipa.subsection(0, 2) }, { ipa.subsection(3, 7) },
		};
		
		testGroups(ipa, phonex, answers);
	}
	
}
