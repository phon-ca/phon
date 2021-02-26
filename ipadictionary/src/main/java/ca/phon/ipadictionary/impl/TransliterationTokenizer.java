package ca.phon.ipadictionary.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.hedlund.tst.TernaryTree;

/**
 * Create tokens from a given input string. The string
 * will be tokenized based on a given input map or by
 * each character if a sequence is not found in the map.
 * 
 */
public class TransliterationTokenizer {

	private final TernaryTree<String> tokenTree = new TernaryTree<>();
	
	private boolean ignoreWhitespace = false;
	
	public TransliterationTokenizer() {
		this(new HashMap<>());
	}
	
	public TransliterationTokenizer(Map<String, String> tokenMap) {
		super();
		for(var entry:tokenMap.entrySet()) {
			tokenTree.put(entry.getKey(), entry.getValue());
		}
	}
	
	public Map<String, String> getTokenMap() {
		return Collections.unmodifiableMap(tokenTree);
	}
	
	public boolean isIgnoreWhitespace() {
		return this.ignoreWhitespace;
	}
	
	public void setIgnoreWhitespace(boolean ignoreWhitespace) {
		this.ignoreWhitespace = ignoreWhitespace;
	}
	
	public boolean isEmpty() {
		return tokenTree.isEmpty();
	}

	public boolean containsKey(String key) {
		return tokenTree.containsKey(key);
	}

	public String get(String key) {
		return tokenTree.get(key);
	}

	public String put(String key, String value) {
		return tokenTree.put(key, value);
	}

	public Set<String> keySet() {
		return tokenTree.keySet();
	}

	public String[] tokenize(CharSequence input) {
		List<String> retVal = new ArrayList<>();
		
		int idx = 0;
		String currentToken = "";
		int currentMatchIdx = -1;
		String currentMatch = "";
		
		while(idx < input.length()) {
			char c = input.charAt(idx);
			if(Character.isWhitespace(c)) {
				if(currentMatchIdx >= 0) {
					retVal.add(currentMatch);
					currentToken = currentToken.substring(currentMatch.length());
				} 
				// add each character as a separate token since no match was found
				for(char ch:currentToken.toCharArray()) retVal.add("" + ch);
				if(!isIgnoreWhitespace()) {
					retVal.add("" + c);
				}
				currentToken = "";
				currentMatchIdx = -1;
				currentMatch = "";
				++idx;
				continue;
			}
			
			currentToken += input.charAt(idx);
			
			// do we have a match
			boolean match = tokenTree.containsKey(currentToken);
			
			// are we a prefix to a longer match
			final String testToken = currentToken;
			boolean isPrefix = tokenTree.keysWithPrefix(currentToken)
						.stream().filter( (key) -> !key.equals(testToken) ).findAny().isPresent();
			
			if(match && !isPrefix) {
				// accept token
				retVal.add(currentToken);
				currentToken = "";
				currentMatchIdx = -1;
				currentMatch = "";
			} else if(match && isPrefix) {
				currentMatchIdx = idx;
				currentMatch = currentToken;
			} else if(!match && !isPrefix) {
				if(currentMatchIdx >= 0) {
					retVal.add(currentMatch);
					idx -= (currentToken.length() - currentMatch.length());
					
				} else {
					retVal.add("" + currentToken.charAt(0));
					idx -= (currentToken.length()-1);
				}
				currentToken = "";
				currentMatchIdx = -1;
				currentMatch = "";
			}
			++idx;
			if(idx >= input.length() && currentToken.length() > 0) {
				if(currentMatchIdx >= 0) {
					retVal.add(currentMatch);
					currentToken = currentToken.substring(currentMatch.length());
				} 
				// add each character as a separate token since no match was found
				for(char ch:currentToken.toCharArray()) retVal.add("" + ch);
			}
		}
		
		return retVal.toArray(new String[0]);
	}
	
	public String transliterate(String input) {
		return transliterate(tokenize(input));
	}

	private final static String toneNumberRegex =
			"[\u2070\u00b9\u00b2\u00b3\u2074\u2075\u2076\u2077\u2078\u2079]+";
	public String transliterate(String[] tokens) {
		StringBuffer buffer = new StringBuffer();
		
		for(String token:tokens) {
			if(tokenTree.containsKey(token)) {
				String tval = tokenTree.get(token);

				// take care of a special case where
				// syllabification is included in the transliteration lookup
				// and we are adding diacritics
				if(tval.matches(toneNumberRegex) &&
						buffer.toString().matches(".+:[a-zA-Z]$")) {
					buffer.insert(buffer.length()-2, tval);
				} else {
					buffer.append(tval);
				}
			} else {
				buffer.append(token);
			}
		}
		
		return buffer.toString();
	}
	
}
