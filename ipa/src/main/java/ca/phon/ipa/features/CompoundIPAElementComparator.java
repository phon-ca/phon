package ca.phon.ipa.features;

import java.text.Collator;
import java.text.ParseException;
import java.util.Collection;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.ipa.IPAElement;
import ca.phon.util.CompoundComparator;

/**
 * A compound comparator for features, with a fallback to a string comparator
 *
 */
public class CompoundIPAElementComparator extends CompoundComparator<IPAElement> {
	
	private final static Logger LOGGER = Logger.getLogger(CompoundIPAElementComparator.class.getName());

	public CompoundIPAElementComparator() {
		super();
	}

	public CompoundIPAElementComparator(
			Collection<Comparator<IPAElement>> comparators) {
		super(comparators);
	}

	@SafeVarargs
	public CompoundIPAElementComparator(Comparator<IPAElement>... comparators) {
		super(comparators);
	}

	@Override
	public int compare(IPAElement o1, IPAElement o2) {
		int retVal = super.compare(o1, o2);
		if(retVal == 0) {
			try {
				final Collator collator = new IPACollator();
				retVal = collator.compare(o1.toString(), o2.toString());
				retVal = (retVal > 0 ? 1 : (retVal < 0 ? -1 : 0));
			} catch (ParseException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
		return retVal;
	}
	
}
