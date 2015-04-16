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
