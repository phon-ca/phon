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
