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
package ca.phon.util.resources;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.logging.log4j.*;

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
