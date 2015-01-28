package ca.phon.syllabifier.basic;

import java.io.File;
import java.io.FileFilter;
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

import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.basic.io.ObjectFactory;
import ca.phon.syllabifier.basic.io.SyllabifierDef;
import ca.phon.util.resources.FolderHandler;

public class BasicSyllabifierFolderHandler extends FolderHandler<Syllabifier> {
	
	private final Logger LOGGER = Logger.getLogger(BasicSyllabifierFolderHandler.class.getName());

	public BasicSyllabifierFolderHandler(File folder) {
		super(folder);
		setFileFilter(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".xml");
			}
		});
	}

	@Override
	public Syllabifier loadFromFile(File f) throws IOException {
		final InputStream is = new FileInputStream(f);
		
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
