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

import java.util.*;

/**
 * Load resources of the parameterized type
 * from various types of media.  The actual
 * loading is done using 
 * 
 * @param <T>
 */
public final class ResourceLoader<T> implements Iterable<T> {
	
	/**
	 * resource handlers
	 */
	private final List<ResourceHandler<T>> handlers = new ArrayList<ResourceHandler<T>>();
	
	/**
	 * Constructor
	 */
	public ResourceLoader() {
	}
	
	/**
	 * Return an iterator of loaded resources.
	 * Resources will be loaded from handlers in
	 * the order the handlers are added to the library.
	 * 
	 * @return {@link Iterator}
	 */
	@Override
	public Iterator<T> iterator() {
		return new LazyIterator();
	}
	
	/**
	 * Add the given handler to the resource loader.
	 * 
	 * @param handler
	 *
	 */
	public void addHandler(ResourceHandler<T> handler) {
		if(handler != null && !handlers.contains(handler)) 
			handlers.add(handler);
	}
	

	private class LazyIterator implements Iterator<T> {
		
		private List<Iterator<T>> iterators;
		
		private int currentHandler = 0;
		
		public LazyIterator() {
			iterators = new ArrayList<Iterator<T>>();
			for(ResourceHandler<T> handler:handlers) {
				final Iterator<T> itr = handler.iterator();
				if(itr.hasNext())
					iterators.add(handler.iterator());
			}
		}

		@Override
		public boolean hasNext() {
			boolean retVal = false;
			if(currentIterator() != null) {
				retVal = currentIterator().hasNext();
			}
			return retVal;
		}

		@Override
		public T next() {
			T retVal = null;
			
			if(currentIterator() != null) {
				retVal = currentIterator().next();
				
				if(!currentIterator().hasNext()) {
					currentHandler++;
				}
			}
			
			return retVal;
		}
		
		private Iterator<T> currentIterator() {
			Iterator<T> currentIterator = null;
			if(currentHandler < iterators.size()) {
				currentIterator = iterators.get(currentHandler);
			}
			return currentIterator;
		}

		@Override
		public void remove() {
			// not implemented
		}
		
	}
}
