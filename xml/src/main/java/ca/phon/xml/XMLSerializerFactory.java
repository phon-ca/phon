/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

/**
 * Obtain instances of {@link XMLSerializer} objects.
 * 
 */
public class XMLSerializerFactory {
	
	public XMLSerializerFactory() {
		
	}
	
	/**
	 * Obtain an XMLSerializer instance for the given class.
	 * 
	 * @param type
	 * @return serializer for the given type or <code>null</code> of
	 *  no serializer is found
	 */
	public XMLSerializer newSerializerForType(Class<?> type) {
		final ServiceLoader<XMLSerializer> loader = ServiceLoader.load(XMLSerializer.class);
		final Iterator<XMLSerializer> itr = loader.iterator();
		
		XMLSerializer retVal = null;
		while(itr.hasNext() && retVal == null) {
			final XMLSerializer currentSerializer = itr.next();
			if(currentSerializer.declaredType() == type) {
				retVal = currentSerializer;
			}
		}
		
		return retVal;
	}
	

}
