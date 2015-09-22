package ca.phon.app.project.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import ca.phon.app.project.ProjectWindow;
import ca.phon.project.Project;

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
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				Toolkit.getDefaultToolkit().beep();
				showMessage("Duplicate Corpus", e.getLocalizedMessage());
			}
		}
		if(corpora.size() > 0) {
			getWindow().getCorpusList().clearSelection();
			getWindow().refreshProject();
		}
	}

}
