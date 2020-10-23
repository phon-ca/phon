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

import java.io.*;
import java.util.*;

import javax.xml.bind.*;

import ca.phon.ipa.xml.*;

/**
 * Maps individual glyphs to their IPA token type.
 * Tokens are loaded from the ipa.xml file.
 */
public final class IPATokens {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(IPATokens.class
			.getName());
	
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
	 * @param ipaXMLFile - the xml file from which to
	 *  read the ipa tokens.  Retrieved using the
	 *  getResourceAsStream() method of the system classloader.
	 * @param antlrTokensFile - mapping of token names to integer
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
	private Map<Character, IPATokenType> tokenMap() {
		if(tokenMap == null) {
			tokenMap = new TreeMap<Character, IPATokenType>();
			reverseMap = new TreeMap<IPATokenType, Set<Character>>();
			tokenNames = new TreeMap<Character, String>();
			
			try {
				// read in xml token file
				final JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
				final Unmarshaller unmarshaller = context.createUnmarshaller();
				
				final JAXBElement<?> ipaEle = 
					JAXBElement.class.cast(unmarshaller.unmarshal(
							getClass().getResourceAsStream(ipaTokensPath)));
				// make sure we have the right type of element
				if(ipaEle.getDeclaredType() == IpaType.class) {
					IpaType ipaDoc = 
						IpaType.class.cast(ipaEle.getValue());
					for(CharType gt:ipaDoc.getChar()) {
						// parse unicode value
						Integer intVal = null;
						try {							
							final Character c = gt.getValue().charAt(0);
							
							tokenNames.put(c, gt.getName());
							
							if(gt.getToken() == null)
								System.out.println(c);
							final IPATokenType tt = IPATokenType.fromXMLType(gt.getToken());
							
							tokenMap.put(c, tt);
							
							Set<Character> tokenChars = reverseMap.get(tt);
							if(tokenChars ==  null) {
								tokenChars = new TreeSet<Character>();
								reverseMap.put(tt, tokenChars);
							}
							tokenChars.add(c);
						} catch (NumberFormatException nfe) {
							LOGGER.warn(nfe.getMessage());
							nfe.printStackTrace();
						}
					}
				}
			} catch (JAXBException e) {
				LOGGER.error(e.getMessage());
				e.printStackTrace();
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
				LOGGER.error(e.getMessage());
				e.printStackTrace();
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
	 * @param tokenType
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
