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
package ca.phon.ipadictionary.ui;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;

import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.IPADictionaryLibrary;
import ca.phon.ipadictionary.exceptions.IPADictionaryExecption;
import ca.phon.ipadictionary.impl.CompoundDictionary;
import ca.phon.ipadictionary.impl.IPADatabaseManager;
import ca.phon.ipadictionary.spi.AddEntry;
import ca.phon.ipadictionary.spi.ClearEntries;
import ca.phon.ipadictionary.spi.RemoveEntry;
import ca.phon.util.Language;

/**
 * Context object for cmd interpreter.
 * 
 * Includes implementation of expressions.
 */
public class IPALookupContext {
	
	private final static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(IPALookupContext.class.getName());

	private enum Commands {
		ADD,
		CREATE,
		DROP,
		IMPORT,
		EXPORT,
		LOOKUP,
		USE,
		LIST,
		REMOVE,
		HELP;

		private String[] cmds = {
			"add",
			"create",
			"drop",
			"import",
			"export",
			"lookup",
			"use",
			"list",
			"remove",
			"help"
		};

		public String getCmdString() {
			return cmds[ordinal()];
		}

		public String[] descs = {
			"insert a new entry into the dictionary",
			"create a new IPA dictionary",
			"drop a user defined IPA dictionary",
			"import dictionary entries from a file",
			"export user defined dictionary entries to a file",
			"lookup IPA for given orthography",
			"switch to another IPA dictionary",
			"lists all available IPA dictionaries",
			"remove an IPA entry from the dictionary",
			"displays this help or help for a command"
		};

		public String getDesc() {
			return descs[ordinal()];
		}

		public String[] usages = {
			"add \"<orthography>\"=\"<ipa>\"",
			"create <language> <name> ",
			"drop ",
			"import \"<file>\"",
			"export \"<file>\"",
			"lookup \"<orthograpny>\"",
			"use <lang>",
			"list",
			"remove \"<orthography>\"=\"<ipa>\"",
			"help <command>?"
		};

		public String getUsage() {
			return usages[ordinal()];
		}

		public String[] examples = {
			"add \"hello\"=\"helo\"",
			"create eng-test \"English Test Dictionary\"",
			"drop ",
			"import \"/Users/me/Desktop/myipa.txt\"",
			"export \"/Users/me/Desktop/ipa.txt\"",
			"lookup \"hello\"",
			"use fra",
			"",
			"remove \"hello\"=\"helo\"",
			"help lookup"
		};

		public String getExample() {
			return examples[ordinal()];
		}

		public String[] notes = {
			"",
			"",
			"",
			"The text file must be UTF-8 with one entry per line formatted as follows:\n"
					+ "\t<orthography><tab><ipa>",
			"",
			"This is the default action.  You may enter just the orthography to perform a lookup.",
			"",
			"",
			"Only user-entered IPA transcripts may be removed.",
			""
		};

		public String getNode() {
			return notes[ordinal()];
		}

		public static Commands getCmdFromString(String txt) {
			Commands retVal = null;

			for(Commands c:values()) {
				if(c.getCmdString().equals(txt)) {
					retVal = c;
					break;
				}
			}

			return retVal;
		}
	}
	
	/**
	 * The current dictionary.
	 */
	private IPADictionary dictionary = IPADictionaryLibrary.getInstance().defaultDictionary();
	
	/**
	 * Listeners
	 */
	private List<IPALookupContextListener> listeners =
		new ArrayList<IPALookupContextListener>();

	/**
	 * Constructor
	 */
	public IPALookupContext() {
		super();
	}
	
	public IPADictionary getDictionary() {
		return dictionary;
	}

	/**
	 * Add a listener
	 */
	public void addLookupContextListener(IPALookupContextListener listener) {
		if(listener != null && !listeners.contains(listener))
			listeners.add(listener);
	}
	
	public void removeLookupContextListener(IPALookupContextListener listener) {
		if(listener != null && listeners.contains(listener))
			listeners.remove(listener);
	}
	
	public void fireDictionaryChanged(String dict) {
		IPALookupContextListener contextListeners[] =
			listeners.toArray(new IPALookupContextListener[0]);
		for(IPALookupContextListener contextListener:contextListeners) {
			contextListener.dictionaryChanged(dict);
		}
	}
	
	public void fireDictionaryAdded(String dict) {
		IPALookupContextListener contextListeners[] =
			listeners.toArray(new IPALookupContextListener[0]);
		for(IPALookupContextListener contextListener:contextListeners) {
			contextListener.dictionaryAdded(dict);
		}
	}
	
	public void fireDictionaryRemoved(String dict) {
		IPALookupContextListener contextListeners[] =
			listeners.toArray(new IPALookupContextListener[0]);
		for(IPALookupContextListener contextListener:contextListeners) {
			contextListener.dictionaryRemoved(dict);
		}
	}

	public void fireMessage(String msg) {
		IPALookupContextListener contextListeners[] =
			listeners.toArray(new IPALookupContextListener[0]);
		for(IPALookupContextListener contextListener:contextListeners) {
			contextListener.handleMessage(msg);
		}
	}

	public void fireError(String err) {
		IPALookupContextListener contextListeners[] =
			listeners.toArray(new IPALookupContextListener[0]);
		for(IPALookupContextListener contextListener:contextListeners) {
			contextListener.errorOccured(err);
		}
	}
	
	/**
	 * Switch dictionary.
	 *  
	 */
	public void switchDictionary(String lang) {
		final IPADictionaryLibrary library = IPADictionaryLibrary.getInstance();
		final List<IPADictionary> dicts = library.dictionariesForLanguage(lang);
		
		final IPADictionary newDict = 
				(dicts.size() == 0 ? null :
					(dicts.size() > 1
						?	new IPADictionary(new CompoundDictionary(dicts.toArray(new IPADictionary[0])))
						:	dicts.get(0))
				);
		
		if(newDict != null) {
			dictionary = newDict;
			String msg = "Using dictionary '" + lang + "'";
			fireMessage(msg);
			fireDictionaryChanged(lang);
		} else {
			String err = "Failed to load dictionary '" + lang + "'";
			fireError(err);
		}
	}
	
	/**
	 * List dicitonaries.
	 * 
	 * 
	 */
	public void list() {
		final Set<Language> dictLangs = IPADictionaryLibrary.getInstance().availableLanguages();
		
		String msg = "Found " + dictLangs.size()  + " dictionaries:";

		for(Language dict:dictLangs) {
			msg += "\n\t" + dict;
		}
		
		fireMessage(msg);
	}
	
	/**
	 * Add transcript
	 */
	public void addTranscript(String ortho, String ipa) {
		final AddEntry addEntry = dictionary.getExtension(AddEntry.class);
		if(addEntry == null) {
			final String msg = "Dictionary does not support the 'add' command";
			fireError(msg);
			return;
		}
		try {
			addEntry.addEntry(ortho, ipa);
		} catch (IPADictionaryExecption e) {
			LOGGER.error( e.getLocalizedMessage(), e);
			fireError(e.getLocalizedMessage());
		}
	}

	/**
	 * Remove transcript
	 */
	public void removeTranscript(String ortho, String ipa) {
		final RemoveEntry removeEntry = dictionary.getExtension(RemoveEntry.class);
		if(removeEntry == null) {
			final String msg = "Dictionary does not support he 'remove' command";
			fireError(msg);
			return;
		}
		try {
			removeEntry.removeEntry(ortho, ipa);
		} catch (IPADictionaryExecption e) {
			LOGGER.error( e.getLocalizedMessage(), e);
			fireError(e.getLocalizedMessage());
		}
	}

	/**
	 * Remove all transcripts
	 */
	public void removeAllTranscripts() {
		final ClearEntries ce = dictionary.getExtension(ClearEntries.class);
		if(ce != null) {
			try {
				ce.clear();
			} catch (IPADictionaryExecption e) {
				LOGGER.error( e.getLocalizedMessage(), e);
				fireError("Unable to remove user-defined entries: " + e.getLocalizedMessage());
			}
			fireMessage("Cleared user-defined entries from dictionary " + dictionary.getLanguage().toString());
		} else {
			fireError("Dictionary does not support the 'clear' operation");
		}
	}
	
	/**
	 * Lookup
	 */
	public void lookup(String ortho) {
		final String[] entries = dictionary.lookup(ortho);
		
		String msg = "Found " +
				entries.length + 
				(entries.length  == 1 ? " entry" : " entries") +
				" for `" + ortho + "`";

		fireMessage(msg);

		for(String entry:entries) {
			fireMessage(entry);
		}
		
	}
	
	/**
	 * Import
	 */
	public void importData(String file) {
		String msg = ("Import data from file `" + file + "`");
		fireMessage(msg);

		Pattern pattern = Pattern.compile("(.*)\\p{Space}+(.*)");
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line = null;
			int lineIndex = 1;
			int numAdditions = 0;
			while((line = in.readLine()) != null) {
				Matcher m = pattern.matcher(line);
				if(m.matches()) {
					String ortho = m.group(1);
					String ipa = m.group(2);
					
					if(ortho.length() > 0 && ipa.length() > 0) {
						addTranscript(ortho, ipa);
						numAdditions++;
					}
				} else {
					String err = "Line " + lineIndex + " invalid. Skipping.";
					fireError(err);
				}
				lineIndex++;
			}
			in.close();
			msg = "Added " + numAdditions + " transcripts.";
			fireMessage(msg);

		} catch (IOException e) {
			fireError(e.toString());
		}
	}
	
	/**
	 * Export
	 */
	public void exportData(String file) {
		try {
			IPADatabaseManager.getInstance().saveDataToFile(file, dictionary.getLanguage().toString());
			fireMessage("User dictionary entries saved to file " + file);
		} catch (IOException e) {
			fireError(e.getLocalizedMessage());
		}
	}
	
	/**
	 * Create
	 */
	public void createDictionary(String lang, String name) {
		try {
			if(!IPADatabaseManager.getInstance().createDictionary(lang, name)) {
				fireError("Dictionary not created");
			} else {
				fireDictionaryAdded(lang);
				fireMessage("Added dictionary " + lang);
			}
		} catch (IllegalArgumentException e) {
			fireError(e.getMessage());
		}
	}
	
	/**
	 * Drop
	 * Only user-defined dictionaries can be dropped.
	 */
	public void dropDictionary(String lang) {
		if(!IPADatabaseManager.getInstance().dropDictionary(lang)) {
			fireError("Dictionary not dropped");
		} else {
			fireDictionaryRemoved(lang);
			fireMessage("Removed dictionary " + lang);
		}
	}

	/**
	 * Print help
	 */
	public void printHelp(String cmd) {
		if(cmd == null) {
			String msg = "The following commands are available:";
			fireMessage(msg);

			for(Commands c:Commands.values()) {
				msg = c.getCmdString() + "\t" + c.getDesc();
				fireMessage("\t" + msg);
			}
		} else {
			Commands c = Commands.getCmdFromString(cmd);
			if(c != null) {
				String msg =
						c.getCmdString() + " : " + c.getDesc();
				fireMessage(msg);
				msg =
						"\tUsage: " + c.getUsage();
				fireMessage(msg);
				if(c.getExample().length() > 0) {
					msg =
							"\tE.g., " + c.getExample();
					fireMessage(msg);
				}

				if(c.getNode().length() > 0) {
					msg =
							"\tNote: " + c.getNode();
					fireMessage(msg);
				}
				
			}
		}
	}
}
