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
package ca.phon.syllabifier.phonex;

import ca.phon.ipa.IPAElement;
import ca.phon.phonex.PhoneMatcher;

/**
 * Phone matcher for sonority distance.
 *
 */
public class SonorityDistancePhoneMatcher implements PhoneMatcher {
	
	/**
	 * Minimum distance
	 */
	private int minDistance;
	
	/**
	 * Allow flat sonority? (default:false)
	 */
	private boolean allowFlat;

	public SonorityDistancePhoneMatcher() {
		this(0, false);
	}
	
	public SonorityDistancePhoneMatcher(int min) {
		this(min, false);
	}
	
	public SonorityDistancePhoneMatcher(int min, boolean allowFlat) {
		this.minDistance = min;
		this.allowFlat = allowFlat;
	}
	
	@Override
	public boolean matches(IPAElement p) {
		boolean retVal = false;
		
		int distance = 0;
		SonorityInfo info = p.getExtension(SonorityInfo.class);
		if(info != null) {
			distance = info.getDistance();
		}
		if(Math.abs(distance) > 0) {
			if(this.minDistance < 0) {
				retVal = distance <= this.minDistance;
			} else {
				retVal = distance >= this.minDistance;
			}
		} else {
			retVal = allowFlat;
		}
		
		return retVal;
	}

	@Override
	public boolean matchesAnything() {
		return false;
	}

}
