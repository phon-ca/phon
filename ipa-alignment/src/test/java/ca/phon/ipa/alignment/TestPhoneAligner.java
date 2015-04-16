/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
