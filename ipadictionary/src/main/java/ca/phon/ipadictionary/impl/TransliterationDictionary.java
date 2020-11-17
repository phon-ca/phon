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
package ca.phon.ipadictionary.impl;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import org.apache.commons.lang3.*;
import org.apache.logging.log4j.*;

import ca.phon.ipa.*;
import ca.phon.ipadictionary.*;
import ca.phon.ipadictionary.exceptions.*;
import ca.phon.ipadictionary.spi.*;
import ca.phon.phonex.*;
import ca.phon.util.*;

/**
 * An IPADictionary implementation that uses a tokenizer and lookup
 * table for generating ipa transcriptions.
 * 
 */
public class TransliterationDictionary implements IPADictionarySPI,
	LanguageInfo, NameInfo, Metadata {
	
	private enum MetadataToken {
		NAME("name"),
		LANGUAGE("lang"),
		PREPROCESSEXPR("prefind"),
		PREPROCESSREPLACE("prereplace"),
		POSTPROCESSEXPR("postfind"),
		POSTPROCESSREPLACE("postreplace"),
		PHONEXFIND("phonexfind"),
		PHONEXREPLACE("phonexreplace"),
		OTHER("other");
		
		private String value;
		
		private MetadataToken(String v) {
			this.value = v;
		}
		
		@Override
		public String toString() {
			return this.value;
		}
	}
	
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(TransliterationDictionary.class.getName());
	
	// token <-> phone mappings
	private Map<String, String> tokenMap;
	
	private TransliterationTokenizer tokenizer;
	
	private URL mapFile;
	
	/**
	 * Dictionary name
	 * Loaded from the dictionary file using the MetadataToken 'name'
	 * E.g.,
	 * 
	 * #name English
	 */
	private String name = null;
	
	/**
	 * Dictionary language
	 * Loaded from the dictionary file using the MetadataToken 'lang'
	 * followed by the appropriate 3-letter ISO-639-3 code.
	 * E.g.,
	 * 
	 * #lang eng
	 */
	private Language language = null;
	
	/**
	 * Other metadata values.  Common values are
	 * 'provider' and 'website'.  E.g.,
	 * 
	 * #provider University of Here
	 * #website http://www.uoh.org/
	 */
	private Map<String, String> metadata = new TreeMap<String, String>();
	
	/**
	 * Regex pattern for pre-processing text
	 */
	private Pattern preFindPattern;
	
	/**
	 * Replace expression used for each instance of preFindPattern found
	 * in the orthographic text
	 */
	private String preReplaceExpr;
	
	private List<Tuple<Pattern,String>> postFindList = new ArrayList<>();
	
	private List<Tuple<PhonexPattern, IPATranscript>> postPhonexFindList = new ArrayList<>();

	public TransliterationDictionary(URL mapFile) {
		super();
		this.mapFile = mapFile;
	}
	
	@Override
	public String getName() {
		if(name == null) {
			try {
				readMetadataFromStream(mapFile.openStream());
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		return name;
	}

	@Override
	public Language getLanguage() {
		if(language == null) {
			try {
				readMetadataFromStream(mapFile.openStream());
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		return language;
	}

	@Override
	public String[] lookup(String orthography) throws IPADictionaryExecption {
		
		if(preFindPattern != null && preReplaceExpr != null) {
			final Matcher m = preFindPattern.matcher(orthography);
			orthography = m.replaceAll(preReplaceExpr);
		}
		
		if(tokenizer == null) {
			tokenizer = new TransliterationTokenizer(getTokenMap());
		}
		String builderStr = tokenizer.transliterate(orthography);
		
		for(var postFind:postFindList) {
			var pattern = postFind.getObj1();
			var m = pattern.matcher(builderStr);
			builderStr = m.replaceAll(postFind.getObj2());
		}
		
		for(var postPhonexFind:postPhonexFindList) {
			try {
				final IPATranscript ipa = IPATranscript.parseIPATranscript(builderStr);
				
				var pattern = postPhonexFind.getObj1();
				var matcher = pattern.matcher(ipa);
				
				final IPATranscriptBuilder ipaBuilder = new IPATranscriptBuilder();
				while(matcher.find()) {
					matcher.appendReplacement(ipaBuilder, postPhonexFind.getObj2());
				}
				matcher.appendTail(ipaBuilder);
				builderStr = ipaBuilder.toIPATranscript().toString();
			} catch (ParseException e) {
				LOGGER.warn(e.getLocalizedMessage(), e);
			}
		}
		
		return new String[] { builderStr };
	}
	
	private Map<String, String> getTokenMap() {
		if(tokenMap == null) {
			try { 
				readTokenMap(mapFile.openStream());
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		return tokenMap;
	}
	
	/**
	 * (RegEx) Pattern used to read dictionary entries from file
	 */
	private Pattern getPattern() {
		String regex = "(.*)"
			+ "(\\p{Space}+)" + 
			"(.*)";
		return Pattern.compile(regex);
	}
	
	private void readTokenMap(InputStream is) 
		throws IOException {
		tokenMap = new LinkedHashMap<String, String>();
		
		final InputStreamReader in = 
			new InputStreamReader(is, "UTF-8");
		final BufferedReader reader = new BufferedReader(in);
		
		final Pattern dictPattern = getPattern();
		
		String line = null;
		while((line = reader.readLine()) != null) {
			if(line.startsWith("#")) {
				// ignore as a comment
				continue;
			}
			
			final Matcher m = dictPattern.matcher(line);
			if(m.matches()) {
				final String key = StringUtils.strip(m.group(1));
				final String ipa = StringUtils.strip(m.group(3));
				
				if(key.length() > 0 && ipa.length() > 0) {
					tokenMap.put(key, ipa);
				}
			}
		}
		reader.close();
	}
	
	@Override
	public String getMetadataValue(String key) {
		return metadata.get(key);
	}

	@Override
	public Iterator<String> metadataKeyIterator() {
		return metadata.keySet().iterator();
	}
	
	/**
	 * Read dictionary metadata from the given stream.
	 * Reading will end when the first non-commented
	 * line is encountered (i.e., the first transcription
	 * pair.)
	 * 
	 * @param is
	 * @throws IOException if an error occurs while
	 *  reading from the stream
	 */
	private void readMetadataFromStream(InputStream is) 
		throws IOException {
		InputStreamReader in =
			new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(in);
		
		String line = null;
		while((line = reader.readLine()) != null) {
			if(!line.startsWith("#")) break;
			
			String comment = line.substring(1);
			
			// check to see if the comment is a metadata instruction
			int firstTokenEnd = comment.indexOf(' ');
			String metaTkn = comment.substring(0, firstTokenEnd);
			
//			for(MetadataToken metaTkn:MetadataToken.values()) {
//				if(metaTkn.toString().equalsIgnoreCase(firstToken)) {
					// process metadata
					String metaDataInfo = comment.substring(firstTokenEnd+1);
					processMetadata(metaTkn, metaDataInfo);
//				}
//			}
		}
		reader.close();
	}
	
	private Tuple<Pattern, String> currentPostFindTuple = new Tuple<>();
	private Tuple<PhonexPattern, IPATranscript> currentPhonexFindTuple = new Tuple<>();
	/**
	 * Process metadata value
	 * 
	 * @param token the type of metadata to process
	 * @param value the value of the metadata
	 */
	private void processMetadata(String token, String value) {
		if(token.equalsIgnoreCase(MetadataToken.NAME.toString())) {
			// set name as value
			this.name = value;
		} else if(token.equalsIgnoreCase(MetadataToken.LANGUAGE.toString())) {
			// attempt to load language
			final Language lang = Language.parseLanguage(value);
			this.language = lang;
		} else if(token.equalsIgnoreCase(MetadataToken.PREPROCESSEXPR.toString())) {
			preFindPattern = Pattern.compile(value);
		} else if(token.equalsIgnoreCase(MetadataToken.PREPROCESSREPLACE.toString())) {
			preReplaceExpr = value;
		} else if(token.equalsIgnoreCase(MetadataToken.POSTPROCESSEXPR.toString())) {
			Pattern p = Pattern.compile(value);
			currentPostFindTuple.setObj1(p);
			if(currentPostFindTuple.getObj1() != null && currentPostFindTuple.getObj2() != null) {
				postFindList.add(currentPostFindTuple);
				currentPostFindTuple = new Tuple<>();
			}
		} else if(token.equalsIgnoreCase(MetadataToken.POSTPROCESSREPLACE.toString())) {
			currentPostFindTuple.setObj2(value);
			if(currentPostFindTuple.getObj1() != null && currentPostFindTuple.getObj2() != null) {
				postFindList.add(currentPostFindTuple);
				currentPostFindTuple = new Tuple<>();
			}
		} else if(token.equalsIgnoreCase(MetadataToken.PHONEXFIND.toString())) { 
			try {
				var p = PhonexPattern.compile(value);
				currentPhonexFindTuple.setObj1(p);
				
				if(currentPhonexFindTuple.getObj1() != null && currentPhonexFindTuple.getObj2() != null) {
					postPhonexFindList.add(currentPhonexFindTuple);
					currentPhonexFindTuple = new Tuple<>();
				}
			} catch (PhonexPatternException e) {
				LOGGER.error(e);
			}
		} else if(token.equalsIgnoreCase(MetadataToken.PHONEXREPLACE.toString())) {
			try {
				var r = IPATranscript.parseIPATranscript(value);
				currentPhonexFindTuple.setObj2(r);
				
				if(currentPhonexFindTuple.getObj1() != null && currentPhonexFindTuple.getObj2() != null) {
					postPhonexFindList.add(currentPhonexFindTuple);
					currentPhonexFindTuple = new Tuple<>();
				}
			} catch (ParseException e) {
				LOGGER.error(e);
			}
		} else {
			metadata.put(token, value);
		}
	}

	@Override
	public void install(IPADictionary dict) {
		dict.putExtension(LanguageInfo.class, this);
		dict.putExtension(NameInfo.class, this);
		dict.putExtension(Metadata.class, this);
	}
	
}
