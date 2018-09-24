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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.logging.log4j.LogManager;

/**
 * Load resources from URLs.
 * 
 */
public abstract class URLHandler<T> implements ResourceHandler<T> {
	
	/* static logger for class */
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(URLHandler.class.getName());
	
	/**
	 * URLs to load
	 */
	private final List<URL> urls = 
			Collections.synchronizedList(new ArrayList<URL>());

	/**
	 * Constructor
	 */
	public URLHandler() {
		
	}
	
	/**
	 * Constructor
	 * 
	 * @param urls initial list of URLs to load
	 */
	public URLHandler(List<URL> urls) {
		this.urls.addAll(urls);
	}
	
	/**
	 * Add the given url to the handler.
	 *
	 * @param url
	 */
	public void add(URL url) {
		this.urls.add(url);
	}
	
	/**
	 * Remove the given url from the loader
	 * 
	 * @parm url
	 */
	public void remove(URL url) {
		this.urls.remove(url);
	}
	
	/**
	 * Live list to the {@link URL}s loaded
	 * by this handler.
	 * 
	 * @return the list of urls
	 */
	public List<URL> getURLS() {
		return this.urls;
	}
	
	/**
	 * Instantiate the object from the given url.
	 *
	 * @param url
	 * @throws IOException
	 */
	public abstract T loadFromURL(URL url) 
			throws IOException;

	@Override
	public Iterator<T> iterator() {
		return new URLIterator(urls.iterator());
	}

	private class URLIterator implements Iterator<T> {
		
		/**
		 * URL iterator
		 */
		private Iterator<URL> urlIterator;
		
		public URLIterator(Iterator<URL> itr) {
			this.urlIterator = itr;
		}

		@Override
		public boolean hasNext() {
			return urlIterator.hasNext();
		}

		@Override
		public T next() {
			T retVal = null;
			final URL url = urlIterator.next();
			if(url != null) {
				try {
					retVal = loadFromURL(url);
				} catch (IOException e) {
					e.printStackTrace();
					LOGGER.error(e.getMessage());
				}
			}
			return retVal;
		}

		@Override
		public void remove() {
		}
		
	}
}
