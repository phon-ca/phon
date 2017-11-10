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
		
		int numExamples = session.getRecordCount();
		int numOldCorrect = 0;
		int numNewCorrect = 0;
		
		final PhoneAligner oldAligner = new PhoneAligner();
		final IndelPhoneAligner newAligner = new IndelPhoneAligner();
		
		for(int rIdx = 0; rIdx < numExamples; rIdx++) {
			final Record r = session.getRecord(rIdx);
			final Group g = r.getGroup(0);
			
			final PhoneMap correctAlignment = g.getPhoneAlignment();
		
			final PhoneMap oldAlignment = oldAligner.calculatePhoneMap(g.getIPATarget(), g.getIPAActual());
			final PhoneMap newAlignment = newAligner.calculatePhoneAlignment(g.getIPATarget(), g.getIPAActual());
			
			if(oldAlignment.toString().equals(correctAlignment.toString())) {
				++numOldCorrect;
			}
			if(newAlignment.toString().equals(correctAlignment.toString())) {
				++numNewCorrect;
			} else {
				System.out.println(correctAlignment);
				System.out.println(newAlignment);
				
				System.out.println(rIdx+1);
			}
			
			
		}
		
		float pOldCorrect = (numOldCorrect/(float)numExamples) * 100.0f;
		float pNewCorrect = (numNewCorrect/(float)numExamples) * 100.0f;
		
		System.out.println("Old: " + pOldCorrect);
		System.out.println("New: " + pNewCorrect);
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
