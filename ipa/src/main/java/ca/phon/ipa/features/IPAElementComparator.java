package ca.phon.ipa.features;

import java.text.Collator;
import java.text.ParseException;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.ipa.IPAElement;

public class IPAElementComparator implements Comparator<IPAElement> {
	
	private final static Logger LOGGER = Logger.getLogger(IPAElementComparator.class.getName());

	@Override
	public int compare(IPAElement o1, IPAElement o2) {
		int retVal = 0;
		try {
			final Collator collator = new IPACollator();
			retVal = collator.compare(o1.toString(), o2.toString());
			retVal = (retVal > 0 ? 1 : (retVal < 0 ? -1 : 0));
		} catch (ParseException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return retVal;
	}

}
