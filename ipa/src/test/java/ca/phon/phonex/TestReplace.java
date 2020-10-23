package ca.phon.phonex;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import ca.phon.ipa.*;

@RunWith(JUnit4.class)
public class TestReplace {

	@Test
	public void testReplace() throws Exception {
		final IPATranscript ipa = IPATranscript.parseIPATranscript("hello world");
		final IPATranscript replace = IPATranscript.parseIPATranscript("\u0254");
		
		final PhonexPattern pattern = PhonexPattern.compile("(\\v)");
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		final IPATranscriptBuilder builder = new IPATranscriptBuilder();
		while(matcher.find()) {
			matcher.appendReplacement(builder, replace);
		}
		matcher.appendTail(builder);
		
		Assert.assertEquals("hɔllɔ wɔrld", builder.toIPATranscript().toString());
	}
	
	@Test
	public void testReplaceWithTone() throws Exception {
		final IPATranscript ipa = IPATranscript.parseIPATranscript("hello\u0304 world");
		final IPATranscript replace = IPATranscript.parseIPATranscript("$1\u0300");
		
		final PhonexPattern pattern = PhonexPattern.compile("({v, -toneextrahigh, -tonehigh, -tonemid, -tonelow, -toneextralow, -tonefalling, -tonerising, -tonelowrising, -tonerisingfalling, -tonefallingrising })");
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		final IPATranscriptBuilder builder = new IPATranscriptBuilder();
		while(matcher.find()) {
			matcher.appendReplacement(builder, replace);
		}
		matcher.appendTail(builder);
		
		Assert.assertEquals("hèllō wòrld", builder.toIPATranscript().toString());
	}
	
}
