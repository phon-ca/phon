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
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.logging.Logger;
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
	private final static Logger LOGGER = Logger.getLogger(ClasspathXMLEntityResolver.class.getName());
	
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
