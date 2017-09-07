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

import java.io.*;

import ca.phon.session.Session;

/**
 * Interface for a session reader.  The reader is responsible
 * for reading an {@link InputStream} and returning the resulting
 * session.  Instances of this class should be obtained using
 * {@link SessionInputFactory}.
 *
 */
public interface SessionReader {
	
	/**
	 * Check to see if this reader can open
	 * the given file.
	 * 
	 * @param file
	 * 
	 * @return <code>true</code> if this reader can open
	 *  the given file, <code>false</code> otherwise
	 * 
	 * @throws IOException if an error occurs when attempting
	 *  to open a file
	 */
	public boolean canRead(File file) throws IOException;
	
	
	/**
	 * Create session from given input stream
	 * 
	 * @param stream
	 * @return session
	 * 
	 * @throws IOException if an error occurs
	 */
	public Session readSession(InputStream stream) throws IOException;
	
}
