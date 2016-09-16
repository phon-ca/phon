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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a language.
 * 
 * A language description comes in two parts: a 3-letter country code
 * and a list of user ids for the language.  The syntax for the 
 * language descriptor is: ([a-z]{3})(-[a-zA-Z0-9]{1,8})*
 */
public class Language implements Comparable<Language> {

	private final static String langRegex = "([a-z]{3})(-[a-zA-Z0-9]{1,8})*";
	
	private LanguageEntry primaryLanguage;
	
	private String[] userIDs;

	/**
	 * Create a new language from the given string
	 * 
	 * @param lang
	 * @return
	 * 
	 * @throws IllegalArgumentException
	 */
	public static Language parseLanguage(String lang) {
		final Language retVal = new Language();
		retVal.parse(lang);
		return retVal;
	}
	
	public Language() {
		super();
		this.primaryLanguage = new LanguageEntry();
		this.userIDs = new String[0];
	}
	
	public Language(LanguageEntry primaryLanguage) {
		this(primaryLanguage, new String[0]);
	}
	
	public Language(LanguageEntry primaryLanguage, String[] userIDs) {
		super();
		this.primaryLanguage = primaryLanguage;
		this.userIDs = userIDs;
	}
	
	private void parse(String lang) {
		if(lang != null && lang.matches(langRegex)) {
			final String[] split = lang.split("-");
			
			if(split.length > 0) {
				final LanguageParser parser = LanguageParser.getInstance();
				primaryLanguage = parser.getEntryById(split[0]);
				
				if(primaryLanguage == null) {
					final Map<String, String> langProps = new HashMap<>();
					langProps.put(LanguageEntry.ID_639_3, split[0]);
					langProps.put(LanguageEntry.ID_639_2B, split[0].substring(0, 2));
					langProps.put(LanguageEntry.REF_NAME, "");
					primaryLanguage = new LanguageEntry(langProps);
				}
				
				userIDs = new String[split.length - 1];
				for(int i = 1; i < split.length; i++) userIDs[i-1] = split[i];
			}
		} else {
			throw new IllegalArgumentException("Invalid lang string: " + lang);
		}
	}

	public LanguageEntry getPrimaryLanguage() {
		return primaryLanguage;
	}

	public void setPrimaryLanguage(LanguageEntry primaryLanguage) {
		this.primaryLanguage = primaryLanguage;
	}

	public String[] getUserIDs() {
		return userIDs;
	}

	public void setUserIDs(String[] userIDs) {
		this.userIDs = userIDs;
	}
	
	public void appendUserID(String userID) {
		this.userIDs = Arrays.copyOf(userIDs, userIDs.length + 1);
		this.userIDs[this.userIDs.length-1] = userID;
	}
	
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(primaryLanguage.getId());
		
		for(String userId:userIDs) {
			builder.append("-");
			builder.append(userId);
		}
		return builder.toString();
	}
	
	@Override
	public boolean equals(Object b) {
		if(!(b instanceof Language)) return false;
		final Language bLang = (Language)b;
		
		boolean primaryMatch = primaryLanguage == bLang.primaryLanguage;
		boolean userIDsMatch = Arrays.equals(userIDs, bLang.userIDs);
		
		return primaryMatch && userIDsMatch;
	}

	@Override
	public int compareTo(Language o) {
		if(toString() == null) return -1;
		if(o == null || o.toString() == null) return 1;
		return toString().compareTo(o.toString());
	}
	
}
