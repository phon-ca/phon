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
