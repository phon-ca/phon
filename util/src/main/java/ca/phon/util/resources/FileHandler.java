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
package ca.phon.util.resources;

import java.io.*;
import java.util.*;

/**
 * A library handler which allow for loading a list of explicit
 * files.
 * 
 */
public abstract class FileHandler<T> implements ResourceHandler<T> {
	
	private final static org.apache.logging.log4j.Logger LOGGER = 
			org.apache.logging.log4j.LogManager.getLogger(FileHandler.class.getName());
	
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
				LOGGER.error(e.getMessage());
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
