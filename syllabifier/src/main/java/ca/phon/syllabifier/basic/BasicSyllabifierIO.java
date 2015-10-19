package ca.phon.syllabifier.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import ca.phon.syllabifier.basic.io.ObjectFactory;
import ca.phon.syllabifier.basic.io.SyllabifierDef;

/**
 * Utility methods for reading/writing {@link BasicSyllabifier}s.
 */
public class BasicSyllabifierIO {
	
	private final static Logger LOGGER = Logger.getLogger(BasicSyllabifierIO.class.getName());

	public BasicSyllabifier readFromFile(File file) 
		throws IOException {
		return readFromStream(new FileInputStream(file));
	}
	
	public BasicSyllabifier readFromStream(InputStream is) 
		throws IOException {
		try {
			final JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			
			final JAXBElement<SyllabifierDef> jaxbEle = 
					unmarshaller.unmarshal(new StreamSource(is), SyllabifierDef.class);
			
			if(jaxbEle.getValue().getName() == null) {
				throw new IOException("Syllabifier not loaded");
			}
			
			return new BasicSyllabifier(jaxbEle.getValue());
		} catch (JAXBException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		}
	}
	
}
