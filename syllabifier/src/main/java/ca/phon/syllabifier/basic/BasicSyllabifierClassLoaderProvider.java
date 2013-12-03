package ca.phon.syllabifier.basic;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierProvider;
import ca.phon.syllabifier.basic.io.ObjectFactory;
import ca.phon.syllabifier.basic.io.SyllabifierDef;
import ca.phon.util.resources.ClassLoaderHandler;

/**
 * Loader for Phon 1.6 syllabifier files.
 */
public class BasicSyllabifierClassLoaderProvider extends ClassLoaderHandler<Syllabifier> implements SyllabifierProvider {
	
	private final static Logger LOGGER = Logger.getLogger(BasicSyllabifierClassLoaderProvider.class.getName());

	private final static String LIST = "syllabifier/basic.list";
	
	public BasicSyllabifierClassLoaderProvider() {
		super();
		super.loadResourceFile(LIST);
	}
	
	@Override
	public Syllabifier loadFromURL(URL url) throws IOException {
		final InputStream is = url.openStream();
		
		
		try {
			final JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			
			final JAXBElement<SyllabifierDef> jaxbEle = 
					unmarshaller.unmarshal(new StreamSource(is), SyllabifierDef.class);
			return new BasicSyllabifier(jaxbEle.getValue());
		} catch (JAXBException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		
		return null;
	}
	
}
