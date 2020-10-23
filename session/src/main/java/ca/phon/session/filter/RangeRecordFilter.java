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
package ca.phon.session.filter;

import java.text.*;
import java.util.*;

import org.apache.commons.lang3.*;

import ca.phon.session.*;
import ca.phon.session.Record;
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
	
	public void setSession(Session s) {
		this.transcript = s;
	}
	
	public Session getSession() {
		return this.transcript;
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
