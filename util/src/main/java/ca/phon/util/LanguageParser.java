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
package ca.phon.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 */
public class LanguageParser implements Iterable<LanguageEntry> {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(LanguageParser.class.getName());

	private final List<LanguageEntry> entries = new ArrayList<LanguageEntry>();;

	private final static String _dataFile = "ISO-639-3_utf-8.txt";

	private LanguageParser() throws IOException {
		this(LanguageParser.class.getClassLoader().getResourceAsStream(_dataFile), "UTF-8");
	}

	/**
	 * Constructor. Prases the specified file.
	 * @param file  the path to the file to parse
	 */
	private LanguageParser(File file, String charset) throws IOException {
		this(new FileInputStream(file), charset);
	}

	private LanguageParser(InputStream is, String charset) throws IOException {
		if(is == null)
			throw new IllegalArgumentException("input stream is null");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
		parseLanguageFile(reader);
	}

	/**
	 * Parses a language file and places its contents in <code>entries</code>.
	 */
	private void parseLanguageFile(BufferedReader br) throws IOException {
		try {
			String line = br.readLine();
			while((line = br.readLine()) != null) {
				LanguageEntry currentEntry = entryFromLine(line);
				entries.add(currentEntry);
			}
		} catch(Exception e) {
			org.apache.logging.log4j.LogManager.getLogger(getClass().getName()).error(e.getMessage());
		} finally {
			br.close();
		}
	}

	/**
	 * Parse the language entry from the given line in the
	 * ISO file.
	 *
	 */
	public static LanguageEntry entryFromLine(String line) {
		if(line == null) return null;

		String[] fields = line.split("\t");

		LanguageEntry retVal = new LanguageEntry();
		retVal.addProperty(LanguageEntry.ID_639_3, fields[0]);
		retVal.addProperty(LanguageEntry.ID_639_2B, fields[1]);
		retVal.addProperty(LanguageEntry.ID_639_2T, fields[2]);
		retVal.addProperty(LanguageEntry.ID_639_1, fields[3]);
		retVal.addProperty(LanguageEntry.SCOPE, fields[4]);
		retVal.addProperty(LanguageEntry.TYPE, fields[5]);
		retVal.addProperty(LanguageEntry.REF_NAME, fields[6]);

		return retVal;
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
		if(singleton == null) {
			try {
				singleton = new LanguageParser();
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		return singleton;
	}
}
