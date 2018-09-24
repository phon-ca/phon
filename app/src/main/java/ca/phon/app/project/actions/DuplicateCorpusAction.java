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
package ca.phon.app.project.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(DuplicateCorpusAction.class.getName());

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

				if(!project.getCorpusMediaFolder(corpus).equals(project.getProjectMediaFolder())) {
					project.setCorpusMediaFolder(corpusName, project.getCorpusMediaFolder(corpus));
				}

			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
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
