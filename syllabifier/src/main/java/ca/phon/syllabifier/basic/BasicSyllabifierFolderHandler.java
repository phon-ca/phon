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

import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.basic.io.*;
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
