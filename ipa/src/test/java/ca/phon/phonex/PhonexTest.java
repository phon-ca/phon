package ca.phon.phonex;

import java.util.List;
import java.util.Map;

import ca.phon.ipa.IPATranscript;
import junit.framework.Assert;

public class PhonexTest {

	public void testGroups(IPATranscript t, String phonex, IPATranscript[][] groupData) {
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(t);
		
		System.out.println(pattern.getFsa().getDotText());
		
		int idx = 0;
		while(matcher.find()) {
			boolean expectingData = idx < groupData.length;
			Assert.assertTrue(expectingData);
			IPATranscript[] data = groupData[idx++];
			
			Assert.assertEquals(data.length, matcher.groupCount());
			for(int i = 1; i <= matcher.groupCount(); i++) {
				final IPATranscript test = new IPATranscript(matcher.group(i));
				Assert.assertEquals(data[i-1], test);
			}
		}
		Assert.assertEquals(groupData.length, idx);
	}
	
	public void testNamedGroups(IPATranscript t, String phonex, List<Map<String, IPATranscript>> groupData) {
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(t);

		int idx = 0;
		while(matcher.find()) {
			boolean expectingData = idx < groupData.size();
			Assert.assertTrue(expectingData);
			Map<String, IPATranscript> data = groupData.get(idx++);
			
			// make sure groups exist
			for(String groupName:data.keySet()) {
				int groupIdx = pattern.groupIndex(groupName);
				Assert.assertTrue(groupIdx > 0);
				
				final IPATranscript test = new IPATranscript(matcher.group(groupIdx));
				Assert.assertEquals(data.get(groupName), test);				
			}
		}	
		Assert.assertEquals(groupData.size(), idx);
	}
	
}
