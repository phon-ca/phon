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
package ca.phon.xml;

import java.util.Iterator;
import java.util.ServiceLoader;

import javax.xml.namespace.QName;

import ca.phon.xml.annotation.XMLSerial;

/**
 * Creates {@link XMLObjectReader} for given {@link QName}
 * references.
 * 
 */
public class XMLObjectReaderFactory {
	
	/**
	 * Class loader to use for loading readers.
	 * 
	 * 
	 */
	private final ClassLoader cl;
	
	public XMLObjectReaderFactory() {
		this(null);
	}
	
	public XMLObjectReaderFactory(ClassLoader cl) {
		super();
		this.cl = cl;
	}

	/**
	 * Return the XMLObjectReader for the given namespace
	 * and element name.
	 * 
	 * @param namespace
	 * @param name
	 * 
	 * @return an {@link XMLObjectReader} or <code>null</code> if
	 *  an appropriate reader could not be found.
	 */
	public <T> XMLObjectReader<T> createReader(String namespace, String name, Class<T> type) {
		return createReader(new QName(namespace, name), type);
	}
	
	/**
	 * Return the XMLObjectReader for the given {@link QName}
	 * 
	 * @param qname
	 * 
	 * @return an {@link XMLObjectReader} or <code>null</code> if
	 *  an appropriate reader could not be found.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> XMLObjectReader<T> createReader(QName qname, Class<T> type) {
		final ServiceLoader<XMLObjectReader> loader = 
				(this.cl != null ? ServiceLoader.load(XMLObjectReader.class, this.cl) : ServiceLoader.load(XMLObjectReader.class));		
		final Iterator<XMLObjectReader> itr = loader.iterator();
		
		while(itr.hasNext()) {
			final XMLObjectReader<?> reader = itr.next();
			final XMLSerial annotation = reader.getClass().getAnnotation(XMLSerial.class);
			if(annotation != null) {
				final QName tName = new QName(annotation.namespace(), annotation.elementName());
				// TODO should really check the parameterized type here instead of
				// the annotation value
				if(tName.equals(qname) && type == annotation.bindType()) {
					return (XMLObjectReader<T>)reader;
				}
			}
		}
		
		return null;
	}
	
}
