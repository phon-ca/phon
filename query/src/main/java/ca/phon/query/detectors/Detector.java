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
package ca.phon.query.detectors;

import java.util.ArrayList;
import java.util.Collection;

import ca.phon.ipa.alignment.PhoneMap;

/**
 * An abstraction of a process detector.
 * 
 */
public abstract class Detector {
	protected ArrayList<DetectorResult> results;
	protected PhoneMap map;
	
	public Detector() {
		this.results = new ArrayList<DetectorResult>();
	}
	
	/**
     * Perform detection.
	 * @param target  ArrayList of Phones for target form
	 * @param actual  ArrayList of Phones for actual form
	 */
	public Collection<DetectorResult> detect(PhoneMap pm)
	{
		// Clear any results that existed from last run
    	this.results.clear();
		map = pm;
		performDetection();
    	return results;
	}
	
	protected abstract void performDetection();
	
}
