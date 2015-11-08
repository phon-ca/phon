package ca.phon.ipa.features;

import java.util.Collection;
import java.util.Comparator;

import ca.phon.ipa.IPAElement;
import ca.phon.util.CompoundComparator;

/**
 * A compound comparator for features, with a fallback to a string comparator
 *
 */
public class CompoundFeatureComparator extends CompoundComparator<IPAElement> {

	public CompoundFeatureComparator() {
		super();
	}

	public CompoundFeatureComparator(
			Collection<Comparator<IPAElement>> comparators) {
		super(comparators);
	}

	@SafeVarargs
	public CompoundFeatureComparator(Comparator<IPAElement>... comparators) {
		super(comparators);
	}

	@Override
	public int compare(IPAElement o1, IPAElement o2) {
		int retVal = super.compare(o1, o2);
		
		if(retVal == 0) {
			retVal = o1.toString().compareTo(o2.toString());
			retVal = (retVal > 0 ? 1 : (retVal < 0 ? -1 : 0));
		}
		
		return retVal;
	}
	
}
