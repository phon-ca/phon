package ca.phon.syllabifier;

import ca.phon.syllabifier.opgraph.OpGraphSyllabifierClassLoaderProvider;
import junit.framework.TestCase;

public class TestSyllabifierLibrary extends TestCase {
	
	public void testOpGraphHandler() {
		OpGraphSyllabifierClassLoaderProvider cpProvider = new OpGraphSyllabifierClassLoaderProvider();
		for(Syllabifier syllabifier:cpProvider) {
			System.out.println(syllabifier.getName() + " " + syllabifier.getLanguage());
		}
	}

}
