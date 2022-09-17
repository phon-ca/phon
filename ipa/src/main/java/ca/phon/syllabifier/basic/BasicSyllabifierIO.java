/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.syllabifier.basic;

import ca.phon.syllabifier.basic.io.*;
import jakarta.xml.bind.*;

import javax.xml.transform.stream.StreamSource;
import java.io.*;

/**
 * Utility methods for reading/writing {@link BasicSyllabifier}s.
 */
public class BasicSyllabifierIO {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(BasicSyllabifierIO.class.getName());

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
			LOGGER.error( e.getMessage(), e);
			throw new IOException(e);
		}
	}
	
}
