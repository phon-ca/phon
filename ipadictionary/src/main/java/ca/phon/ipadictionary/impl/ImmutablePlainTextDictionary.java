package ca.phon.ipadictionary.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import ca.phon.ipadictionary.ContractionRule;
import ca.phon.ipadictionary.exceptions.BackingStoreException;
import ca.phon.ipadictionary.exceptions.IPADictionaryExecption;
import ca.phon.ipadictionary.spi.AddEntry;
import ca.phon.ipadictionary.spi.GenerateSuggestions;
import ca.phon.ipadictionary.spi.IPADictionarySPI;
import ca.phon.ipadictionary.spi.Metadata;
import ca.phon.ipadictionary.spi.OrthoKeyIterator;
import ca.phon.ipadictionary.spi.LanguageInfo;
import ca.phon.ipadictionary.spi.NameInfo;
import ca.phon.ipadictionary.spi.PrefixSearch;
import ca.phon.ipadictionary.spi.RemoveEntry;
import ca.phon.util.LanguageEntry;
import ca.phon.util.LanguageParser;
import ca.phon.util.radixtree.RadixTree;

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
	private RadixTree<List<String>> _db;
	
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
	private File dbFile;
	
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
	private LanguageEntry language = new LanguageEntry();
	
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
	public ImmutablePlainTextDictionary(File dbFile) {
		this.dbFile = dbFile;
		loadMetadata();
	}
	
	/**
	 * Load metadata from dictionary file
	 */
	private void loadMetadata() {
		try {
			readMetadataFromStream(new FileInputStream(dbFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Lazy-load the database.
	 */
	protected RadixTree<List<String>> lazyLoadDb()
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
	private RadixTree<List<String>> loadDictFromFile(File file)
		throws IOException {
		InputStream is = new FileInputStream(file);
		return readEntriesFromStream(is);
	}
	
	/**
	 * Get the file used by this dictionary.
	 * 
	 * @return file
	 */
	public File getFile() {
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
		if(token.equalsIgnoreCase(MetadataToken.NAME.toString())) {
			// set name as value
			this.name = value;
		} else if(token.equalsIgnoreCase(MetadataToken.LANGUAGE.toString())) {
			// attempt to load language
			LanguageEntry language = 
				LanguageParser.getInstance().getEntryById(value);
			if(language != null)
				this.language = language;
		} else if(token.equalsIgnoreCase(MetadataToken.CONTRACTION_RULE.toString())) {
			// TODO load contraction rules
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
	private RadixTree<List<String>> readEntriesFromStream(InputStream is) 
		throws IOException {
		InputStreamReader in = 
			new InputStreamReader(is, "UTF-8");
		BufferedReader reader = new BufferedReader(in);
		
		Pattern dictPattern = getPattern();
		
		RadixTree<List<String>> retVal = new RadixTree<List<String>>();
		
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
	public LanguageEntry getLanguage() {
		return language;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String[] lookup(String orthography) 
		throws IPADictionaryExecption {
		
		RadixTree<List<String>> db;
		try {
			db = lazyLoadDb();
		} catch (IOException e) {
			throw new BackingStoreException(e);
		}
		
		List<String> ipaEntries = 
			db.get(orthography.toLowerCase());
		if(ipaEntries != null) {
			return ipaEntries.toArray(new String[0]);
		} else {
			return new String[0];
		}
	}

	@Override
	public String[] generateSuggestions(String orthography) {
		// deal with contractions
		String regex = "(.*)'(.*)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(orthography);
		String[] retVal = new String[0];
		if(m.matches()) {
			String lhs = m.group(1);
			String rhs = m.group(2);
			
			// get entries for both sides
			String[] lhsEntries = new String[0];
			try {
				lookup(lhs);
			} catch (IPADictionaryExecption e) {
				
			}
			
			String[] rhsEntries = new String[0];
			try {
				lookup(rhs);
			} catch (IPADictionaryExecption e ) {
				
			}
			
			Set<String> transcriptions = new HashSet<String>();
			
			if(lhsEntries.length > 0 && rhsEntries.length > 0) {
				for(String lhsEntry:lhsEntries) {
					for(String rhsEntry:rhsEntries) {
						for(ContractionRule ctrRule:ctrRules) {
							if(ctrRule.matches(lhs, rhs, lhsEntry, rhsEntry)) {
								String tr = ctrRule.buildTranscript(lhs, rhs, lhsEntry, rhsEntry);
								transcriptions.add(tr);
							}
						}
					}                   
				}
			}
			retVal = transcriptions.toArray(new String[0]);
		}
		return retVal;
	}

	@Override
	public Iterator<String> iterator() {
		RadixTree<List<String>> db;
		try {
			db = lazyLoadDb();
		} catch (IOException e) {
			return null;
		}
		
		return db.keySet().iterator();
	}

	@Override
	public String[] keysWithPrefix(String prefix) {
		RadixTree<List<String>> db;
		try {
			db = lazyLoadDb();
		} catch (IOException e) {
			return new String[0];
		}
		
		return db.getKeysWithPrefix(prefix).toArray(new String[0]);
	}

	@Override
	public String getMetadataValue(String key) {
		return metadata.get(key);
	}

	@Override
	public Iterator<String> metadataKeyIterator() {
		return metadata.keySet().iterator();
	}
}
