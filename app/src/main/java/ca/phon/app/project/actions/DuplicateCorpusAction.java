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
package ca.phon.app.project.actions;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import org.apache.commons.io.*;

import ca.phon.app.project.*;
import ca.phon.project.*;
import ca.phon.util.*;

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

		putValue(NAME, "Duplicate corpus");
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
