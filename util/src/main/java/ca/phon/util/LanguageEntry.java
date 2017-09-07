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
package ca.phon.util;

import java.util.*;

/**
 * An entry in the ISO-639-3 code set.  May also
 * include custom userIds.
 * 
 */
public class LanguageEntry {
	
	/** Unknown language */
	private static final String UNKNOWN_LANG = "xxx				I	L	Unknown";
	
	/** 639-3 code */
	public final static String ID_639_3 = "Id";
	/** 639-2 bibliographic */
	public final static String ID_639_2B = "Part2B";
	/** 639-2 terminology */
	public final static String ID_639_2T = "Part2T";
	/** 639-1 code */
	public final static String ID_639_1 = "Part1";
	/** 
	 * One of:
	 *  - I(ndividual)
	 *  - M(acrolanguage)
	 *  - S(pecial)
	 */
	public final static String SCOPE = "Scope";
	/**
	 * One of:
	 *  - A(ncient)
	 *  - C(onstructed)
	 *  - E(xtinct)
	 *  - H(istorical)
	 *  - L(iving)
	 *  - S(pecial)
	 */
	public final static String TYPE = "Type";
	/**
	 * Reference language name
	 */
	public final static String REF_NAME = "Ref_Name";
	/**
	 * Comments
	 */
	public final static String COMMENT = "Comment";
	
	/**
	 * User ids
	 */
	public final static String USER_ID = "UserIDs";
	
	/**
	 * Default constructor
	 */
	public LanguageEntry() {
		String[] fields = UNKNOWN_LANG.split("\t");
		
//		LanguageEntry retVal = new LanguageEntry();
		addProperty(ID_639_3, fields[0]);
		addProperty(ID_639_2B, fields[1]);
		addProperty(ID_639_2T, fields[2]);
		addProperty(ID_639_1, fields[3]);
		addProperty(SCOPE, fields[4]);
		addProperty(TYPE, fields[5]);
		addProperty(REF_NAME, fields[6]);
		addProperty(USER_ID, "");
	}
	
	public LanguageEntry(Map<String, String> props) {
		for(String key:props.keySet()) {
			addProperty(key, props.get(key));
		}
	}
	
	private Hashtable<String, String> props = new Hashtable<String, String>();
	
	/**
	 * Add a new property to this entry
	 * @param name   the name of the property
	 * @param value  the value of the property
	 */
	void addProperty(String name, String value) {
		props.put(name, value);
	}
	
	/**
	 * Get the alpha-3 (ISO 639-3) code for this language entry.
	 * @return  the type of this entry, or null if it is unknown
	 */
	public String getId() {
		return props.get(ID_639_3);
	}
	
	/**
	 * Returns the custom User ID added to this language
	 * 
	 * @return User ID or empty string
	 */
	public String getUserId() {
		return props.get(USER_ID);
	}
	
	/**
	 * Get the 639-2 bibliographic code
	 */
	public String getBibliographicId() {
		return props.get(ID_639_2B);
	}
	
	/**
	 * Get the 639-2 terminology code
	 */
	public String getTerminologyId() {
		return props.get(ID_639_2B);
	}
	
	/**
	 * Get the 2-letter (ISO 639-1) code
	 */
	public String getAlpha2Id() {
		return props.get(ID_639_1);
	}
	
	/**
	 * Get the language scope
	 */
	public String getScope() {
		return props.get(SCOPE);
	}
	
	/**
	 * Get lanugage type
	 */
	public String getType() {
		return props.get(TYPE);
	}
	
	/**
	 * Get language reference name
	 */
	public String getName() {
		return props.get(REF_NAME);
	}
	
	/**
	 * Find and retrieve the value of a property.
	 * @param name  the name of the property to check for
	 * @return      the value of the property if it exists, null otherwise
	 */
	public String getProperty(String name) {
		return props.get(name);
	}
	
	@Override
	public String toString() {
		return getId() + (getUserId().length() > 0 ? "-" + getUserId() : "");
	}
}