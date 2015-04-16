/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.session;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ca.phon.util.Range;

/**
 * Filters utterances based on their position in the list.
 * 
 * Ranges can be given in string format separated by ',':
 * Ex:
 *   1..2, 4, 8...10
 * Will return utterances 1,2,4,8,9
 */
public class RangeRecordFilter extends AbstractRecordFilter {
	
	/*
	 * Range patterns
	 */
	private final static String pointRangeEx = "[0-9]+";
	private final static String incRangeEx = "([0-9]+)\\.\\.([0-9]+)";
	private final static String exclRangeEx = "[0-9]+\\.\\.\\.[0-9]+";
	
	private List<Range> ranges = new ArrayList<Range>();
	
	private Session transcript;
	
	public RangeRecordFilter(Session t) {
		this.transcript = t;
	}
	
	public RangeRecordFilter(Session t, String ranges) 
		throws ParseException {
		
		this.transcript = t;
		
		String[] splitRanges = ranges.split(",");
		for(String r:splitRanges) {
			Range range = rangeFromString(StringUtils.strip(r));
			this.ranges.add(range);
		}
		
	}
	
	public void addRange(Range r) {
		ranges.add(r);
	}
	
	public void addRange(String rStr) 
		throws ParseException {
		ranges.add(rangeFromString(rStr));
	}
	
	public List<Range> getRanges() {
		return this.ranges;
	}
	
	public void setRanges(List<Range> ranges) {
		this.ranges = ranges;
	}
	
	private Range rangeFromString(String rStr) 
		throws ParseException {
		Range retVal = new Range(0, 0, false);
		
		if(rStr.matches(pointRangeEx)) {
			// single record
			Integer uttIdx = Integer.parseInt(rStr)-1;
			retVal = new Range(uttIdx, uttIdx, false);
		} else if(rStr.matches(incRangeEx)) {
			String v1Str = rStr.substring(0, rStr.indexOf('.'));
			String v2Str = rStr.substring(rStr.lastIndexOf('.')+1);
			
			Integer v1 = Integer.parseInt(v1Str)-1;
			Integer v2 = Integer.parseInt(v2Str)-1;
			
			retVal = new Range(v1, v2, false);
		} else if(rStr.matches(exclRangeEx)) {
			String v1Str = rStr.substring(0, rStr.indexOf('.'));
			String v2Str = rStr.substring(rStr.lastIndexOf('.')+1);
			
			Integer v1 = Integer.parseInt(v1Str)-1;
			Integer v2 = Integer.parseInt(v2Str)-1;
			
			retVal = new Range(v1, v2, true);
		} else {
			throw new ParseException(rStr, 0);
		}
		
		return retVal;
	}
	
	@Override
	public boolean checkRecord(Record utt) {
		int uttIdx = transcript.getRecordPosition(utt);
		
		boolean retVal = false;
		
		if(uttIdx >= 0) {
			for(Range r:ranges) {
				if(r.contains(uttIdx)) {
					retVal = true;
					break;
				}
			}
		}
		
		return retVal;
	}

	
}
