package ca.phon.ipa.alignment;

import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;

@RunWith(JUnit4.class)
public class TestPhoneAligner {

	@Test
	public void testAligner() throws Exception {
		final PhoneAligner aligner = new PhoneAligner();
		
		final IPATranscript model = IPATranscript.parseTranscript("ˈhæpiː ˈbʌɹθˌdeɪ");
		final IPATranscript actual = IPATranscript.parseTranscript("ˈæpiː ˈbʌːˌteɪ");
		
		final PhoneMap pm = aligner.calculatePhoneMap(model, actual);
		Assert.assertNotNull(pm);
		Assert.assertEquals(pm.getAlignmentLength(), model.removePunctuation().size());
	
		// test individual alignment
		for(final IPAElement ele:model.removePunctuation()) {
			final List<IPAElement> top = Collections.singletonList(ele);
			final List<IPAElement> aligned = pm.getAligned(top);
			if(aligned.size() > 0) {
				final List<IPAElement> reverse = pm.getAligned(aligned);
				Assert.assertEquals(top, reverse);
			}
		}
		
		{
			final List<IPAElement> top = model.removePunctuation().subList(0, 3);
			final List<IPAElement> expected = actual.removePunctuation().subList(0, 2);
			final List<IPAElement> aligned = pm.getAligned(top);
			Assert.assertEquals(expected, aligned);
			final List<IPAElement> reverse = pm.getAligned(aligned);
			final List<IPAElement> expectedReverse = top.subList(1, top.size());
			Assert.assertEquals(expectedReverse, reverse);
		}
		
		{
			final List<IPAElement> top = model.removePunctuation().subList(6, 8);
			final List<IPAElement> expected = Collections.emptyList();
			final List<IPAElement> aligned = pm.getAligned(top);
			Assert.assertEquals(expected, aligned);
		}
		
	}
	
	
}
