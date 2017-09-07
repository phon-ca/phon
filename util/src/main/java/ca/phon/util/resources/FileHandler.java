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
package ca.phon.util.resources;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * A library handler which allow for loading a list of explicit
 * files.
 * 
 */
public abstract class FileHandler<T> implements ResourceHandler<T> {
	
	private final static Logger LOGGER = 
			Logger.getLogger(FileHandler.class.getName());
	
	/**
	 * List of files to include in the handler
	 */
	private final List<File> files = 
			Collections.synchronizedList(new ArrayList<File>());

	@Override
	public Iterator<T> iterator() {
		return new FileIterator(files.toArray(new File[0]));
	}
	
	/**
	 * Add a file to the handler
	 * 
	 * @param file
	 */
	public void addFile(File f) {
		if(f != null && !files.contains(f)) {
			files.add(f);
		}
	}
	
	/**
	 * Remove a file from the handler
	 * 
	 * @param file
	 */
	public void removeFile(File f) {
		files.remove(f);
	}
	
	/**
	 * Live list of the {@link File}s loaded
	 * by this handler.
	 * 
	 * @return this list of {@link File}s
	 */
	public List<File> getFiles() {
		return this.files;
	}
	
	/**
	 * Abstract method for loading the given file as
	 * an instance of the parameterized type.
	 * 
	 * @param file
	 * @returns T
	 * @throws IOException
	 */
	public abstract T loadFromFile(File f) throws IOException;
	
	/**
	 * Iterator, will lazily create streams for
	 * resources.
	 * 
	 * {@link #next()} <em>may</em> return <code>null</code> if
	 * the file could not be opened.
	 */
	private class FileIterator implements Iterator<T> {
		
		/**
		 * The list of files to iterator over
		 */
		private final File[] fileArray;
		
		/**
		 * Current index
		 */
		private int currentIndex = 0;
		
		public FileIterator(File[] files) {
			this.fileArray = files;
		}

		@Override
		public boolean hasNext() {
			boolean retVal = (currentIndex < fileArray.length);
			return retVal;
		}

		@Override
		public T next() {
			File f = fileArray[currentIndex++];
			T obj = null;
			try {
				obj = loadFromFile(f);
			} catch (IOException e) {
				LOGGER.severe(e.getMessage());
				e.printStackTrace();
			}
			return obj;
		}

		@Override
		public void remove() {
			// not implemented
		}
		
	}

}
