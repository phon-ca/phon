/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.ipa.features;

import java.text.*;
import java.util.*;

import ca.phon.ipa.*;
import ca.phon.util.*;

/**
 * A compound comparator for features, with a fallback to a string comparator
 *
 */
public class CompoundIPAElementComparator extends CompoundComparator<IPAElement> {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(CompoundIPAElementComparator.class.getName());

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
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		
		return retVal;
	}
	
}
