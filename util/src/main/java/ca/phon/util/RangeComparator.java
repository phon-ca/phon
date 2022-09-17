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
