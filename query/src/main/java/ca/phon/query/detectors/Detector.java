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
package ca.phon.query.detectors;

import ca.phon.ipa.alignment.PhoneMap;

import java.util.*;

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
