/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.syllabifier;

import java.io.*;
import java.util.*;

import org.apache.commons.io.*;

import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.syllabifier.*;
import ca.phon.syllabifier.basic.*;
import ca.phon.syllabifier.opgraph.*;
import ca.phon.syllabifier.opgraph.extensions.*;
import ca.phon.util.*;

public class UserSyllabifierProvider implements SyllabifierProvider {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(UserSyllabifierProvider.class.getName());
	
	public final static String SYLLABIFIER_FOLDER_PROP = UserSyllabifierProvider.class.getName() + ".folder";
	
	public final static String DEFAULT_FOLDER = PrefHelper.getUserDataFolder() + File.separator + "syllabifier";

	private String folder = PrefHelper.getUserPreferences().get(SYLLABIFIER_FOLDER_PROP, DEFAULT_FOLDER);
	
	public UserSyllabifierProvider() {
		super();
	}

	@Override
	public Iterator<Syllabifier> iterator() {
		final File syllabifierFolder = new File(folder);
		if(!syllabifierFolder.exists()) {
			syllabifierFolder.mkdirs();
		}
		@SuppressWarnings("unchecked")
		final Iterator<File> fileIterator = 
				FileUtils.iterateFiles(syllabifierFolder, new String[] {"xml", "opgraph"}, true);
		return new UserSyllabifierIterator(fileIterator);
	}
	
	private Syllabifier readSyllabifier(File file)
		throws IOException {
		Syllabifier retVal = null;
		try {
			final BasicSyllabifierIO io = new BasicSyllabifierIO();
			retVal = io.readFromFile(file);
		} catch (IOException e) {
			// try an opgraph syllabifier
			try {
				final OpGraph graph = OpgraphIO.read(file);
				if(graph.getExtension(SyllabifierSettings.class) != null) {
					retVal = new OpGraphSyllabifier(graph);
				}
			} catch (IOException e2) {
				throw new IOException(file.getAbsolutePath() + " is not a syllabifier definition");
			}
		}
		return retVal;
	}

	private class UserSyllabifierIterator implements Iterator<Syllabifier> {
		
		private Iterator<File> fileIterator;
		
		public UserSyllabifierIterator(Iterator<File> fileIterator) {
			super();
			
			this.fileIterator = fileIterator;
		}

		@Override
		public boolean hasNext() {
			return fileIterator.hasNext();
		}

		@Override
		public Syllabifier next() {
			Syllabifier retVal = null;
			
			final File file = fileIterator.next();
			try {
				retVal = readSyllabifier(file);
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
			
			return retVal;
		}
		
	}
	
}
