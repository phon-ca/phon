package ca.phon.ipadictionary.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.phon.ipadictionary.exceptions.BackingStoreException;
import ca.phon.ipadictionary.exceptions.DuplicateEntry;
import ca.phon.ipadictionary.exceptions.IPADictionaryExecption;
import ca.phon.ipadictionary.spi.AddEntry;
import ca.phon.ipadictionary.spi.GenerateSuggestions;
import ca.phon.ipadictionary.spi.IPADictionarySPI;
import ca.phon.ipadictionary.spi.RemoveEntry;
import ca.phon.util.LanguageEntry;
import ca.phon.util.StringUtils;
import ca.phon.util.radixtree.RadixTree;

/**
 * Implements the basic dictionary format used by Phon.
 * The input file should bef a UTF-8 stream of
 * characters with a single orthography and ipa transcription
 * per line.  The orthography and transcript can be
 * separated using a specified token (default '\p{Space}') -
 * regular expressions are allowed.
 * 
 * This dictionary allows changes.  For dictionaries which do not
 * allow changes, see {@link ImmutablePlainTextDictionary}.
 * 
 */
public class MutablePlainTextDictionary extends ImmutablePlainTextDictionary
	implements AddEntry, RemoveEntry {

	/**
	 * Programmatic control of edit actions 
	 */
	private boolean editable;
	
	/**
	 * Modified flag
	 */
	private boolean modified = false;
	
	public MutablePlainTextDictionary(File dbFile) {
		super(dbFile);
	}

	/**
	 * Is the db editable
	 * 
	 * @return <code>true</code> if this.editable is <code>true</code> and
	 *  the file is writable.  <code>false</code> othersie
	 */
	public boolean isEditable() {
		return getFile().canWrite() && this.editable;
	}
	
	/**
	 * Set the database edit-ability flag
	 * 
	 * @param editable
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	@Override
	public synchronized void removeEntry(String orthography, String ipa)
			throws IPADictionaryExecption {
		RadixTree<List<String>> db;
		try {
			db = lazyLoadDb();
		} catch (IOException e) {
			throw new BackingStoreException(e);
		}
		
		List<String> ipaEntries = db.get(orthography);
		if(ipaEntries != null) {
			if(ipaEntries.contains(ipa)) {
				ipaEntries.remove(ipa);
				setModified(true);
			}
		}
	}

	@Override
	public synchronized void addEntry(String orthography, String ipa)
			throws IPADictionaryExecption {
		RadixTree<List<String>> db;
		try {
			db = lazyLoadDb();
		} catch (IOException e) {
			throw new BackingStoreException(e);
		}
		
		// try to add entry to database tree
		List<String> ipaEntries = db.get(orthography);
		if(ipaEntries == null) {
			ipaEntries = new ArrayList<String>();
			db.put(orthography, ipaEntries);
		}
		if(ipaEntries.contains(ipa)) {
			throw new DuplicateEntry();
		}
		ipaEntries.add(ipa);
		setModified(true);
	}
	
	/**
	 * Has the dictionary changed?
	 * 
	 * @return <code>true</code> if an entry has been added/removed,
	 *  false otherwise
	 */
	public boolean isModified() {
		return this.modified;
	}
	
	/**
	 * Set the modified flag for the dictionary
	 * 
	 * @param modified
	 */
	public void setModified(boolean modified) {
		this.modified = modified;
	}
}
