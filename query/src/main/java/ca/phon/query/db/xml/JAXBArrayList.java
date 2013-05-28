/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

package ca.phon.query.db.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A special implementation of {@link ArrayList} that synchronizes objects in
 * two lists. More specifically, whenever an object <code>X</code> is removed from
 * this list, an associated object <code>Y</code> is removed from the other list.
 * 
 * A mapping between objects is necessary, and is defined by the {@link Mapper}
 * interface in this class.
 * 
 * Note that it is assumed that if object <code>X</code> is at index <code>i</code> in
 * this list, then <code>f(X)</code> is also at index <code>i</code> in the wrapped list.
 */
public class JAXBArrayList<T, V> extends ArrayList<T> {
	/** The wrapped list */
	private List<V> list;
	
	/** The mapping from objects of type <code>T</code> to <code>V</code> */
	private Mapper<T, V> mapper;
	
	/**
	 * A mapping from objects of type T to objects of type V.
	 */
	public static interface Mapper<T, V> {
		public abstract V map(T x);
		public abstract T create(V y);
	}
	
	/**
	 * Constructs a wrapper around the specified list.
	 * 
	 * @param list  the list to wrap
	 */
	public JAXBArrayList(List<V> list, Mapper<T, V> mapper) {
		this.list = list;
		this.mapper = mapper;
		for(V x : list)
			super.add(mapper.create(x));
	}
	
	@Override
	public void add(int index, T e) {
		list.add(index, mapper.map(e));
		super.add(index, e);
	}

	@Override
	public boolean add(T e) {
		add(super.size(), e);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return addAll(super.size(), c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		for(T e : c)
			add(index++, e);
		return (c.size() > 0);
	}

	@Override
	public void clear() {
		super.clear();
		list.clear();
	}

	@Override
	public Iterator<T> iterator() {
		final Iterator<T> superIter = super.iterator();
		final Iterator<V> xmlListIter = list.iterator();
		
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return superIter.hasNext();
			}

			@Override
			public T next() {
				xmlListIter.next();
				return superIter.next();
			}

			@Override
			public void remove() {
				superIter.remove();
				xmlListIter.remove();
			}			
		};
	}

	@Override
	public ListIterator<T> listIterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		return (remove(super.indexOf(o)) != null);
	}

	@Override
	public T remove(int index) {
		T ret = null;
		if(index >= 0 && index < super.size()) {
			// First, remove from this list
			ret = super.remove(index);
			if(ret != null) {
				// Now remove from the wrapped list
				V other = list.remove(index);
				if(other == null || mapper.map(ret) != other) {
					// We couldn't remove it from the wrapped list, so add
					// the one back into this list and return null
					super.add(index, ret);
					ret = null;
				}
			}
		}
		return ret;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T set(int index, T e) {
		list.set(index, mapper.map(e));
		return super.set(index, e);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}
}
