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

package ca.phon.util.radixtree;

/**
 * An interface for implementing visitors that can traverse {@link RadixTree}.
 * A visitor defines how to treat a key/value pair in the radix tree, and can
 * also return a result from the traversal.
 * 
 * @param <V> the type stored in the radix tree we will visit
 * @param <R> the type used for results
 */
public interface RadixTreeVisitor<V, R> {
	/**
	 * Visits a node in a radix tree. 
	 * 
	 * @param key  the key of the node being visited 
	 * @param value  the value of the node being visited
	 */
	public abstract void visit(String key, V value);
	
	/**
	 * An overall result from the traversal of the radix tree.
	 * 
	 * @return the result
	 */
	public abstract R getResult();
}
