package ca.phon.util;

import java.util.Arrays;

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
	public static Language fromString(String lang) {
		final Language retVal = new Language();
		retVal.parseLanguage(lang);
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
	
	private void parseLanguage(String lang) {
		if(lang != null && lang.matches(langRegex)) {
			final String[] split = lang.split("-");
			
			if(split.length > 0) {
				final LanguageParser parser = LanguageParser.getInstance();
				primaryLanguage = parser.getEntryById(split[0]);
				
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
		return toString().compareTo(o.toString());
	}
	
}
