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
package ca.phon.ipadictionary;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import ca.phon.ipadictionary.impl.*;
import ca.phon.plugin.PluginManager;
import ca.phon.util.Language;
import ca.phon.util.resources.ClassLoaderHandler;

public class DefaultDictionaryProvider extends ClassLoaderHandler<IPADictionary> 
	implements DictionaryProvider {

	private final static String DICT_LIST = "dict/dicts.list";
	
	private Set<Language> dbDicts = null;
	
	public DefaultDictionaryProvider() {
		super(PluginManager.getInstance());
		loadResourceFile(DICT_LIST);
	}
	
	@Override
	public IPADictionary loadFromURL(URL url) throws IOException {
		final IPADictionary immutableDict = 
				new IPADictionary(new ImmutablePlainTextDictionary(url));
		final IPADictionary databaseDict =
				new IPADictionary(new DatabaseDictionary(immutableDict.getLanguage()));
		
		dbDicts.remove(immutableDict.getLanguage());
		
		final CompoundDictionary compoundDict =
				new CompoundDictionary(new IPADictionary[]{ databaseDict, immutableDict });
		return new IPADictionary(compoundDict);
	}

	@Override
	public Iterator<IPADictionary> iterator() {
		dbDicts = IPADatabaseManager.getInstance().getAvailableLanguages();
		return new CustomIterator(super.iterator());
	}
	
	private class CustomIterator implements Iterator<IPADictionary> {
		
		private Iterator<IPADictionary> itr;
		
		private Iterator<Language> langItr = null;
		
		public CustomIterator(Iterator<IPADictionary> itr) {
			this.itr = itr;
		}

		@Override
		public boolean hasNext() {
			return (itr.hasNext() ? true : (langItr != null ? langItr.hasNext() : false));
		}

		@Override
		public IPADictionary next() {
			IPADictionary retVal = null;
			
			if(langItr != null) {
				Language lang = langItr.next();
				retVal = new IPADictionary(new DatabaseDictionary(lang));
				if(!langItr.hasNext())
					langItr = null;
			} else {
				retVal = itr.next();
				if(!itr.hasNext()) {
					langItr = dbDicts.iterator();
				}
			}
			
			return retVal;
		}

		@Override
		public void remove() {
		}
		
	}
	
}
