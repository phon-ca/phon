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
package ca.phon.ipa.features;

/**
 * Feature family
 */
public enum FeatureFamily {
	PLACE("place"),
	MANNER("manner"),
	HEIGHT("height"),
	TONGUE_ROOT("tongue root"),
	BACKNESS("backness"),
	DIACRITIC("diacritic"),
	LABIAL("labial"),
	DORSAL("dorsal"),
	CORONAL("coronal"),
	VOICING("voicing"),
	CONTINUANCY("continuancy"),
	NASALITY("nasality"),
	STRIDENCY("stridency"),
	GUTTURAL("guttural"),
	UNDEFINED("undefined");
	
	private final String value;
	
	FeatureFamily(String v) {
	    value = v;
	}
	
	public String value() {
	    return value;
	}
	
	public static FeatureFamily fromValue(String v) {
	    for (FeatureFamily c: FeatureFamily.values()) {
	        if (c.value.equals(v)) {
	            return c;
	        }
	    }
	    throw new IllegalArgumentException(v);
	}
}
