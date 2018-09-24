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

import java.text.Collator;
import java.text.ParseException;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.ipa.IPAElement;

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
