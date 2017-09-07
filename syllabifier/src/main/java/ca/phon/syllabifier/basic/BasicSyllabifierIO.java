/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.syllabifier.basic;

import java.io.*;
import java.util.logging.*;

import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;

import ca.phon.syllabifier.basic.io.*;

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
