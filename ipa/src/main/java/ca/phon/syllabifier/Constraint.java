/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.syllabifier;

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.phone.Phone;
import ca.phon.syllabifier.io.BooleanConnector;
import ca.phon.syllabifier.io.BooleanOperator;
import ca.phon.syllabifier.io.ConstraintType;
import ca.phon.util.Range;

public class Constraint extends ConstraintType {
	
	public boolean matchesPhone(Phone phone) {
		List<Phone> testList = new ArrayList<Phone>();
		testList.add(phone);
		
		return findRangesInList(testList).size() != 0;
	}
	
	public boolean matchesEmptyList() {
		boolean retVal = true;
		
		boolean notResult = false;
		boolean andValues = true;
		
		for(Object obj:this.getOperatorAndPhoneSequenceAndConnector()) {
			if(obj instanceof ConstraintType.Operator) {
				ConstraintType.Operator op = (ConstraintType.Operator)obj;
				if(op.getType() == BooleanOperator.NOT) {
					notResult = true;
					continue;
				}
			} else if(obj instanceof ConstraintType.Connector) {
				ConstraintType.Connector connector = (ConstraintType.Connector)obj;
				if(connector.getType() == BooleanConnector.OR)
					andValues = false;
				else
					andValues = true;
			} else {
				if(!(obj instanceof SyllabificationRule))
					continue;
				
				SyllabificationRule rule  = (SyllabificationRule)obj;
				boolean ruleAllowsEmpty = rule.matchesEmptyList();
				
				if(notResult)
					ruleAllowsEmpty = !ruleAllowsEmpty;
				
				if(andValues)
					retVal &= ruleAllowsEmpty;
				else
					retVal |= ruleAllowsEmpty;
			}
		}
		
		return retVal;
	}
	
	public List<Range> findRangesInList(List<Phone> tape) {
		List<Range> retVal = null;
		
		boolean notResult = false;
		boolean andValues = true;
		
		for(Object obj:this.getOperatorAndPhoneSequenceAndConnector()) {
			if(obj instanceof ConstraintType.Operator) {
				ConstraintType.Operator op = (ConstraintType.Operator)obj;
				if(op.getType() == BooleanOperator.NOT) {
					notResult = true;
					continue; // move to next in list
				}
			} else if(obj instanceof ConstraintType.Connector) {
				ConstraintType.Connector connector = (ConstraintType.Connector)obj;
				if(connector.getType() == BooleanConnector.OR)
					andValues = false;
				else
					andValues = true;
			} else {
				// we have a constraint, all of our constraints implement SyllabificationRule
				// only allow objects that are of this type
				if(!(obj instanceof SyllabificationRule))
					continue;
				SyllabificationRule rule  = (SyllabificationRule)obj;
				List<Range> currentList = rule.findRangesInList(tape);
				
				if(notResult) {
					currentList = notRangeList(currentList, tape.size());
					notResult = false; // reset flag
				}
				
				if(retVal == null) {
					retVal = currentList;
				} else {
					if(andValues) {
						retVal = andRangeLists(retVal, currentList);
					} else {
						retVal = orRangeLists(retVal, currentList);
					}
				}
			}
		}
		
		return retVal;
	}
	
	public List<Range> notRangeList(List<Range> aList, int maxValue) {
		List<Range> retVal = new ArrayList<Range>();
		
		for(int i = 0; i < maxValue; i++) {
			boolean found = false;
			for(Range aRange:aList) {
				if(aRange.contains(i)) {
					found = true;
					break;
				}
			}
			if(!found)
				retVal.add(new Range(i, i));
		}
		
		return retVal;
	}
	
	public List<Range> orRangeLists(List<Range> aList, List<Range> bList) {
		List<Range> retVal = new ArrayList<Range>();
		
		retVal.addAll(aList);
		
		for(Range bRange:bList) {
			if(!retVal.contains(bRange))
				retVal.add(bRange);
		}
		
		return retVal;
	}
	
	public List<Range> andRangeLists(List<Range> aList, List<Range> bList) {
		List<Range> retVal = new ArrayList<Range>();
		
		for(Range outerRange:aList) {
			for(Range innerRange:bList) {
				Range intersect = outerRange.intersect(innerRange);
				if(intersect != null) retVal.add(intersect);
			}
		}
		
		return retVal;
	}

}
