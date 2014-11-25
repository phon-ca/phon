package ca.phon.ipa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.tree.IpaTernaryTree;

@RunWith(JUnit4.class)
public class TestTernaryTree {
	
	private final static String IPA_FILE = "ipa.txt";

	@Test
	public void testTreeOrdering() throws IOException, ParseException {
		final ca.phon.ipa.features.FeatureComparator comparator = ca.phon.ipa.features.FeatureComparator.createPlaceComparator();
		
		final IpaTernaryTree<List<IPATranscript>> tree = new IpaTernaryTree<List<IPATranscript>>(comparator);
		
		final InputStream is = getClass().getResourceAsStream(IPA_FILE);
		final BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		String line = null;
		while((line = in.readLine()) != null) {
			final String[] parts = line.split("\\p{Space}");
			
			// parse transcript
			final IPATranscript ipa = IPATranscript.parseIPATranscript(parts[1]);
			final IPATranscript key = ipa.removePunctuation();
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
