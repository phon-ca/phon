/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.syllabifier;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import ca.phon.app.opgraph.OpgraphIO;
import ca.phon.opgraph.OpGraph;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierProvider;
import ca.phon.syllabifier.basic.BasicSyllabifierIO;
import ca.phon.syllabifier.opgraph.OpGraphSyllabifier;
import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;
import ca.phon.util.PrefHelper;

public class UserSyllabifierProvider implements SyllabifierProvider {
	
	private final static Logger LOGGER = Logger.getLogger(UserSyllabifierProvider.class.getName());
	
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
			
			return retVal;
		}
		
	}
	
}
