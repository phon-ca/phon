package ca.phon.ipadictionary;

import java.util.Iterator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.util.resources.ResourceLoader;

@RunWith(JUnit4.class)
public class IPADictionaryManagerTest {

	@Test
	public void testDictionaryList() {
		final IPADictionaryLibrary library = IPADictionaryLibrary.getInstance();
		
		final ResourceLoader<IPADictionary> dictLoader = library.getLoader();
		final Iterator<IPADictionary> dictItr = dictLoader.iterator();
		while(dictItr.hasNext()) {
			final IPADictionary dict = dictItr.next();
			System.out.println(dict.getLanguage());
		}
	}
	
}
