/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.ipa;

import ca.phon.ipa.features.IPAElementComparator;
import ca.phon.ipa.tree.IpaTernaryTree;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.*;
import java.text.ParseException;
import java.util.*;

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
