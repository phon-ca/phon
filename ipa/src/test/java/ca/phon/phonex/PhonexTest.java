/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
