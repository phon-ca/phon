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
	
	public List<ResourceHandler<T>> getHandlers() {
		return this.handlers;
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
