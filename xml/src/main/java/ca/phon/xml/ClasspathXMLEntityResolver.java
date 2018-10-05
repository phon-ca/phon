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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;

/**
 * <p>XML entity resolver that will look for catalog files
 * using the provided {@link ClassLoader}.  The catalog
 * files must be in the location "xml/catalog.cat".
 * </p>
 */
public class ClasspathXMLEntityResolver implements XMLResolver {
	
	/** Logger */
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ClasspathXMLEntityResolver.class.getName());
	
	private ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	
	private final String CATALOG_FILE = "xml/catalog.cat";
	
	/**
	 * Create a new xml resolver
	 * 
	 */
	public ClasspathXMLEntityResolver() {
		super();
	}
	
	public ClasspathXMLEntityResolver(ClassLoader loader) {
		this.classLoader = loader;
	}
	
	/**
	 * Returns the entity URL for the given systemID.
	 * 
	 * @param systemID
	 * 
	 * @return location of entity, or <code>null</code> if not
	 *  found
	 */
	protected URL locateEntity(String systemID)
		throws IOException {
		final Enumeration<URL> catalogURLs = classLoader.getResources(CATALOG_FILE);
		while(catalogURLs.hasMoreElements()) {
			final URL catalogURL = catalogURLs.nextElement();
			final InputStream catalogStream = catalogURL.openStream();
			
			// parse system IDs from file
			final Scanner scanner = new Scanner(catalogStream);
			final Pattern scannerPattern = Pattern.compile("SYSTEM\\p{Space}\"(.*)\"\\p{Space}\"(.*)\"");
			while(scanner.hasNext(scannerPattern)) {
				final String scannerLine = scanner.next();
				final Matcher matcher = scannerPattern.matcher(scannerLine);
				
				if(matcher.matches()) {
					final String id = matcher.group(1);
					final String location = matcher.group(2);
					
					if(id.equals(systemID)) {
						// return the resource if found
						return classLoader.getResource(location); 
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public Object resolveEntity(String publicID, String systemID,
			String baseURI, String namespace) throws XMLStreamException {
		try {
			final URL entityURL = locateEntity(systemID);
			if(entityURL != null) {
				return entityURL.openStream();
			}
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
		return null;
	}

}
