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
package ca.phon.phonex;

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.Pause;
import ca.phon.ipa.Phone;
import ca.phon.ipa.StressMarker;
import ca.phon.ipa.SyllableBoundary;
import ca.phon.ipa.WordBoundary;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Filter a iterable list of phones based on phonex
 * flags.
 */
public class PhonexFlagVisitor extends VisitorAdapter<IPAElement> {
	
	/**
	 * Return value
	 */
	private final List<IPAElement> filteredList = 
			new ArrayList<IPAElement>();
	
	/**
	 * phonex flags
	 */
	

	@Override
	public void fallbackVisit(IPAElement obj) {
		
	}
	
	@Visits
	public void basicPhone(Phone phone) {
		
	}
	
	@Visits
	public void compoundPhone(CompoundPhone phone) {
		
	}
	
	@Visits
	public void pause(Pause phone) {
		
	}
	
	@Visits
	public void stress(StressMarker phone) {
		
	}

	@Visits
	public void syllableBoundary(SyllableBoundary phone) {
		
	}
	
	@Visits
	public void wordBoundary(WordBoundary phone) {
		
	}
}
