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
package ca.phon.session.io;

import java.io.IOException;
import java.io.OutputStream;

import ca.phon.session.Session;

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
