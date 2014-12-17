package ca.phon.orthography;

import java.text.ParseException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestOrthographyParserErrors {

	@Test
	public void testUnfinishedComment() {
		final String text = "hello (world";
		try {
			Orthography.parseOrthography(text);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	
}
