package ca.phon.orthography;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Orthography parser tests
 */ 
@RunWith(JUnit4.class)
public class TestOrthographyParser {
	
	@Test
	public void testWordPrefixCodes() {
		final String wordData = "word";
		
		for(WordPrefix wp:WordPrefix.values()) {
			final Orthography ortho = new Orthography(wp.getCode() + wordData);
			Assert.assertEquals(1, ortho.size());
			
			final OrthoElement wordEle = ortho.get(0);
			Assert.assertEquals(OrthoWord.class, wordEle.getClass());
			
			final OrthoWord word = (OrthoWord)wordEle;
			Assert.assertEquals(wp, word.getPrefix());
			Assert.assertEquals(wordData, word.getWord());
		}
	}
	
	@Test
	public void testWordSuffixCodes() {
		final String wordData = "word";
		
		for(WordSuffix ws:WordSuffix.values()) {
			final Orthography ortho = new Orthography(wordData + "@" + ws.getCode());
			Assert.assertEquals(1, ortho.size());
			
			final OrthoElement wordEle = ortho.get(0);
			Assert.assertEquals(OrthoWord.class, wordEle.getClass());
			
			final OrthoWord word = (OrthoWord)wordEle;
			Assert.assertEquals(ws, word.getSuffix());
			Assert.assertEquals(wordData, word.getWord());
		}
	}
	
	@Test
	public void testWordComboCodes() {
		final String wordData = "word";
		
		for(WordPrefix wp:WordPrefix.values()) {
			for(WordSuffix ws:WordSuffix.values()) {
				final String txt = wp.getCode() + wordData + "@" + ws.getCode();
				
				final Orthography ortho = new Orthography(txt);
				Assert.assertEquals(1, ortho.size());
				
				final OrthoElement wordEle = ortho.get(0);
				Assert.assertEquals(OrthoWord.class, wordEle.getClass());
				
				final OrthoWord word = (OrthoWord)wordEle;
				Assert.assertEquals(wp, word.getPrefix());
				Assert.assertEquals(ws, word.getSuffix());
				Assert.assertEquals(wordData, word.getWord());
			}
		}
	}
	
	@Test
	public void testComment() {
		final String comment = "this is a test";
		final String txt = "word (" + comment + ") .";
		
		final Orthography ortho = new Orthography(txt);
		Assert.assertEquals(3, ortho.size());
		
		final OrthoElement commentEle = ortho.get(1);
		Assert.assertEquals(OrthoComment.class, commentEle.getClass());
		
		final OrthoComment c = (OrthoComment)commentEle;
		Assert.assertEquals(comment, c.getData());
	}
	
	@Test
	public void testCommentWithType() {
		final String comment = "this is a test";
		final String type = "test";
		final String txt = "word (" + type + ":" + comment + ") .";
		
		final Orthography ortho = new Orthography(txt);
		Assert.assertEquals(3, ortho.size());
		
		final OrthoElement commentEle = ortho.get(1);
		Assert.assertEquals(OrthoComment.class, commentEle.getClass());
		
		final OrthoComment c = (OrthoComment)commentEle;
		Assert.assertEquals(comment, c.getData());
	}
	
	@Test
	public void testEvent() {
		final String event = "this is a test";
		final String txt = "word *" + event + "* .";
		
		final Orthography ortho = new Orthography(txt);
		Assert.assertEquals(3, ortho.size());
		
		final OrthoElement eventEle = ortho.get(1);
		Assert.assertEquals(OrthoEvent.class, eventEle.getClass());
		
		final OrthoEvent e = (OrthoEvent)eventEle;
		Assert.assertEquals(event, e.getData());
	}
	
	@Test
	public void testEventWithType() {
		final String event = "this is a test";
		final String type = "test";
		final String txt = "word *" + type + ":" + event + "* .";
		
		final Orthography ortho = new Orthography(txt);
		Assert.assertEquals(3, ortho.size());
		
		final OrthoElement eventEle = ortho.get(1);
		Assert.assertEquals(OrthoEvent.class, eventEle.getClass());
		
		final OrthoEvent e = (OrthoEvent)eventEle;
		Assert.assertEquals(event, e.getData());
	}
	
	@Test
	public void testPunct() {
		final String word = "word";
		
		for(OrthoPunctType opt:OrthoPunctType.values()) {
			final String testString = word + " " + opt.getChar();
			
			final Orthography ortho = new Orthography(testString);
			Assert.assertEquals(2, ortho.size());
			
			final OrthoElement punctEle = ortho.get(1);
			Assert.assertEquals(OrthoPunct.class, punctEle.getClass());
			
			final OrthoPunct punct = (OrthoPunct)punctEle;
			Assert.assertEquals(opt.getChar()+"", punct.text());
		}
	}
	
}
