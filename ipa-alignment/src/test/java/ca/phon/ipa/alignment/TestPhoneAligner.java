package ca.phon.ipa.alignment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestPhoneAligner {

	@Test
	public void testAligner() throws Exception {
		final PhoneAligner aligner = new PhoneAligner();
//		
//		final IPATranscript model = IPATranscript.parseIPATranscript("ˈhæpiː ˈbʌɹθˌdeɪ");
//		final IPATranscript actual = IPATranscript.parseIPATranscript("ˈæpiː ˈbʌːˌteɪ");
//		
//		final PhoneMap pm = aligner.calculatePhoneMap(model, actual);
//		Assert.assertNotNull(pm);
//		Assert.assertEquals(pm.getAlignmentLength(), model.removePunctuation().length());
//	
//		// test individual alignment
//		for(final IPAElement ele:model.removePunctuation()) {
//			final List<IPAElement> top = Collections.singletonList(ele);
//			final List<IPAElement> aligned = pm.getAligned(top);
//			if(aligned.size() > 0) {
//				final List<IPAElement> reverse = pm.getAligned(aligned);
//				Assert.assertEquals(top, reverse);
//			}
//		}
//		
//		{
//			final IPATranscript top = model.removePunctuation().subsection(0, 3);
//			final IPATranscript expected = actual.removePunctuation().subsection(0, 2);
//			final IPATranscript aligned = new IPATranscript(pm.getAligned(top.toList()));
//			Assert.assertEquals(expected.toString(), aligned.toString());
//			
//			final List<IPAElement> reverse = pm.getAligned(aligned.toList());
//			final List<IPAElement> expectedReverse = top.subsection(1, top.length()).toList();
//			Assert.assertEquals(expectedReverse, reverse);
//		}
//		
//		{
//			final List<IPAElement> top = model.removePunctuation().subsection(6, 8).toList();
//			final List<IPAElement> expected = Collections.emptyList();
//			final List<IPAElement> aligned = pm.getAligned(top);
//			Assert.assertEquals(expected, aligned);
//		}
		
	}
	
	
}
