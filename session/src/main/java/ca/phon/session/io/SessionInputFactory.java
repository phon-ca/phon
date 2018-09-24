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
package ca.phon.session.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used to create instances of session readers.
 *
 */
public class SessionInputFactory {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(SessionInputFactory.class.getName());
	
	/**
	 * Service loader
	 */
	private final ServiceLoader<SessionReader> readerLoader;
	
	/**
	 * Constructor
	 */
	public SessionInputFactory() {
		super();
		readerLoader = ServiceLoader.load(SessionReader.class);
	}
	
	public SessionInputFactory(ClassLoader cl) {
		super();
		readerLoader = ServiceLoader.load(SessionReader.class, cl);
	}
	
	/**
	 * Get specified reader.
	 * 
	 * @param sessionIO
	 * @return session reader or <code>null</code> if not found
	 */
	public SessionReader createReader(SessionIO sessionIO) {
		return createReader(sessionIO.id(), sessionIO.version());
	}
	
	
	/**
	 * Get the list of available session readers.
	 * 
	 * @return list of readers
	 */
	public List<SessionIO> availableReaders() {
		final List<SessionIO> retVal = new ArrayList<SessionIO>();
		
		final Iterator<SessionReader> readerItr = readerLoader.iterator();
		while(readerItr.hasNext()) {
			// look for the SessionIO annotation
			final SessionReader reader = readerItr.next();
			final SessionIO sessionIO = reader.getClass().getAnnotation(SessionIO.class);
			
			if(sessionIO != null)
				retVal.add(sessionIO);
		}		
		
		return retVal;
	}
	
	/**
	 * Create a new session reader given the SessionIO version.
	 * 
	 * @param id
	 * @param version
	 * @return the new SessionReader or <code>null</code> if not found
	 */
	public SessionReader createReader(String id, String version) {
		SessionReader retVal = null;
		
		final Iterator<SessionReader> readerItr = readerLoader.iterator();
		while(readerItr.hasNext()) {
			// look for the SessionIO annotation
			final SessionReader reader = readerItr.next();
			final SessionIO sessionIO = reader.getClass().getAnnotation(SessionIO.class);
			if(sessionIO != null && sessionIO.version().equals(version) && sessionIO.id().equals(id)) {
				retVal = reader;
				break;
			}
		}
		
		return retVal;
	}
	
	/**
	 * Create a new session reader for the given file.
	 * 
	 * @param file
	 * @return session reader or <code>null</code> if not found
	 * 
	 * @throws IOException
	 */
	public SessionReader createReaderForFile(File file) {
		SessionReader retVal = null;
		
		final Iterator<SessionReader> readerItr = readerLoader.iterator();
		while(readerItr.hasNext()) {
			final SessionReader reader = readerItr.next();
			final SessionIO sessionIO = reader.getClass().getAnnotation(SessionIO.class);
			if(sessionIO != null && file.getName().endsWith(sessionIO.extension())) {
				try {
					if(reader.canRead(file)) {
						retVal = reader;
						break;
					}
				} catch (IOException e) {
					LOGGER.error( e.getLocalizedMessage(), e);
				}
			}
		}
		
		return retVal;
	}
	
}
