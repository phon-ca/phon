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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * XML Serializer.
 * 
 * 
 */
public interface XMLSerializer {
	
	/**
	 * Read an object of type {@link #declaredType()} from
	 * the given {@link InputStream}.
	 * 
	 * @param input
	 * @return the read object
	 * 
	 * @throws IOException if an error occurs during
	 *  read
	 */
	public <T> T read(Class<T> type, InputStream input) throws IOException;
	
	/**
	 * Write an object of type {@link #declaredType()} to
	 * the given {@link OutputStream} as xml data.
	 * 
	 * @param output
	 * 
	 * @throws IOException if an error occurs during
	 *  write
	 */
	public <T> void write(Class<T> type, T obj, OutputStream output) throws IOException;
	
	/**
	 * The type handled by this serializer.
	 * 
	 * @return
	 */
	public Class<?> declaredType();

}
