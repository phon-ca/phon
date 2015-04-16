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
package ca.phon.util.resources;

import java.util.Iterator;

/**
 * Responsible for finding resources for a resource loader.
 * 
 * Implementing classes must defind an iterator
 * for loading objects of the parameterized type.
 */
public interface ResourceHandler<T> extends Iterable<T> {
	
	/**
	 * Return an iterator for instances of
	 * type T.
	 * 
	 * @return an iterator providing instances
	 *  of the parameterized type
	 *  
	 */
	public Iterator<T> iterator();

}
