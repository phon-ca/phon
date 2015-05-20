/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.orthography;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Orthography parser tests
 */ 
@RunWith(JUnit4.class)
public class TestOrthographyParser {
	
	@Test
	public void testWordPrefixCodes() throws ParseException {
		final String wordData = "word";
		
		for(WordPrefix wp:WordPrefix.values()) {
			final Orthography ortho = Orthography.parseOrthography(wp.getCode() + wordData);
			Assert.assertEquals(1, ortho.length());
			
			final OrthoElement wordEle = ortho.elementAt(0);
			Assert.assertEquals(OrthoWord.class, wordEle.getClass());
			
			final OrthoWord word = (OrthoWord)wordEle;
			Assert.assertEquals(wp, word.getPrefix());
			Assert.assertEquals(wordData, word.getWord());
		}
	}
	
	@Test
	public void testWordSuffixCodes() throws ParseException {
		final String wordData = "word";
		
		for(WordSuffix ws:WordSuffix.values()) {
			final Orthography ortho = Orthography.parseOrthography(wordData + "@" + ws.getCode());
			Assert.assertEquals(1, ortho.length());
			
			final OrthoElement wordEle = ortho.elementAt(0);
			Assert.assertEquals(OrthoWord.class, wordEle.getClass());
			
			final OrthoWord word = (OrthoWord)wordEle;
			Assert.assertEquals(ws, word.getSuffix());
			Assert.assertEquals(wordData, word.getWord());
		}
	}
	
	@Test
	public void testWordComboCodes() throws ParseException {
		final String wordData = "word";
		
		for(WordPrefix wp:WordPrefix.values()) {
			for(WordSuffix ws:WordSuffix.values()) {
				final String txt = wp.getCode() + wordData + "@" + ws.getCode();
				
				final Orthography ortho = Orthography.parseOrthography(txt);
				Assert.assertEquals(1, ortho.length());
				
				final OrthoElement wordEle = ortho.elementAt(0);
				Assert.assertEquals(OrthoWord.class, wordEle.getClass());
				
				final OrthoWord word = (OrthoWord)wordEle;
				Assert.assertEquals(wp, word.getPrefix());
				Assert.assertEquals(ws, word.getSuffix());
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
