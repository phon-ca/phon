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

import java.io.*;

import jakarta.xml.bind.*;
import javax.xml.transform.stream.*;

import ca.phon.syllabifier.*;
import ca.phon.syllabifier.basic.io.*;
import ca.phon.util.resources.*;

public class BasicSyllabifierFolderHandler extends FolderHandler<Syllabifier> {
	
	private final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(BasicSyllabifierFolderHandler.class.getName());

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
			LOGGER.error( e.getMessage(), e);
		}
		
		return null;
	}

	
	
}
