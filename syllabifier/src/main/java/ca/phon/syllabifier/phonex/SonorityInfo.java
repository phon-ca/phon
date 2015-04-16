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
package ca.phon.syllabifier.phonex;

import ca.phon.extensions.Extension;
import ca.phon.ipa.IPAElement;

/**
 * Sonority information used during syllabification.
 *
 */
@Extension(IPAElement.class)
public class SonorityInfo {
	
	/**
	 * Sonority value, an integer
	 */
	private int sonority = 0;
	
	/**
	 * Sonority distance from previous phone
	 */
	private int distance = 0;
	
	public SonorityInfo() {
		
	}
	
	public SonorityInfo(int sonority, int distance) {
		this.sonority = sonority;
		this.distance = distance;
	}

	public int getSonority() {
		return sonority;
	}

	public void setSonority(int sonority) {
		this.sonority = sonority;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

}
