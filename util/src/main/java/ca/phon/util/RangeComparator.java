/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.util.Comparator;

public class RangeComparator implements Comparator<Range> {

	@Override
	public int compare(Range o1, Range o2) {
		
		if(o1.equals(o2))
			return 0;
		
		if(o1.getStart() == o2.getStart()) {
			// the longest range is the highest
			if(o1.getRange() <= o2.getRange())
				return 1;
			else
				return -1;
		} else if(o1.getStart() > o2.getStart()) {
			return 1;
		} else if(o1.getStart() < o2.getStart()) {
			return -1;
		}
		return 0;
	}

}
