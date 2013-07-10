package ca.phon.syllabifier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SyllabifierLibraryTest {

	@Test
	public void testSyllabifierList() {
		final SyllabifierLibrary library = SyllabifierLibrary.getInstance();
		
		for(String name:library.availableSyllabifierNames()) {
			System.out.println(name);
		}
	}
	
}
