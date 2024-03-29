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
package ca.phon.ipa.features;

import ca.phon.ipa.IPAElement;

import java.text.*;
import java.util.Comparator;

public class IPAElementComparator implements Comparator<IPAElement> {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(IPAElementComparator.class.getName());

	@Override
	public int compare(IPAElement o1, IPAElement o2) {
		int retVal = 0;
		try {
			final Collator collator = new IPACollator();
			retVal = collator.compare(o1.toString(), o2.toString());
			retVal = (retVal > 0 ? 1 : (retVal < 0 ? -1 : 0));
		} catch (ParseException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
		return retVal;
	}

}
