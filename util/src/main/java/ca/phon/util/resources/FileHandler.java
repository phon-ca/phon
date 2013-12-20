package ca.phon.util.resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
