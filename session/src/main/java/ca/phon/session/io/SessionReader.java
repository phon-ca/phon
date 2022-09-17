/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.session.io;

import ca.phon.session.Session;

import java.io.*;

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
