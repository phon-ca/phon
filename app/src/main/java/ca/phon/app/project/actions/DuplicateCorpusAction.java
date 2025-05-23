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

import ca.phon.app.log.LogUtil;
import ca.phon.app.project.ProjectWindow;
import ca.phon.project.LocalProject;
import ca.phon.project.MutableProject;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.util.CollatorFactory;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Duplicate selected corpora in the project window. Corpus names
 * are suffixed with an index.
 *
 * E.g., <code>MyCorpus</code> becomes <code>MyCorpus (1)</code>
 *
 */
public class DuplicateCorpusAction extends ProjectWindowAction {

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
			final Pattern pattern = Pattern.compile("(.+) \\((\\d+)\\)");
			final Matcher matcher = pattern.matcher(corpus);
			if(matcher.matches()) {
				// get index from session name
				String idxStr = matcher.group(2);
				try {
					idx = Integer.parseInt(idxStr);
				} catch (NumberFormatException e) {
					// ignore - should not happen
				}
			}
			String corpusName = corpus + " (" + (++idx) + ")";
			while(project.hasCorpus(corpusName)) {
				corpusName = corpus + " (" + (++idx) + ")";
			}
			if(project instanceof LocalProject localProject) {
				final File oldCorpusFile = new File(localProject.getCorpusPath(corpus));
				final File dupCorpusFile = new File(localProject.getCorpusPath(corpusName));
				try {
					FileUtils.copyDirectory(oldCorpusFile, dupCorpusFile);
					dupCorpusNames.add(corpusName);
					corpusDescs.add(project.getCorpusDescription(corpus));
				} catch (IOException e) {
					LogUtil.warning(e);
					Toolkit.getDefaultToolkit().beep();
					showMessage("Duplicate Corpus", e.getLocalizedMessage());
				}
			} else {
				final MutableProject  mutableProject = project.getExtension(MutableProject.class);
				if(mutableProject != null) {
					final String corpusDescription = project.getCorpusDescription(corpus);
					try {
						mutableProject.addCorpus(corpusName, corpusDescription);
						final Iterator<String> sessionItr = project.getSessionIterator(corpus);
						while(sessionItr.hasNext()) {
							final String sessionName = sessionItr.next();
							final Session session = project.openSession(corpus, sessionName);
							session.setCorpus(corpusName);

							final var writeLock = mutableProject.getSessionWriteLock(session);
							try {
								mutableProject.saveSession(session, writeLock);
							} finally {
								mutableProject.releaseSessionWriteLock(session, writeLock);
							}
						}
					} catch (IOException e) {
						LogUtil.warning(e);
						Toolkit.getDefaultToolkit().beep();
						showMessage("Duplicate Corpus", e.getLocalizedMessage());
					}
				} else {
					Toolkit.getDefaultToolkit().beep();
					showMessage("Duplicate Corpus", "Cannot duplicate corpus in this project type.");
				}
			}
		}
		if(corpora.size() > 0) {
			int indices[] = new int[dupCorpusNames.size()];
			getWindow().refreshProject();

			List<String> projectCorpora = new ArrayList<>();
			final Iterator<String> corpusItr = project.getCorpusIterator();
			while(corpusItr.hasNext()) {
				projectCorpora.add(corpusItr.next());
			}
			Collections.sort(projectCorpora, CollatorFactory.defaultCollator());

			final MutableProject mutableProject = project.getExtension(MutableProject.class);
			if(mutableProject != null) {
				for (int i = 0; i < dupCorpusNames.size(); i++) {
					String corpusName = dupCorpusNames.get(i);

					// apply corpus descriptions to duplicated corpora
					String corpusDesc = corpusDescs.get(i);
					mutableProject.setCorpusDescription(corpusName, corpusDesc);

					indices[i] = projectCorpora.indexOf(corpusName);
				}
			}
			getWindow().getCorpusList().setSelectedIndices(indices);
		}
	}

}
