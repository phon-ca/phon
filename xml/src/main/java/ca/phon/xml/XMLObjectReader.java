/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.xml;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A class that reads objects from a given XML stream.
 * 
 */
public interface XMLObjectReader<T> {

	/**
	 * Read from the given xml input stream.
	 * 
	 * @param eventReader
	 * @param type
	 * 
	 * @return object of given type read from the
	 *  given eventReader
	 *  
	 * @throws IOException if something goes wrong
	 */
	public T read(Document doc, Element ele)
		throws IOException;
	
}
