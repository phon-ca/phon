package ca.phon.orthography;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Orthography parser tests
 */ 
@RunWith(JUnit4.class)
public class TestOrthographyParser {

	/**
	 * Test simple words
	 */
	@Test
	public void testSimpleWords() {
		// word list
		final String[] words = {
			"hello", "0omission", "ka@fs", "yyy"
		};
		
		for(String word:words) {
			final Orthography orthography = new Orthography(word);
			Assert.assertEquals(1, orthography.size());
		}
	}
	
	@Test
	public void testCompoundWords() {
		// word list
		final String[] words = {
			"one+two", "three~four"
		};
		
		for(String word:words) {
			final Orthography orthography = new Orthography(word);
			Assert.assertEquals(1, orthography.size());
		}
	}
	
	@Test
	public void testPhrase() {
		
	}
	
}
