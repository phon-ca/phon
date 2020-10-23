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
package ca.phon.orthography;

import java.text.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

/**
 * Orthography parser tests
 */ 
@RunWith(JUnit4.class)
public class TestOrthographyParser {
	
	@Test
	public void testWordPrefixCodes() throws ParseException {
		final String wordData = "word";
		
		for(WordPrefixType wp:WordPrefixType.values()) {
			final Orthography ortho = Orthography.parseOrthography(wp.getCode() + wordData);
			Assert.assertEquals(1, ortho.length());
			
			final OrthoElement wordEle = ortho.elementAt(0);
			Assert.assertEquals(OrthoWord.class, wordEle.getClass());
			
			final OrthoWord word = (OrthoWord)wordEle;
			Assert.assertNotNull(word.getPrefix());
			Assert.assertEquals(wp, word.getPrefix().getType());
			Assert.assertEquals(wordData, word.getWord());
		}
	}
	
	@Test
	public void testWordSuffixCodes() throws ParseException {
		final String wordData = "word";
		
		for(WordSuffixType ws:WordSuffixType.values()) {
			final Orthography ortho = Orthography.parseOrthography(wordData + ws.getCode());
			Assert.assertEquals(1, ortho.length());
			
			final OrthoElement wordEle = ortho.elementAt(0);
			Assert.assertEquals(OrthoWord.class, wordEle.getClass());
			
			final OrthoWord word = (OrthoWord)wordEle;
			Assert.assertNotNull(word.getSuffix());
			Assert.assertEquals(ws, word.getSuffix().getType());
			Assert.assertEquals(wordData, word.getWord());
		}
	}
	
	@Test
	public void testWordComboCodes() throws ParseException {
		final String wordData = "word";
		
		for(WordPrefixType wp:WordPrefixType.values()) {
			for(WordSuffixType ws:WordSuffixType.values()) {
				final String txt = wp.getCode() + wordData + ws.getCode();
				
				final Orthography ortho = Orthography.parseOrthography(txt);
				Assert.assertEquals(1, ortho.length());
				
				final OrthoElement wordEle = ortho.elementAt(0);
				Assert.assertEquals(OrthoWord.class, wordEle.getClass());
				
				final OrthoWord word = (OrthoWord)wordEle;
				Assert.assertEquals(wp, word.getPrefix().getType());
				Assert.assertEquals(ws, word.getSuffix().getType());
				Assert.assertEquals(wordData, word.getWord());
			}
		}
	}
	
	@Test
	public void testComment() throws ParseException {
		final String comment = "this is a test";
		final String txt = "word (" + comment + ") .";
		
		final Orthography ortho = Orthography.parseOrthography(txt);
		Assert.assertEquals(3, ortho.length());
		
		final OrthoElement commentEle = ortho.elementAt(1);
		Assert.assertEquals(OrthoComment.class, commentEle.getClass());
		
		final OrthoComment c = (OrthoComment)commentEle;
		Assert.assertEquals(comment, c.getData());
	}
	
	@Test
	public void testCommentWithType() throws ParseException {
		final String comment = "this is a test";
		final String type = "test";
		final String txt = "word (" + type + ":" + comment + ") .";
		
		final Orthography ortho = Orthography.parseOrthography(txt);
		Assert.assertEquals(3, ortho.length());
		
		final OrthoElement commentEle = ortho.elementAt(1);
		Assert.assertEquals(OrthoComment.class, commentEle.getClass());
		
		final OrthoComment c = (OrthoComment)commentEle;
		Assert.assertEquals(comment, c.getData());
	}
	
	@Test
	public void testEvent() throws ParseException {
		final String event = "this is a test";
		final String txt = "word *" + event + "* .";
		
		final Orthography ortho = Orthography.parseOrthography(txt);
		Assert.assertEquals(3, ortho.length());
		
		final OrthoElement eventEle = ortho.elementAt(1);
		Assert.assertEquals(OrthoEvent.class, eventEle.getClass());
		
		final OrthoEvent e = (OrthoEvent)eventEle;
		Assert.assertEquals(event, e.getData());
	}
	
	@Test
	public void testEventWithType() throws ParseException {
		final String event = "this is a test";
		final String type = "test";
		final String txt = "word *" + type + ":" + event + "* .";
		
		final Orthography ortho = Orthography.parseOrthography(txt);
		Assert.assertEquals(3, ortho.length());
		
		final OrthoElement eventEle = ortho.elementAt(1);
		Assert.assertEquals(OrthoEvent.class, eventEle.getClass());
		
		final OrthoEvent e = (OrthoEvent)eventEle;
		Assert.assertEquals(event, e.getData());
	}
	
	@Test
	public void testPunct() throws ParseException {
		final String word = "word";
		
		for(OrthoPunctType opt:OrthoPunctType.values()) {
			final String testString = word + " " + opt.getChar();
			
			final Orthography ortho = Orthography.parseOrthography(testString);
			Assert.assertEquals(2, ortho.length());
			
			final OrthoElement punctEle = ortho.elementAt(1);
			Assert.assertEquals(OrthoPunct.class, punctEle.getClass());
			
			final OrthoPunct punct = (OrthoPunct)punctEle;
			Assert.assertEquals(opt.getChar()+"", punct.text());
		}
	}
	
	@Test
	public void testWordnets() throws ParseException {
		final String[] wordnets = new String[]{ "one+two", "one+two+three", "a~b~c", "yo~ma+ma" };
		for(String wd:wordnets) {
			final Orthography ortho = Orthography.parseOrthography(wd);
			
			Assert.assertEquals(1, ortho.length());
			Assert.assertEquals(wd, ortho.toString());
			Assert.assertEquals(OrthoWordnet.class, ortho.elementAt(0).getClass());
		}
	}
	
}
