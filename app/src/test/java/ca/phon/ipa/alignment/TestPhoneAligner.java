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
	
	private Session openSession() throws IOException {
		final SessionInputFactory factory = new SessionInputFactory();
		final SessionIO readerIO = factory.availableReaders().get(0);
		final SessionReader reader = factory.createReader(readerIO);
		
		final InputStream is = getClass().getResourceAsStream(SESSION_FILE);
		return reader.readSession(is);
	}
	
	public void testAligner() throws Exception {
		final Session session = openSession();
		
		int numTargets = 0;
		int numOldCorrect = 0;
		int numNewCorrect = 0;
		
		final PhoneAligner oldAligner = new PhoneAligner();
		final IndelPhoneAligner newAligner = new IndelPhoneAligner();
		
		for(int rIdx = 0; rIdx < session.getRecordCount(); rIdx++) {
			final Record r = session.getRecord(rIdx);
			final Group g = r.getGroup(0);
			
			final PhoneMap correctAlignment = g.getPhoneAlignment();
			numTargets += correctAlignment.getAlignmentLength();
		
			final PhoneMap oldAlignment = oldAligner.calculatePhoneMap(g.getIPATarget(), g.getIPAActual());
			final PhoneMap newAlignment = newAligner.calculatePhoneAlignment(g.getIPATarget(), g.getIPAActual());
			
			if(oldAlignment.toString().equals(correctAlignment.toString())) {
				numOldCorrect += correctAlignment.getAlignmentLength();
			} else {
				numOldCorrect += checkCorrect(g, correctAlignment, oldAlignment);
			}
			
			if(newAlignment.toString().equals(correctAlignment.toString())) {
				numNewCorrect += correctAlignment.getAlignmentLength();
			} else {
				numNewCorrect += checkCorrect(g, correctAlignment, newAlignment);
				
				System.out.println();
				System.out.println("Record:\t" + (rIdx+1));
				System.out.println("Correct:\t" + correctAlignment );
				System.out.println("Old Aligner:\t" + oldAlignment + 
						" (" + ((checkCorrect(g, correctAlignment, oldAlignment)/(float)correctAlignment.getAlignmentLength()) * 100.0f) + "%)");
				System.out.println("New Aligner:\t" + newAlignment +
						" (" + ((checkCorrect(g, correctAlignment, newAlignment)/(float)correctAlignment.getAlignmentLength()) * 100.0f) + "%)");
			}
		}
		
		float pOldCorrect = (numOldCorrect/(float)numTargets) * 100.0f;
		float pNewCorrect = (numNewCorrect/(float)numTargets) * 100.0f;
		
		System.out.println("Old: " + pOldCorrect);
		System.out.println("New: " + pNewCorrect);
	}
	
	public int checkCorrect(Group g, PhoneMap correct, PhoneMap align) {
		List<IPAElement> targetEles = new ArrayList<>();
		for(IPAElement ele:g.getIPATarget().audiblePhones()) targetEles.add(ele);
		
		List<IPAElement> actualEles = new ArrayList<>();
		for(IPAElement ele:g.getIPAActual().audiblePhones()) actualEles.add(ele);
		
		int tally = 0;
		for(IPAElement ele:targetEles) {
			List<IPAElement> correctAligned = correct.getAligned(new IPAElement[]{ele});
			List<IPAElement> testAligned = align.getAligned(new IPAElement[]{ele});
			
			if(correctAligned.size() > 0 && correctAligned.size() == testAligned.size()
					&& correctAligned.get(0) == testAligned.get(0)) {
				++tally;
				actualEles.remove(correctAligned.get(0));
			} else if(correctAligned.size() == 0 && testAligned.size() == 0) {
				++tally;
			}
		}
		
		for(IPAElement ele:actualEles) {
			List<IPAElement> correctAligned = correct.getAligned(new IPAElement[]{ele});
			List<IPAElement> testAligned = align.getAligned(new IPAElement[]{ele});
			
			if(correctAligned.size() > 0 && correctAligned.size() == testAligned.size()
					&& correctAligned.get(0) == testAligned.get(0)) {
				++tally;
			} else if(correctAligned.size() == 0 && testAligned.size() == 0) {
				++tally;
			}
		}
		
		
		return tally;
	}
	
	public static void main(String[] args) {
		TestPhoneAligner test = new TestPhoneAligner();
		try {
			test.testAligner();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
