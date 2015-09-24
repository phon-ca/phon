package ca.phon.app.project.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import ca.phon.app.project.ProjectWindow;
import ca.phon.project.Project;
import ca.phon.util.CollatorFactory;

/**
 * Duplicate selected corpora in the project window. Corpus names
 * are suffixed with an index.
 * 
 * E.g., <code>MyCorpus</code> becomes <code>MyCorpus (1)</code>
 *
 */
public class DuplicateCorpusAction extends ProjectWindowAction {

	private final static Logger LOGGER = Logger.getLogger(DuplicateCorpusAction.class.getName());
	
	private static final long serialVersionUID = -500973090454907775L;

	public DuplicateCorpusAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Duplicate Corpus");
		putValue(SHORT_DESCRIPTION, "Duplicate selected corpus/corpora");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		// duplicate all selected corpora
		final List<String> corpora = getWindow().getSelectedCorpora();
		final List<String> dupCorpusNames = new ArrayList<>();
		final List<String> corpusDescs = new ArrayList<>();
		final Project project = getWindow().getProject();
		for(String corpus:corpora) {
			int idx = 0;
			String corpusName = corpus + " (" + (++idx) + ")";
			while(project.getCorpora().contains(corpusName)) {
				corpusName = corpus + " (" + (++idx) + ")";
			}
			final File oldCorpusFile = new File(project.getCorpusPath(corpus));
			final File dupCorpusFile = new File(project.getCorpusPath(corpusName));
			try {
				FileUtils.copyDirectory(oldCorpusFile, dupCorpusFile);
				dupCorpusNames.add(corpusName);
				corpusDescs.add(project.getCorpusDescription(corpus));
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				Toolkit.getDefaultToolkit().beep();
				showMessage("Duplicate Corpus", e.getLocalizedMessage());
			}
		}
		if(corpora.size() > 0) {
			int indices[] = new int[dupCorpusNames.size()];
			getWindow().refreshProject();
			
			List<String> sessions = project.getCorpora();
			Collections.sort(sessions, CollatorFactory.defaultCollator());
			for(int i = 0; i < dupCorpusNames.size(); i++) {
				String corpusName = dupCorpusNames.get(i);

				// apply corpus descriptions to duplicated corpora
				String corpusDesc = corpusDescs.get(i);
				project.setCorpusDescription(corpusName, corpusDesc);
				
				indices[i] = sessions.indexOf(corpusName);
			}
			getWindow().getCorpusList().setSelectedIndices(indices);
		}
	}

}
