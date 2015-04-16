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
package ca.phon.session.io;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Factory for creating {@link SessionWriter}s
 *
 */
public class SessionOutputFactory {

	/**
	 * Service loader
	 */
	private final ServiceLoader<SessionWriter> writerLoader;
	
	/**
	 * Constructor
	 */
	public SessionOutputFactory() {
		super();
		writerLoader = ServiceLoader.load(SessionWriter.class);
	}
	
	public SessionOutputFactory(ClassLoader cl) {
		super();
		writerLoader = ServiceLoader.load(SessionWriter.class, cl);
	}
	
	/**
	 * Create a new session writer
	 * 
	 * @return a new {@link SessionWriter} or <code>null</code>
	 *  if a compatible writer could not be found
	 */
	public SessionWriter createWriter() {
		SessionWriter retVal = null;
		
		final Iterator<SessionWriter> writerItr = writerLoader.iterator();
		if(writerItr.hasNext()) {
			retVal = writerItr.next();
		}
		
		return retVal;
	}
	
	/**
	 * Create a new session writer for the given version name.
	 * 
	 * @param version
	 * @return a new {@link SessionWriter} or <code>null</code>
	 *  if a writer for the version is not found
	 */
	public SessionWriter createWriter(String version) {
		SessionWriter retVal = null;
		
		final Iterator<SessionWriter> writerItr = writerLoader.iterator();
		while(writerItr.hasNext()) {
			final SessionWriter writer = writerItr.next();
			final SessionIO sessionIO = writer.getClass().getAnnotation(SessionIO.class);
			if(sessionIO != null && sessionIO.version().equals(version)) {
				retVal = writer;
				break;
			}
		}
		
		return retVal;
	}
}
