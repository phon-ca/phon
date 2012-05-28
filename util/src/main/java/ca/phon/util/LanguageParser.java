/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;


/**
 * 
 */
public class LanguageParser implements Iterable<LanguageEntry> {
	private final List<LanguageEntry> entries = new ArrayList<LanguageEntry>();;

	private final static String _dataFile = "ISO-639-3_utf-8.txt";
	
	private LanguageParser() {
		InputStream is = getClass().getResourceAsStream(_dataFile);
		parseLanguageFile(is);
	}
	
	/**
	 * Constructor. Prases the specified file.
	 * @param fileName  the path to the file to parse
	 */
	private LanguageParser(String fileName) {
		this(new File(fileName));
	}
	
	/**
	 * Constructor. Prases the specified file.
	 * @param file  the path to the file to parse
	 */
	private LanguageParser(File file) {
		try {
			parseLanguageFile(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses a language file and places its contents in <code>entries</code>.
	 */
	private void parseLanguageFile(InputStream is) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			// The first two lines are simply header lines; discard
//			br.readLine(); // date
//			br.readLine(); // %%
//			
			String line = br.readLine();
			while((line = br.readLine()) != null) {
				LanguageEntry currentEntry = LanguageEntry.fromString(line);
				entries.add(currentEntry);
				
//				do {
//					int sepLoc = line.indexOf(':');
//					if(sepLoc == -1 || line.trim().equals("%%"))
//						break;
//					
//					String name = line.substring(0, sepLoc);
//					String value = line.substring(sepLoc + 1);
//					currentEntry.addProperty(name.trim(), value.trim());
//				} while((line = br.readLine()) != null);
//					
//				entries.add(currentEntry);
			}
		} catch(Exception e) {
			Logger.getLogger(getClass().getName()).severe(e.getMessage());
			return;
		}
	}
	
	/**
	 * Get a collection of languages.
	 * @return  a {@link Collection} of languages known to the parser
	 */
	public List<LanguageEntry> getLanguages() {
		return entries;
	}
	
	/**
	 * Get a {@link LanguageEntry} object based on subtag.
	 * @param subtag  the subtag to check for
	 * @return        a {@link LanguageEntry} object
	 */
	public LanguageEntry getEntryById(String subtag) {
		return getEntryByProperty(LanguageEntry.ID_639_3, subtag);
	}
	
	/**
	 * Get a {@link LanguageEntry} object based on language name.
	 * @param language  the language to check for
	 * @return          a {@link LanguageEntry} object
	 */
	public LanguageEntry getEntryByLanguage(String language) {
		return getEntryByProperty(LanguageEntry.REF_NAME, language);
	}
	
	/**
	 * Fetches {@link LanguageEntry} objects that have a specified property
	 * and property value.
	 * @param prop   the name of the property to check for
	 * @param value  the value of the property to check for
	 * @return       a {@link Collection} of {@link LanguageEntry} objects
	 */
	public Collection<LanguageEntry> getEntriesByProperty(String prop, String value) {
		Vector<LanguageEntry> props = new Vector<LanguageEntry>();
		for(LanguageEntry entry : entries) {
			String propValue = entry.getProperty(prop);
			if(propValue != null && value.equals(propValue))
				props.add(entry);
		}
		return props;
	}
	
	/**
	 * Fetches a single {@link LanguageEntry} object that has the specified
	 * property value.
	 * @param prop   the name of the property to check for
	 * @param value  the value of the property to check for
	 * @return       a {@link LanguageEntry} object, or null if nothing found
	 */
	public LanguageEntry getEntryByProperty(String prop, String value) {
		for(LanguageEntry entry : entries) {
			String propValue = entry.getProperty(prop);
			if(propValue != null && value.equals(propValue))
				return entry;
		}
		return null;
	}
	
	@Override
	public Iterator<LanguageEntry> iterator() {
		return entries.iterator();
	}
	
	
	/*
	 * Singleton pattern
	 */
	private static LanguageParser singleton = null;
	
	public static LanguageParser getInstance() {
		if(singleton == null)
			singleton = new LanguageParser();
		return singleton;
	}
}
