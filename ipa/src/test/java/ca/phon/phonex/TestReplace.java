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
package ca.phon.phonex;

import ca.phon.ipa.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestReplace {

	@Test
	public void testReplace() throws Exception {
		final IPATranscript ipa = IPATranscript.parseIPATranscript("hello world");
		final IPATranscript replace = IPATranscript.parseIPATranscript("\u0254");
		
		final PhonexPattern pattern = PhonexPattern.compile("(\\v)");
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		final IPATranscriptBuilder builder = new IPATranscriptBuilder();
		while(matcher.find()) {
			matcher.appendReplacement(builder, replace);
		}
		matcher.appendTail(builder);
		
		Assert.assertEquals("hɔllɔ wɔrld", builder.toIPATranscript().toString());
	}
	
	@Test
	public void testReplaceWithTone() throws Exception {
		final IPATranscript ipa = IPATranscript.parseIPATranscript("hello\u0304 world");
		final IPATranscript replace = IPATranscript.parseIPATranscript("\\1\u0300");
		
		final PhonexPattern pattern = PhonexPattern.compile("({v, -toneextrahigh, -tonehigh, -tonemid, -tonelow, -toneextralow, -tonefalling, -tonerising, -tonelowrising, -tonerisingfalling, -tonefallingrising })");
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		final IPATranscriptBuilder builder = new IPATranscriptBuilder();
		while(matcher.find()) {
			matcher.appendReplacement(builder, replace);
		}
		matcher.appendTail(builder);
		
		Assert.assertEquals("hèllō wòrld", builder.toIPATranscript().toString());
	}
	
}
