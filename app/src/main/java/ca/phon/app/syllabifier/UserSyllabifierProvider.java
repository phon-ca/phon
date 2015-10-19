package ca.phon.app.syllabifier;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import ca.gedge.opgraph.OpGraph;
import ca.phon.opgraph.OpgraphIO;
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
