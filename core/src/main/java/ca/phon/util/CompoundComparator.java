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
package ca.phon.util;

import java.util.*;

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
