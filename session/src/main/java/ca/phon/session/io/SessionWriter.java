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
 * Interface for writing {@link Session} objects
 * to a given {@link OutputStream}
 *
 */
public interface SessionWriter {

	/**
	 * Write the given {@link Session} to the provided
	 * {@link OutputStream}
	 * 
	 * @param session
	 * @param out
	 * 
	 * @throws IOException
	 */
	public void writeSession(Session session, OutputStream out)
		throws IOException;
	
}
