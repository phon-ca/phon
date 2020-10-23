/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.session;

import java.util.*;
import java.util.regex.*;

import ca.phon.extensions.*;
import ca.phon.ipa.*;
import ca.phon.util.*;

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
			if(comment.getTag().equals("Generic")) {
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
			if(comment.getTag().equals("Generic")) {
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
					factory.createComment("Generic", String.format(COMMENT_FORMAT, syllabifierMap.get(tierName).toString(), tierName));
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
