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
package ca.phon.session;

import java.util.*;
import java.util.regex.*;

import ca.phon.extensions.Extension;
import ca.phon.ipa.IPATranscript;
import ca.phon.util.Language;

/**
 * Extension which provides the default syllabifier selection for the session.
 * 
 * Each {@link IPATranscript} tier may have it's own syllabifier selection.
 *
 */
@Extension(value=Session.class)
public class SyllabifierInfo {
	
	private final static String COMMENT_FORMAT = "syllabifier: %s, tier: %s";
	
	private final static String COMMENT_PATTERN = "syllabifier: ([a-zA-Z]{2,3}(-[a-zA-Z]{1,8})*), tier: (.+)";
	
	/**
	 * Map of tier name to syllabifier languague
	 *
	 */
	private final Map<String, Language> syllabifierMap = new HashMap<String, Language>();
	
	public SyllabifierInfo() {
		super();
	}
	
	public SyllabifierInfo(Session session) {
		super();
		
		updateInfo(session);
	}
	
	/**
	 * Updates info from session comments.
	 * 
	 * @param session
	 * 
	 */
	public void updateInfo(Session session) {
		final Pattern commentPattern = Pattern.compile(COMMENT_PATTERN);
		for(int i = 0; i < session.getMetadata().getNumberOfComments(); i++) {
			final Comment comment = session.getMetadata().getComment(i);
			if(comment.getType() == CommentEnum.Generic) {
				final String commentValue = comment.getValue();
				final Matcher matcher = commentPattern.matcher(commentValue);
				if(matcher.matches()) {
					final String langString = matcher.group(1);
					final String tierName = matcher.group(3);
					syllabifierMap.put(tierName, Language.parseLanguage(langString));
				}
			}
		}
	}
	
	/**
	 * Updates comments for session
	 * @param session
	 */
	public void saveInfo(Session session) {
		final Pattern commentPattern = Pattern.compile(COMMENT_PATTERN);
		final List<String> updatedTiers = new ArrayList<String>();
		final List<Integer> toRemove = new ArrayList<Integer>();
		for(int i = 0; i < session.getMetadata().getNumberOfComments(); i++) {
			final Comment comment = session.getMetadata().getComment(i);
			if(comment.getType() == CommentEnum.Generic) {
				final String commentValue = comment.getValue();
				final Matcher matcher = commentPattern.matcher(commentValue);
				if(matcher.matches()) {
					final String langString = matcher.group(1);
					final String tierName = matcher.group(3);
					if(syllabifierMap.containsKey(tierName)) {
						updatedTiers.add(tierName);
						comment.setValue(String.format(COMMENT_FORMAT, syllabifierMap.get(tierName).toString(), tierName));
					} else {
						toRemove.add(i);
					}
				}
			}
		}
		
		// remove comments we no longer need
		Collections.sort(toRemove);
		for(int i = toRemove.size() - 1; i >= 0; i--) {
			session.getMetadata().removeComment(i);
		}
		
		// add comments that don't exist yet
		final SessionFactory factory = SessionFactory.newFactory();
		final Set<String> set = new HashSet<String>(syllabifierMap.keySet());
		set.removeAll(updatedTiers);
		for(String tierName:set) {
			if(tierName == null) break;
			final Comment c = 
					factory.createComment(CommentEnum.Generic, String.format(COMMENT_FORMAT, syllabifierMap.get(tierName).toString(), tierName));
			session.getMetadata().addComment(c);
		}
		
	}
	
	public Language getSyllabifierLanguageForTier(String tier) {
		return syllabifierMap.get(tier);
	}

	public void setSyllabifierLanguageForTier(String tier, Language lang) {
		syllabifierMap.put(tier, lang);
	}
	
}
