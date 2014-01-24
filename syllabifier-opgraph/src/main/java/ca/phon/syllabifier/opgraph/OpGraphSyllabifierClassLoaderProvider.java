package ca.phon.syllabifier.opgraph;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierProvider;
import ca.phon.util.resources.ClassLoaderHandler;

/**
 * Load opgraph syllabifiers listed in the syllabifier/opgraph.list file.
 * 
 */
public class OpGraphSyllabifierClassLoaderProvider extends ClassLoaderHandler<Syllabifier> implements SyllabifierProvider {
	
	private final static String LIST = "syllabifier/opgraph.list";
	
	/**
	 * Constructor
	 */
	public OpGraphSyllabifierClassLoaderProvider() {
		super();
		super.loadResourceFile(LIST);
	}

	@Override
	public Syllabifier loadFromURL(URL url) throws IOException {
		final InputStream is = url.openStream();
		final OpGraphSyllabifier syllabifier = OpGraphSyllabifier.createSyllabifier(is);
		return syllabifier;
	}

}
