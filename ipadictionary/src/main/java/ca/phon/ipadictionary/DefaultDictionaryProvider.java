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
package ca.phon.ipadictionary;

import java.io.*;
import java.net.*;
import java.util.*;

import ca.phon.ipadictionary.impl.*;
import ca.phon.plugin.*;
import ca.phon.util.*;
import ca.phon.util.resources.*;

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
