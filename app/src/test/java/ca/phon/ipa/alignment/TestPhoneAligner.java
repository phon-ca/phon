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
package ca.phon.ipa.alignment;

import java.io.*;
import java.util.*;

import ca.phon.ipa.IPAElement;
import ca.phon.session.*;
import ca.phon.session.io.*;

public class TestPhoneAligner {

	private final static String SESSION_FILE = "Alignment Examples.xml";
	
	private final int OLD_ALIGNER = 0;
	private final int NEW_ALIGNER = 1;
	
	private final int CORRECT = 0;
	private final int SUB = 1;
	
	private Session openSession() throws IOException {
		final SessionInputFactory factory = new SessionInputFactory();
		final SessionIO readerIO = factory.availableReaders().get(0);
		final SessionReader reader = factory.createReader(readerIO);
		
		final InputStream is = getClass().getResourceAsStream(SESSION_FILE);
		return reader.readSession(is);
	}
	
	public void testAligner() throws Exception {
		final Session session = openSession();
		
		 
		int numWords = 0;
		int numWordsCorrect[] = {0, 0};
		
		int numSegmentalCorrect[] = {0, 0};
		int numSegmentalSub[] = {0, 0};
		
		final PhoneAligner oldAligner = new PhoneAligner();
		final IndelPhoneAligner newAligner = new IndelPhoneAligner();
		
		for(int rIdx = 0; rIdx < session.getRecordCount(); rIdx++) {
			final Record r = session.getRecord(rIdx);
			final Group g = r.getGroup(0);
			++numWords;
			
			final PhoneMap correctAlignment = g.getPhoneAlignment();
		
			final PhoneMap oldAlignment = oldAligner.calculatePhoneMap(g.getIPATarget(), g.getIPAActual());
			final PhoneMap newAlignment = newAligner.calculatePhoneAlignment(g.getIPATarget(), g.getIPAActual());
			
			int oldCheck[] = checkCorrect(g, correctAlignment, oldAlignment);
			numSegmentalCorrect[OLD_ALIGNER] += oldCheck[CORRECT];
			numSegmentalSub[OLD_ALIGNER] += oldCheck[SUB];
			
			if(oldCheck[CORRECT] == correctAlignment.getAlignmentLength()) {
				numWordsCorrect[OLD_ALIGNER]++;
			}
			
			int newCheck[] = checkCorrect(g, correctAlignment, newAlignment);
			numSegmentalCorrect[NEW_ALIGNER] += newCheck[CORRECT];
			numSegmentalSub[NEW_ALIGNER] += newCheck[SUB];
			
			if(newCheck[CORRECT] == correctAlignment.getAlignmentLength()) {
				numWordsCorrect[NEW_ALIGNER]++;
			}
		}
		
		float pWordsCorrect[] = new float[2];
		pWordsCorrect[OLD_ALIGNER] = (numWordsCorrect[OLD_ALIGNER]/(float)numWords) * 100.0f;
		pWordsCorrect[NEW_ALIGNER] = (numWordsCorrect[NEW_ALIGNER]/(float)numWords) * 100.0f;
		
		float pSegmentalCorrect[] = new float[2];
		pSegmentalCorrect[OLD_ALIGNER] = 
				(numSegmentalCorrect[OLD_ALIGNER]/(float)(numSegmentalCorrect[OLD_ALIGNER]+numSegmentalSub[OLD_ALIGNER])) * 100.0f;
		pSegmentalCorrect[NEW_ALIGNER] = 
				(numSegmentalCorrect[NEW_ALIGNER]/(float)(numSegmentalCorrect[NEW_ALIGNER]+numSegmentalSub[NEW_ALIGNER])) * 100.0f;
		
		System.out.println("Word: \tO = " + pWordsCorrect[OLD_ALIGNER] + "%\t N = " + pWordsCorrect[NEW_ALIGNER] + "%");
		System.out.println("Phone: \tO = " + pSegmentalCorrect[OLD_ALIGNER] + "%\t N = " + pSegmentalCorrect[NEW_ALIGNER] + "%");
	}
	
	public int[] checkCorrect(Group g, PhoneMap correct, PhoneMap align) {
		int retVal[] = new int[2];
		
		List<IPAElement> targetEles = new ArrayList<>();
		for(IPAElement ele:g.getIPATarget().audiblePhones()) targetEles.add(ele);
		
		List<IPAElement> actualEles = new ArrayList<>();
		for(IPAElement ele:g.getIPAActual().audiblePhones()) actualEles.add(ele);
		
		for(IPAElement ele:targetEles) {
			List<IPAElement> correctAligned = correct.getAligned(new IPAElement[]{ele});
			List<IPAElement> testAligned = align.getAligned(new IPAElement[]{ele});
			
			if(correctAligned.size() > 0 && correctAligned.size() == testAligned.size()
					&& correctAligned.get(0) == testAligned.get(0)) {
				retVal[CORRECT]++;
				actualEles.remove(correctAligned.get(0));
			} else if(correctAligned.size() == 0 && testAligned.size() == 0) {
				retVal[CORRECT]++;
			} else {
				retVal[SUB]++;
			}
		}
		
		for(IPAElement ele:actualEles) {
			List<IPAElement> correctAligned = correct.getAligned(new IPAElement[]{ele});
			List<IPAElement> testAligned = align.getAligned(new IPAElement[]{ele});
			
			if(correctAligned.size() > 0 && correctAligned.size() == testAligned.size()
					&& correctAligned.get(0) == testAligned.get(0)) {
				retVal[CORRECT]++;
			} else if(correctAligned.size() == 0 && testAligned.size() == 0) {
				retVal[CORRECT]++;
			} else {
				retVal[SUB]++;
			}
		}
		
		return retVal;
	}
	
	public static void main(String[] args) {
		TestPhoneAligner test = new TestPhoneAligner();
		try {
			test.testAligner();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
