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
package ca.phon.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 
 * @param <T>
 */
public class CompoundComparator<T> implements Comparator<T> {
	
	private final List<Comparator<T>> comparators =
			new ArrayList<>();
	
	public CompoundComparator() {
	}
	
	public CompoundComparator (Collection<Comparator<T>> comparators) {
		this.comparators.addAll(comparators);
	}
	
	@SafeVarargs
	public CompoundComparator(final Comparator<T> ... comparators) {
		for(Comparator<T> comparator:comparators) {
			this.comparators.add(comparator);
		}
	}

	@Override
	public int compare(T o1, T o2) {
		int retVal = 0;
		for(Comparator<T> comparator:comparators) {
			retVal = comparator.compare(o1, o2);
			// return the value from the first comparator that says
			// the values are different
			if(retVal != 0) break;
		}		
		return retVal;
	}

}
