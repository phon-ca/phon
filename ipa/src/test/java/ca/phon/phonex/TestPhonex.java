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

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.ipa.IPATranscript;
import junit.framework.Assert;

@RunWith(JUnit4.class)
public class TestPhonex extends PhonexTest {

	@Test(expected=NoSuchPluginException.class)
	public void testMissingPlugin() throws Exception {
		final String phonex = "\\v:noplugin()";
		PhonexPattern.compile(phonex);
	}

	@Test(expected=PhonexPatternException.class)
	public void textInvalidFeature() throws Exception {
		PhonexPattern.compile("{invalid}");
	}
	
	@Test
	public void testComment1() throws Exception {
		final String text = "bbadd";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "/* First consonant */(\\c) /* Second consonant */ (\\c)";
		PhonexPattern.compile(phonex);
		
		final IPATranscript[][] answers = {
				{ ipa.subsection(0, 1), ipa.subsection(1, 2) },
				{ ipa.subsection(3, 4), ipa.subsection(4, 5) }
		};
		
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testComment2() throws Exception {
		final String text = "bbadd";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "/* First consonant */(\\c) (/* Second consonant */ \\c)";
		PhonexPattern.compile(phonex);
		
		final IPATranscript[][] answers = {
				{ ipa.subsection(0, 1), ipa.subsection(1, 2) },
				{ ipa.subsection(3, 4), ipa.subsection(4, 5) }
		};
		
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testComment3() throws Exception {
		final String text = "bbadd";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "/* First consonant */\n"
				+ "(\\c) (/* Second consonant */ \\c)";
		PhonexPattern.compile(phonex);
		
		final IPATranscript[][] answers = {
				{ ipa.subsection(0, 1), ipa.subsection(1, 2) },
				{ ipa.subsection(3, 4), ipa.subsection(4, 5) }
		};
		
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testComment4() throws Exception {
		final String text = "bbadd";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "// First consonant\n"
				+ "(\\c)\n"
				+ "// Second consonant\n"
				+ "(\\c)";
		PhonexPattern.compile(phonex);
		
		final IPATranscript[][] answers = {
				{ ipa.subsection(0, 1), ipa.subsection(1, 2) },
				{ ipa.subsection(3, 4), ipa.subsection(4, 5) }
		};
		
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testBacktracking() throws Exception {
		final String phonex = "(\\c*\\v+\\c*)*";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final String ipa = "bbaab aba";

		final PhonexMatcher matcher = pattern.matcher(IPATranscript.parseIPATranscript(ipa));
		int numMatches = 0;
		while(matcher.find()) {
			++numMatches;
		}

		Assert.assertEquals(2, numMatches);
	}

	@Test
	public void testCompoundPhoneMatcher() throws ParseException {
		final String text = "st\u0361\u0283aad\u035c\u0292";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		// look for all compound phones
		final String phonex = "._.";
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		int numFound = 0;
		while(matcher.find()) numFound++;
		Assert.assertEquals(2, numFound);
		
		// look for specific compound phones
	}
	
	@Test
	public void testRegexMatcher() throws ParseException {
		final String text =  "st\u0361\u0283aad\u035c\u0292";
		final String phonex = "'t.+'";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		Assert.assertEquals(true, matcher.find());
	}
	
	@Test
	public void testFeatureSetMatcher() throws ParseException {
		
	}

	@Test
	public void testUnicodeValueMatcher() throws ParseException {
		
	}

	@Test
	public void testAlignmentMarker() throws ParseException {
		final String text = "ba \u2194 ab";
		final String phonex = "(\\w+)\\b\u2194\\b(\\w+)";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		Assert.assertEquals(true, matcher.matches());
		Assert.assertEquals(ipa.subsection(0, 2).toList(), matcher.group(1));
		Assert.assertEquals(ipa.subsection(5, 7).toList(), matcher.group(2));
	}
	
	@Test
	public void testEmptyQuery() throws ParseException {
		final IPATranscript ipa = new IPATranscript();
		boolean matches = ipa.matches("^$");
		
		Assert.assertEquals(true, matches);
	}
	
	@Test
	public void testLookAheadBehind() throws ParseException {
		final String text = "ˈkʀət͡jə";
		final String phonex = "(?<\\w)\\c(?>\\w)";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		Assert.assertTrue(matcher.find());
		Assert.assertEquals(ipa.elementAt(2), matcher.group().get(0));
	}
	
	@Test
	public void testLookBehindWithBoundary() throws ParseException {
		final String text = "ˈk:oʀ:oi:di:dt͡j:oi:n";
		// Beginning of input boundary marker must be inside look-behind group
		final String phonex = "(?<\\S\\c:L*)\\c:O(?>\\w:sctype(\"-O\"))";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final PhonexPattern pattern = PhonexPattern.compile(phonex);
		
		final PhonexMatcher matcher = pattern.matcher(ipa);
		
		Assert.assertTrue(matcher.find());
		Assert.assertEquals(ipa.elementAt(5), matcher.group().get(0));
	}
	
	@Test
	public void testGrouping1() throws ParseException {
		final String text = "zuˈkini";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "\\S(\\c)(\\v)";
		IPATranscript[][] answers = {
				{ ipa.subsection(0, 1), ipa.subsection(1, 2) }, 
				{ ipa.subsection(3, 4), ipa.subsection(4, 5) },
				{ ipa.subsection(5, 6), ipa.subsection(6, 7) },
				
		};
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testNamedGrouping1() throws ParseException {
		final String text = "zuˈkini";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "\\S(C=\\c)(V=\\v)";
		
		final Map<String, IPATranscript> hm1 = new HashMap<>();
		hm1.put("C", ipa.subsection(0, 1));
		hm1.put("V", ipa.subsection(1, 2));
		
		final Map<String, IPATranscript> hm2 = new HashMap<>();
		hm2.put("C", ipa.subsection(3, 4));
		hm2.put("V", ipa.subsection(4, 5));
		
		final Map<String, IPATranscript> hm3 = new HashMap<>();
		hm3.put("C", ipa.subsection(5, 6));
		hm3.put("V", ipa.subsection(6, 7));
		
		final List<Map<String, IPATranscript>> answers = List.of(hm1, hm2, hm3);
		testNamedGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testGrouping2() throws ParseException {
		final String text = "zuˈkini";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "(\\S(\\c)(\\v))";
		IPATranscript[][] answers = {
				{ ipa.subsection(0, 2), ipa.subsection(0, 1), ipa.subsection(1, 2) }, 
				{ ipa.subsection(2, 5), ipa.subsection(3, 4), ipa.subsection(4, 5) },
				{ ipa.subsection(5, 7), ipa.subsection(5, 6), ipa.subsection(6, 7) },
				
		};
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testNamedGrouping2() throws ParseException {
		final String text = "zuˈkini";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "(\\S(C=\\c)(V=\\v))";
		
		final Map<String, IPATranscript> hm1 = new HashMap<>();
		hm1.put("C", ipa.subsection(0, 1));
		hm1.put("V", ipa.subsection(1, 2));
		
		final Map<String, IPATranscript> hm2 = new HashMap<>();
		hm2.put("C", ipa.subsection(3, 4));
		hm2.put("V", ipa.subsection(4, 5));
		
		final Map<String, IPATranscript> hm3 = new HashMap<>();
		hm3.put("C", ipa.subsection(5, 6));
		hm3.put("V", ipa.subsection(6, 7));
		
		final List<Map<String, IPATranscript>> answers = List.of(hm1, hm2, hm3);
		testNamedGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testGrouping3() throws ParseException {
		final String text = "zuˈkini";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "(\\S((\\c)(\\v)))";
		IPATranscript[][] answers = {
				{ ipa.subsection(0, 2), ipa.subsection(0, 2), ipa.subsection(0, 1), ipa.subsection(1, 2) }, 
				{ ipa.subsection(2, 5), ipa.subsection(3, 5), ipa.subsection(3, 4), ipa.subsection(4, 5) },
				{ ipa.subsection(5, 7), ipa.subsection(5, 7), ipa.subsection(5, 6), ipa.subsection(6, 7) },
				
		};
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testNamedGrouping3() throws ParseException {
		final String text = "zuˈkini";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "(syll=\\S((C=\\c)(V=\\v)))";
		
		final Map<String, IPATranscript> hm1 = new HashMap<>();
		hm1.put("C", ipa.subsection(0, 1));
		hm1.put("V", ipa.subsection(1, 2));
		hm1.put("syll", ipa.subsection(0, 2));
		
		final Map<String, IPATranscript> hm2 = new HashMap<>();
		hm2.put("C", ipa.subsection(3, 4));
		hm2.put("V", ipa.subsection(4, 5));
		hm2.put("syll", ipa.subsection(2, 5));
		
		final Map<String, IPATranscript> hm3 = new HashMap<>();
		hm3.put("C", ipa.subsection(5, 6));
		hm3.put("V", ipa.subsection(6, 7));
		hm3.put("syll", ipa.subsection(5, 7));
		
		final List<Map<String, IPATranscript>> answers = List.of(hm1, hm2, hm3);
		testNamedGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testGrouping4() throws ParseException {
		final String text = "ˈkʀət͡jə";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "((\\c)(\\c))(\\v)";
		IPATranscript[][] answers = {
				{ ipa.subsection(1, 3), ipa.subsection(1, 2), ipa.subsection(2, 3), ipa.subsection(3, 4) }
		};
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testNamedGrouping4() throws ParseException {
		final String text = "ˈkʀət͡jə";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "((C1=\\c)(C2=\\c))(V=\\v)";
		
		final HashMap<String, IPATranscript> hm1 = new HashMap<>();
		hm1.put("C1", ipa.subsection(1, 2));
		hm1.put("C2", ipa.subsection(2, 3));
		hm1.put("V", ipa.subsection(3, 4));
		
		final List<Map<String, IPATranscript>> answers = List.of(hm1);
		testNamedGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testOrExprGroups1() throws ParseException {
		final String text = "ˈkʀət͡jə";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "((C1=\\c)(C2=\\c)|(C1=\\c))\\v";
		IPATranscript[][] answers = {
				{ ipa.subsection(1, 3), ipa.subsection(1, 2), ipa.subsection(2, 3) }, 
				{ ipa.subsection(4, 5), ipa.subsection(4, 5), ipa.subsection(0, 0) }
				
		};
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testOrExprGroups2() throws ParseException {
		final String text = "ˈk:On:Oɛ:Di:Dp:Oə:Nɹ:C";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "((C1=\\c:sctype(\"O|LA\"))(C2=\\c:sctype(\"O|LA\"))(C3=\\c:O)|(C1=\\c:sctype(\"O|LA\"))(C2=\\c:O)|(C1=\\c:O)).:N";
		IPATranscript[][] answers = {
				{ ipa.subsection(1, 3), ipa.subsection(1, 2), ipa.subsection(2, 3), ipa.subsection(0, 0) },
				{ ipa.subsection(5, 6), ipa.subsection(5, 6), ipa.subsection(0, 0), ipa.subsection(0, 0) }
				
		};
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testOrExprGroups3() throws ParseException {
		final String text = "k:Oə:Nn:Cˈs:Lt:Oɹ:Oe:Dɪ:Dn:Ct:Cs:R";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "((C1=\\c:L:O)(C2=\\c:L:O)(C3=\\c:L:O)|(C1=\\c:L:O)(C2=\\c:L:O)|(C1=\\c:L:O))";
		IPATranscript[][] answers = {
				{ ipa.subsection(0, 1), ipa.subsection(0, 1), ipa.subsection(0, 0), ipa.subsection(0, 0) },
				{ ipa.subsection(4, 7), ipa.subsection(4, 5), ipa.subsection(5, 6), ipa.subsection(6, 7) }
		};
		
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testOrExprGroups4() throws ParseException {
		final String text = "k:Oə:Nn:Cˈs:Lt:Oɹ:Oe:Dɪ:Dn:Ct:Cs:R";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "( (C1=\\c:L:O)(C2=\\c:L:O)(C3=\\c:O) | (C1=\\c:L:O)(C2=\\c:O) | (C1=\\c:O) ) ( (N1=.:N) (N2=.:N) | (N1=.:N) )";
		IPATranscript[][] answers = {
				{ ipa.subsection(0, 1), ipa.subsection(0, 1), ipa.subsection(0, 0), ipa.subsection(0, 0), ipa.subsection(1, 2), ipa.subsection(1, 2), ipa.subsection(0, 0) },
				{ ipa.subsection(4, 7), ipa.subsection(4, 5), ipa.subsection(5, 6), ipa.subsection(6, 7), ipa.subsection(7, 9), ipa.subsection(7, 8), ipa.subsection(8, 9) }
		};
		
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testOrExprGroups5() throws ParseException {
		final String text = "ˈæ:Nt:C ˈb:Oʌ:Nɹ:Cθ:Cˌd:Oe:Dɪ:D";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = 
			"/* this is a test */ (O=\n" + 
			"	(O1=\\c:L:O)(O2=\\c:L:O)(O3=\\c:O) | (\\c:L:O)(\\c:O) | (\\c:O)\n" + 
			")?\n" + 
			"(R=\n" + 
			"	(N=\n" + 
			"		(N1=.:D)(N2=.:D) | (.:N)\n" + 
			"	)\n" + 
			"	(C=\n" + 
			"		(C1=\\c:C)(C2=\\c:C:R)(C3=\\c:C:R) | (\\c:C)(\\c:C:R) | (\\c:C)\n" + 
			"	)?\n" + 
			")";
		final IPATranscript[][] answers = {
			{   // ˈæ:Nt:C (0-3)
				ipa.subsection(0, 0), ipa.subsection(0, 0), ipa.subsection(0, 0), ipa.subsection(0, 0), // O, O1, O2, O3
				ipa.subsection(1, 3), // R
				ipa.subsection(1, 2), ipa.subsection(1, 2), ipa.subsection(0, 0), // N, N1, N2
				ipa.subsection(2, 3), ipa.subsection(2, 3), ipa.subsection(0, 0), ipa.subsection(0, 0) // C, C1, C2, C3
			},
			{
				// ˈb:Oʌ:Nɹ:Cθ:C (5-9)
				ipa.subsection(5, 6), ipa.subsection(5, 6), ipa.subsection(0, 0), ipa.subsection(0, 0), // O, O1, O2, O3
				ipa.subsection(6, 9), // R
				ipa.subsection(6, 7), ipa.subsection(6, 7), ipa.subsection(0, 0), // N, N1, N2
				ipa.subsection(7, 9), ipa.subsection(7, 8), ipa.subsection(8, 9), ipa.subsection(0, 0) // C, C1, C2, C3
			},
			{
				// ˌd:Oe:Dɪ:D (10-13)
				ipa.subsection(10, 11), ipa.subsection(10, 11), ipa.subsection(0, 0), ipa.subsection(0, 0), // O, O1, O2, O3
				ipa.subsection(11, 13), // R
				ipa.subsection(11, 13), ipa.subsection(11, 12), ipa.subsection(12, 13), // N, N1, N2
				ipa.subsection(0, 0), ipa.subsection(0, 0), ipa.subsection(0, 0), ipa.subsection(0, 0) // C, C1, C2, C3
			}
		};
		
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testOrExprNamedGroups1() throws ParseException {
		final String text = "ˈkʀət͡jə";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "((C1=\\c)(C2=\\c)|(C1=\\c))\\v";
		
		final Map<String, IPATranscript> hm1 = new HashMap<>();
		hm1.put("C1", ipa.subsection(1, 2));
		hm1.put("C2", ipa.subsection(2, 3));
		
		final Map<String, IPATranscript> hm2 = new HashMap<>();
		hm2.put("C1", ipa.subsection(4, 5));
		hm2.put("C2", ipa.subsection(0, 0));
		
		final List<Map<String, IPATranscript>> answers = List.of(hm1, hm2);
		testNamedGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testOrExprNamedGroups2() throws ParseException {
		final String text = "k:Oə:Nn:Cˈs:Lt:Oɹ:Oe:Dɪ:Dn:Ct:Cs:R";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = 
				"(O=\n" + 
				"	(O1=\\c:L:O)(O2=\\c:L:O)(O3=\\c:O) | (\\c:L:O)(\\c:O) | (\\c:O)\n" + 
				")?\n" +
				"(R=\n" + 
				"	(N=\n" + 
				"		(N1=.:N) (N2=.:N) | (.:N)\n" + 
				"	)\n" + 
				"	(C=\n" + 
				"		(C1=\\c:C)(C2=\\c:C:R)(C3=\\c:C:R) | (\\c:C)(\\c:C:R) | (\\c:C)\n" + 
				"	)?\n" + 
				")";
		
		final Map<String, IPATranscript> hm1 = new HashMap<>();
		hm1.put("O", ipa.subsection(0, 1));
		hm1.put("O1", ipa.subsection(0, 1));
		hm1.put("R", ipa.subsection(1, 3));
		hm1.put("N", ipa.subsection(1, 2));
		hm1.put("N1", ipa.subsection(1, 2));
		hm1.put("C", ipa.subsection(2, 3));
		hm1.put("C1", ipa.subsection(2, 3));
		
		final Map<String, IPATranscript> hm2 = new HashMap<>();
		hm2.put("O", ipa.subsection(4, 7));
		hm2.put("O1", ipa.subsection(4, 5));
		hm2.put("O2", ipa.subsection(5, 6));
		hm2.put("O3", ipa.subsection(6, 7));
		hm2.put("R", ipa.subsection(7, 12));
		hm2.put("N", ipa.subsection(7, 9));
		hm2.put("N1", ipa.subsection(7, 8));
		hm2.put("N2", ipa.subsection(8, 9));
		hm2.put("C", ipa.subsection(9, 12));
		hm2.put("C1", ipa.subsection(9, 10));
		hm2.put("C2", ipa.subsection(10, 11));
		hm2.put("C3", ipa.subsection(11, 12));
		
		final List<Map<String, IPATranscript>> answers = List.of(hm1, hm2);
		
		testNamedGroups(ipa, phonex, answers);
	}
	
	@Test(expected=PhonexPatternException.class)
	public void testDuplicateGroupName1() throws ParseException {
		final String phonex = "(C=\\c)(C=\\c)\\v";
		PhonexPattern.compile(phonex);
	}
	
	@Test(expected=PhonexPatternException.class)
	public void testDuplicateGroupName2() throws ParseException {
		final String phonex = "((C1=\\c)(C2=\\c)|(C2=\\c))\\v";
		PhonexPattern.compile(phonex);
	}
	
	@Test
	public void testNonCapturingGroup1() throws ParseException {
		final String text = "ˈkʀət͡jə";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "(\\S\\c+\\v)(?=\\S\\c+\\v)";
		IPATranscript[][] answers = {
				{ ipa.subsection(0, 4) }
		};
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testNonCapturingGroup2() throws ParseException {
		final String text = "ˈkʀət͡jə";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "(?=(\\S\\c+\\v)(\\S\\c+\\v))";
		IPATranscript[][] answers = {
				{} 
		};
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testNonCapturingGroup3() throws ParseException {
		final String text = "ˈkʀət͡jə";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "((?=\\S\\c+\\v)(\\c+\\v))";
		IPATranscript[][] answers = {
				{ ipa, ipa.subsection(4, 6) }
		};
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testLookBehind1() throws ParseException {
		final String text = "zuˈkini";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "(?<\\s\\c\\v)(\\c\\v)";
		IPATranscript[][] answers = {
				{ ipa.subsection(5, 7) }
		};
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testLookBehind2() throws ParseException {
		final String text = "zuˈkini";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "(?<(\\s?\\c\\v)+)(\\c\\v)";
		IPATranscript[][] answers = {
				{ ipa.subsection(5, 7) }
		};
		testGroups(ipa, phonex, answers);
	}
	
	@Test
	public void testReluctantQuantifier() throws ParseException {
		final String text = "ˈh:Oæ:N^p:Oiː:Nb:Oʌ:Nɹ:Cθ:Cˌd:Oe͜ɪ:N";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "(?<\\p)(.+?)(?>\\S)";
		IPATranscript[][] answers = {
				{ ipa.subsection(4, 6) }
		};
		testGroups(ipa, phonex, answers);
	}
	
	@Test(expected=PhonexPatternException.class)
	public void testUnbalancedParenthesis1() throws ParseException {
		final String phonex = "((C1=\\c)(C2=\\c)))\\v";
		PhonexPattern.compile(phonex);
	}
	
	@Test(expected=PhonexPatternException.class)
	public void testUnbalancedParenthesis2() throws ParseException {
		final String phonex = "((C1=\\c)(C2=\\c)\\v";
		PhonexPattern.compile(phonex);
	}
	
	@Test(expected=PhonexPatternException.class)
	public void testUnbalancedParenthesis3() throws ParseException {
		final String phonex = "((C1=\\c(C2=\\c))\\v";
		PhonexPattern.compile(phonex);
	}
	
	@Test(expected=PhonexPatternException.class)
	public void testInvalidBackReference1() throws ParseException {
		final String phonex = "(\\c)\\2";
		PhonexPattern.compile(phonex);
	}
	
	@Test(expected=PhonexPatternException.class)
	public void testInvalidBackReference2() throws ParseException {
		final String phonex = "(\\c)\\-2";
		PhonexPattern.compile(phonex);
	}
	
	@Test
	public void testBackReference() throws ParseException {
		final String text = "hello";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "((\\c)\\2)";
		final IPATranscript[][] answers = new IPATranscript[][] {
			{ ipa.subsection(2, 4), ipa.subsection(2, 3) }
		};
		testGroups(ipa, phonex, answers);
	}

	@Test
	public void testRelativeReference() throws ParseException {
		final String text = "hello";
		final IPATranscript ipa = IPATranscript.parseIPATranscript(text);
		
		final String phonex = "((\\c)\\-1)";
		final IPATranscript[][] answers = new IPATranscript[][] {
			{ ipa.subsection(2, 4), ipa.subsection(2, 3) }
		};
		testGroups(ipa, phonex, answers);
	}
	
}
