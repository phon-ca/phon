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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;

/**
 * 
 */
public class XMLUtil {
	
	/**
	 * Provide a catalog resolver for all schemas
	 * identified in the $classloader::xml/catalog.cat
	 * resource.
	 * 
	 * @return the catalog resolver
	 * 
	 */
	public static XMLResolver getXMLResolver() {
		return new ClasspathXMLEntityResolver();
	}
	
	public static XMLResolver getXMLResolver(ClassLoader classLoader) {
		return new ClasspathXMLEntityResolver(classLoader);
	}
	
	/**
	 * Create a new xml input factory.
	 */
	public static XMLInputFactory newInputFactory() {
		final XMLInputFactory retVal = XMLInputFactory.newFactory();
		retVal.setXMLResolver(getXMLResolver());
		return retVal;
	}
	
	/**
	 * Create a new xml input factory
	 * 
	 * @param blah
	 * @param blah2
	 * 
	 */
	public static XMLInputFactory newInputFactory(String factoryID, ClassLoader classLoader) {
		final XMLInputFactory retVal = XMLInputFactory.newFactory(factoryID, classLoader);
		retVal.setXMLResolver(getXMLResolver(classLoader));
		return retVal;
	}
}
