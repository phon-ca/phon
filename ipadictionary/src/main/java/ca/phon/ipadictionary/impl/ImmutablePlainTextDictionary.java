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
package ca.phon.ipadictionary.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import ca.hedlund.tst.TernaryTree;
import ca.phon.ipadictionary.ContractionRule;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.exceptions.BackingStoreException;
import ca.phon.ipadictionary.exceptions.IPADictionaryExecption;
import ca.phon.ipadictionary.spi.GenerateSuggestions;
import ca.phon.ipadictionary.spi.IPADictionarySPI;
import ca.phon.ipadictionary.spi.LanguageInfo;
import ca.phon.ipadictionary.spi.Metadata;
import ca.phon.ipadictionary.spi.NameInfo;
import ca.phon.ipadictionary.spi.OrthoKeyIterator;
import ca.phon.ipadictionary.spi.PrefixSearch;
import ca.phon.util.Language;
import ca.phon.util.Tuple;

/**
 * Implements the basic dictionary format used by Phon.
 * The input file should bef a UTF-8 stream of
 * characters with a single orthography and ipa transcription
 * per line.  The orthography and transcript can be
 * separated using a specified token (default '\p{Space}') -
 * regular expressions are allowed.
 * 
 * This dictionary is immutable.  For dictionaries which 
 * allow changes, see {@link MutablePlainTextDictionary}.
 * 
 */
public class ImmutablePlainTextDictionary implements IPADictionarySPI,
	LanguageInfo, NameInfo, GenerateSuggestions, OrthoKeyIterator, PrefixSearch, Metadata {
	
	/*
	 * token descriptions for metadata/processing instructions
	 */
	private enum MetadataToken {
		NAME("name"),
		LANGUAGE("lang"),
		CONTRACTION_RULE("ctr"),
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

	/**
	 * default separator regex
	 */
	private static final String DEFAULT_SEPARATOR = "\\p{Space}";
	
	/**
	 * Separator
	 */
	private String separator = DEFAULT_SEPARATOR;
	
	/**
	 * Database
	 */
	private TernaryTree<List<String>> _db;
	
	/**
	 * Contraction rules.  These rules are loaded from a file called 
	 * <lang>.ctr in the same directory as the dictionary file.  They
	 * can also be set manually.
	 */
	private List<ContractionRule> ctrRules = new ArrayList<ContractionRule>();
	
	/**
	 * Location of the database on disk.  The dictionary will
	 * attempt to keep this file up-to-date as entries are added/removed.
	 */
	private URL dbFile;
	
	/**
	 * Dictionary name
	 * Loaded from the dictionary file using the MetadataToken 'name'
	 * E.g.,
	 * 
	 * #name English
	 */
	private String name = "";
	
	/**
	 * Dictionary language
	 * Loaded from the dictionary file using the MetadataToken 'lang'
	 * followed by the appropriate 3-letter ISO-639-3 code.
	 * E.g.,
	 * 
	 * #lang eng
	 */
	private Language language = new Language();
	
	/**
	 * Other metadata values.  Common values are
	 * 'provider' and 'website'.  E.g.,
	 * 
	 * #provider University of Here
	 * #website http://www.uoh.org/
	 */
	private Map<String, String> metadata = new TreeMap<String, String>();
	
	/**
	 * Default constructor.
	 * 
	 * @param file
	 */
	public ImmutablePlainTextDictionary(URL dbFile) {
		this.dbFile = dbFile;
		loadMetadata();
	}
	
	/**
	 * Load metadata from dictionary file
	 */
	private void loadMetadata() {
		try {
			readMetadataFromStream(dbFile.openStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Lazy-load the database.
	 */
	protected TernaryTree<List<String>> lazyLoadDb()
		throws IOException {
		if(_db == null) {
			_db = loadDictFromFile(dbFile);
		}
		return _db;
	}
	
	/**
	 * Read dictionary entries from the given file.
	 * File should be UTF-8 and formatted as
	 * indicated above.
	 * 
	 * @param file
	 * @returns a new radix tree acting as our database
	 * @throws IOException if an error occurs while
	 *  attempting to read the file contents
	 */
	private TernaryTree<List<String>> loadDictFromFile(URL file)
		throws IOException {
		InputStream is = file.openStream();
		return readEntriesFromStream(is);
	}
	
	/**
	 * Get the file used by this dictionary.
	 * 
	 * @return file
	 */
	public URL getFile() {
		return this.dbFile;
	}
	
	/**
	 * Set the contraction rules used by this dictionary.
	 * 
	 * @param ctrRuleList a list of {@link ContractionRule}
	 * 
	 */
	public void setContractionRules(List<ContractionRule> ctrRuleList) {
		ctrRules = ctrRuleList;
	}
	
	/**
	 * Return the {@link ContractionRule} used by this dictionary
	 * for generating suggested transcriptions.
	 * 
	 * @return the list of {@link ContractionRule}
	 */
	public List<ContractionRule> getContractionRules() {
		return this.ctrRules;
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
	
	/**
	 * Process metadata value
	 * 
	 * @param token the type of metadata to process
	 * @param value the value of the metadata
	 */
	private void processMetadata(String token, String value) {
		token = token.trim();
		value = value.trim();
		if(token.equalsIgnoreCase(MetadataToken.NAME.toString())) {
			// set name as value
			this.name = value;
		} else if(token.equalsIgnoreCase(MetadataToken.LANGUAGE.toString())) {
			// attempt to load language
			final Language lang = Language.parseLanguage(value);
			this.language = lang;
		} else if(token.equalsIgnoreCase(MetadataToken.CONTRACTION_RULE.toString())) {
			final ContractionRule cr = ContractionRule.parseContractionRule(value);
			ctrRules.add(cr);
		} else {
			metadata.put(token, value);
		}
	}
	
	/**
	 * Read dictionary entries from the given stream.
	 * Stream contents should be UTF-8 and formatted as
	 * indicated above.
	 * 
	 * @param is
	 * @returns a new radix tree acting as our database
	 * @throws IOException if an error occurs while
	 *  attempting to read the stream contents
	 */
	private TernaryTree<List<String>> readEntriesFromStream(InputStream is) 
		throws IOException {
		InputStreamReader in = 
			new InputStreamReader(is, "UTF-8");
		BufferedReader reader = new BufferedReader(in);
		
		Pattern dictPattern = getPattern();
		
		TernaryTree<List<String>> retVal = new TernaryTree<List<String>>();
		
		String line = null;
		while((line = reader.readLine()) != null) {
			if(line.startsWith("#")) {
				// ignore as a comment
				continue;
			}
			
			Matcher m = dictPattern.matcher(line);
			if(m.matches()) {
				String orthography = StringUtils.strip(m.group(1)).toLowerCase();
				String ipa = StringUtils.strip(m.group(3)).toLowerCase();
				
				if(orthography.length() > 0 && ipa.length() > 0) {
					List<String> ipaEntries = 
						retVal.get(orthography);
					if(ipaEntries == null) {
						ipaEntries = new ArrayList<String>();
						retVal.put(orthography, ipaEntries);
					}
					if(!ipaEntries.contains(ipa)) {
						ipaEntries.add(ipa);
					}
				}
			}
		}
		reader.close();
		
		return retVal;
	}
	
	/**
	 * (RegEx) Pattern used to read dicationary entries from file
	 */
	private Pattern getPattern() {
		String regex = "(.*)"
			+ "(" + separator + ")" + 
			"(.*)";
		return Pattern.compile(regex);
	}

	@Override
	public Language getLanguage() {
		return language;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String[] lookup(String orthography) 
		throws IPADictionaryExecption {
		orthography = StringUtils.strip(orthography, "?!\"'.\\/@&$()^%#*");
		TernaryTree<List<String>> db;
		try {
			db = lazyLoadDb();
		} catch (IOException e) {
			throw new BackingStoreException(e);
		}
		
		List<String> ipaEntries = 
			db.get(orthography.toLowerCase());
		if(ipaEntries != null && ipaEntries.size() > 0) {
			return ipaEntries.toArray(new String[0]);
		} else {
			return new String[0];
		}
	}

	@Override
	public String[] generateSuggestions(String orthography) {
		// deal with contractions
		String regex = "(.+)'(.+)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(orthography);
		String[] retVal = new String[0];
		if(m.matches()) {
			String lhs = m.group(1);
			String rhs = m.group(2);
			
			// get entries for both sides
			String[] lhsEntries = new String[0];
			try {
				lhsEntries = lookup(lhs);
			} catch (IPADictionaryExecption e) {
				
			}
			
			String[] rhsEntries = new String[0];
			try {
				rhsEntries = lookup(rhs);
			} catch (IPADictionaryExecption e ) {
				
			}
			
			Set<String> transcriptions = new HashSet<String>();
			
			final List<Tuple<String, String>> ipaPairs = new ArrayList<Tuple<String,String>>();
			for(String lhsEntry:lhsEntries) {
				if(rhsEntries.length == 0) {
					final Tuple<String, String> ipaPair = new Tuple<String, String>(lhsEntry, new String());
					ipaPairs.add(ipaPair);
				} else {
					for(String rhsEntry:rhsEntries) {
						final Tuple<String, String> ipaPair = new Tuple<String, String>(lhsEntry, rhsEntry);
						ipaPairs.add(ipaPair);
					}
				}
			}
			
			for(Tuple<String, String> ipaPair:ipaPairs) {
				final String lhsEntry = ipaPair.getObj1();
				final String rhsEntry = ipaPair.getObj2();
				for(ContractionRule ctrRule:ctrRules) {
					if(ctrRule.matches(lhs, rhs, lhsEntry, rhsEntry)) {
						String tr = ctrRule.buildTranscript(lhs, rhs, lhsEntry, rhsEntry);
						transcriptions.add(tr);
					}
				}
			}
			retVal = transcriptions.toArray(new String[0]);
		}
		return retVal;
	}

	@Override
	public Iterator<String> iterator() {
		TernaryTree<List<String>> db;
		try {
			db = lazyLoadDb();
		} catch (IOException e) {
			return null;
		}
		
		return db.keySet().iterator();
	}

	@Override
	public String[] keysWithPrefix(String prefix) {
		TernaryTree<List<String>> db;
		try {
			db = lazyLoadDb();
		} catch (IOException e) {
			return new String[0];
		}
		
		return db.keysWithPrefix(prefix).toArray(new String[0]);
	}

	@Override
	public String getMetadataValue(String key) {
		return metadata.get(key);
	}

	@Override
	public Iterator<String> metadataKeyIterator() {
		return metadata.keySet().iterator();
	}

	@Override
	public void install(IPADictionary dict) {
		dict.putExtension(LanguageInfo.class, this);
		dict.putExtension(NameInfo.class, this);
		dict.putExtension(GenerateSuggestions.class, this);
		dict.putExtension(OrthoKeyIterator.class, this);
		dict.putExtension(PrefixSearch.class, this);
		dict.putExtension(Metadata.class, this);
	}
}
