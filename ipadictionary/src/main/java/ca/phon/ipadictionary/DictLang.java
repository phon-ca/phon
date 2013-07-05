package ca.phon.ipadictionary;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.phon.ipadictionary.spi.LanguageInfo;
import ca.phon.util.LanguageEntry;
import ca.phon.util.LanguageParser;

/**
 * Simple class representing the language of an IPADictionary.
 * 
 * Dictionary language is a String with format:
 * 
 * ([a-z]{3})-([a-zA-Z0-9]{1,8})*
 *
 * The first group represent the 3-letter ISO code of the
 * language, returned as a {@link LanguageEntry} object.
 * 
 * The second group containes user-defined ids for the
 * dictionary.
 */
public class DictLang implements LanguageInfo {
	
	private LanguageEntry language;
	
	private String[] userIds;
	
	/**
	 * Parse the DictLang given a string
	 */
	public static DictLang fromString(String lang) {
		final Pattern pattern = Pattern.compile("([a-z]{3})(-[a-zA-Z0-9]{1,8})*");
		final Matcher matcher = pattern.matcher(lang);
		
		final DictLang retVal = new DictLang();
		if(matcher.matches()) {
			final LanguageEntry entry  = LanguageParser.getInstance().getEntryById(matcher.group(1));
			
			if(entry != null)
				retVal.setLanguage(entry);
			
			if(matcher.group(2) != null) {
				final String userStr = lang.substring(matcher.end(1)+1);
				final String userIds[] = userStr.split("-");
				retVal.setUserIds(userIds);
			}
		}
		return retVal;
	}
	
	public DictLang() {
		this(new LanguageEntry());
	}

	public DictLang(String lang) {
		this(LanguageEntry.fromString(lang));
	}
	
	public DictLang(LanguageEntry language) {
		this(language, new String[0]);
	}
	
	public DictLang(LanguageEntry language, String[] userIds) {
		this.language = language;
		this.userIds = userIds;
	}

	@Override
	public LanguageEntry getLanguage() {
		return language;
	}

	public void setLanguage(LanguageEntry language) {
		this.language = language;
	}

	@Override
	public String[] getUserIds() {
		return userIds;
	}

	public void setUserIds(String[] userIds) {
		this.userIds = userIds;
	}
	
	@Override
	public boolean equals(Object b) {
		if(!(b instanceof LanguageInfo)) return false;
		
		final LanguageInfo bInfo = (LanguageInfo)b;
		
		boolean primaryMatch = getLanguage().getId().equals(bInfo.getLanguage().getId());
		boolean idMatch = getUserIds().length == bInfo.getUserIds().length;
		if(idMatch) {
			for(int i = 0; i < getUserIds().length; i++) {
				idMatch &= getUserIds()[i].equals(bInfo.getUserIds()[i]);
			}
		}
		
		return primaryMatch && idMatch;
	}
	
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(getLanguage().getId());
		for(String userId:getUserIds()) {
			builder.append("-");
			builder.append(userId);
		}
		return builder.toString();
	}
	
}
