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
package ca.phon.ipa.features;

import java.text.*;
import java.util.*;
import java.util.logging.*;

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
