/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
