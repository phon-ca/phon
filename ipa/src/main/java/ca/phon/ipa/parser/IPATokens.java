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
package ca.phon.ipa.parser;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Maps individual glyphs to their IPA token type.
 * Tokens are loaded from the ipa.xml file.
 */
public final class IPATokens {
	
	/**
	 * Location of ipa token definitions
	 */
	private final static String IPA_TOKENS = "ipa.xml";
	
	/**
	 * Locaiton of antlr token type file
	 */
	private final static String ANTLR_TOKENS = "IPA.tokens";
	
	/**
	 * Location of xml file
	 */
	private final String ipaTokensPath;
	
	/**
	 * Loacation of the antlr tokens file
	 */
	private final String antlrTokensPath;
	
	/**
	 * Token map
	 */
	private Map<Character, IPATokenType> tokenMap;
	
	/**
	 * Reversed token map
	 */
	private Map<IPATokenType, Set<Character>> reverseMap;
	
	/**
	 * Antlr token map
	 */
	private Map<IPATokenType, Integer> antlrTokenMap;
	
	/**
	 * Token names
	 */
	private Map<Character, String> tokenNames;
	
	/**
	 * Shared instance
	 */
	private static IPATokens _sharedInstance;
	
	public static IPATokens getSharedInstance() {
		if(_sharedInstance == null) {
			_sharedInstance = new IPATokens();
		}
		return _sharedInstance;
	}
	
	/**
	 * Constructor
	 *
	 */
	IPATokens() {
		this(IPA_TOKENS, ANTLR_TOKENS);
	}
	
	/**
	 * Hidden Constructor
	 * 
	 * @param ipaXMLPath - the xml file from which to
	 *  read the ipa tokens.  Retrieved using the
	 *  getResourceAsStream() method of the system classloader.
	 * @param antlrTokensPath - mapping of token names to integer
	 *  values for the ANTLR IPA parser. Retrieved using the
	 *  getResourceAsStream() method of the system classloader.
	 */
	private IPATokens(String ipaXMLPath, String antlrTokensPath) {
		this.ipaTokensPath = ipaXMLPath;
		this.antlrTokensPath = antlrTokensPath;
	}
	
	/**
	 * Return the token map.  Loaded if necessary.
	 * 
	 * @return the token map
	 */
	private synchronized Map<Character, IPATokenType> tokenMap() {
		if(tokenMap == null) {
			tokenMap = new TreeMap<Character, IPATokenType>();
			reverseMap = new TreeMap<IPATokenType, Set<Character>>();
			tokenNames = new TreeMap<Character, String>();

			// read in xml token file using StAX
			try {
				final InputStream in = getClass().getResourceAsStream(ipaTokensPath);
				if(in == null) {
					throw new FileNotFoundException(ipaTokensPath + " not found!");
				}
				char currentChar = '\0';
				String name = "";
				String token = "";
				final XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(in);
				while(reader.hasNext()) {
					reader.next();
					if((reader.isStartElement() && reader.getLocalName().equals("char")) || (reader.isEndElement() && reader.getLocalName().equals("ipa"))) {
						if (currentChar != '\0') {
							// add to tokens
							tokenNames.put(currentChar, name);
							final TokenType tt = TokenType.valueOf(token);
							final IPATokenType ipaTokenType = IPATokenType.fromXMLType(tt);
							tokenMap.put(currentChar, ipaTokenType);

							Set<Character> tokenChars = reverseMap.get(ipaTokenType);
							if(tokenChars ==  null) {
								tokenChars = new TreeSet<Character>();
								reverseMap.put(ipaTokenType, tokenChars);
							}
							tokenChars.add(currentChar);
						}
						if(reader.isStartElement()) {
							final String charVal = reader.getAttributeValue(null, "value");
							currentChar = charVal.charAt(0);
							name = "";
							token = "";
						}
					} else if(reader.isStartElement() && reader.getLocalName().equals("token")) {
						token = reader.getElementText();
					} else if(reader.isStartElement() && reader.getLocalName().equals("name")) {
						name = reader.getElementText();
					}
				}
			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).warning(e.getMessage());
			}
		}
		return tokenMap;
	}
	
	/**
	 * Get the reverse map - create if necessary.
	 * 
	 * @return the reverse token map
	 */
	private Map<IPATokenType, Set<Character>> reverseMap() {
		tokenMap();
		return reverseMap;
	}
	
	/**
	 * Return the map of token type to antlr Integer token value.
	 * Loads from file if necessary.
	 * 
	 * @return the token to antlr type map
	 */
	private Map<IPATokenType, Integer> antlrTokenMap() {
		if(antlrTokenMap == null) {
			antlrTokenMap = new HashMap<IPATokenType, Integer>();
			
			// read in token types as properties
			Properties antlrTokenProps = new Properties();
			try {
				final InputStream in = getClass().getClassLoader().getResourceAsStream(antlrTokensPath);
				if(in == null) {
					throw new FileNotFoundException(antlrTokensPath + " not found!");
				}
				antlrTokenProps.load(in);
			} catch (IOException e) {
				Logger.getLogger(getClass().getName()).warning(e.getMessage());
			}
			
			for(Object key:antlrTokenProps.keySet()) {
				String keyName = key.toString();
				Object keyVal = antlrTokenProps.get(key);
				
				Integer tokenVal = Integer.parseInt(keyVal.toString());
				IPATokenType tt = IPATokenType.fromString(keyName);
				
				if(tt != null && tokenVal >= 0) {
					antlrTokenMap.put(tt, tokenVal);
				}
			}
			
		}
		return antlrTokenMap;
	}

	/**
	 * Return the token type for the given character.
	 * 
	 * @return the given {@link Character}s token type
	 *  or <code>null</code> if not found.
	 */
	public IPATokenType getTokenType(Character c) {
		return tokenMap().get(c);
	}
	
	/**
	 * Return the integer value for the given token type.
	 * 
	 * @param type
	 * @return the integer representation of the token
	 *  or <code>-1</code> if not found.
	 */
	public int getTypeValue(IPATokenType type) {
		Integer mapVal = antlrTokenMap().get(type);
		return (mapVal == null ? -1 : mapVal);
	}
	
	/**
	 * Get the characters with the given token type.
	 * 
	 * @param type the type to lookup
	 * @return the set of characters assigned the
	 *  given type
	 */
	public Set<Character> getCharactersForType(IPATokenType type) {
		return reverseMap().get(type);
	}
	
	/**
	 * Get all characters in the token set
	 * 
	 * @return all possible characters in the IPA token
	 *  language.
	 */
	public Set<Character> getCharacterSet() {
		return tokenMap().keySet();
	}
	
	/**
	 * Get the name of the provided char
	 * 
	 * @param c
	 */
	public String getCharacterName(Character c) {
		return tokenNames.get(c);
	}
}
