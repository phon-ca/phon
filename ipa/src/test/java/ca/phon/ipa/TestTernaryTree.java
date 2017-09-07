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
package ca.phon.ipa;

import java.io.*;
import java.text.ParseException;
import java.util.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.features.IPAElementComparator;
import ca.phon.ipa.tree.IpaTernaryTree;

@RunWith(JUnit4.class)
public class TestTernaryTree {
	
	private final static String IPA_FILE = "ipa.txt";

	@Test
	public void testTreeOrdering() throws IOException, ParseException {
		final Comparator<IPAElement> comparator = new IPAElementComparator();
		
		final IpaTernaryTree<List<IPATranscript>> tree = 
				new IpaTernaryTree<List<IPATranscript>>(comparator);
		
		final InputStream is = getClass().getResourceAsStream(IPA_FILE);
		final BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		String line = null;
		while((line = in.readLine()) != null) {
			final String[] parts = line.split("\\p{Space}");
			
			// parse transcript
			final IPATranscript ipa = IPATranscript.parseIPATranscript(parts[1]);
			final IPATranscript key = ipa.removePunctuation(true);
			List<IPATranscript> ipaList = tree.get(key);
			if(ipaList == null) {
				ipaList = new ArrayList<IPATranscript>();
				tree.put(key, ipaList);
			}
			ipaList.add(ipa);
		}
		
		for(IPATranscript key:tree.keySet()) {
			final List<IPATranscript> ipaList = tree.get(key);
			for(IPATranscript ipa:ipaList) {
				System.out.println(ipa);
			}
		}
	}
	
}
