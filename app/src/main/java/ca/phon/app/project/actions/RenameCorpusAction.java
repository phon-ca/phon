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
import ca.phon.app.project.*;
import ca.phon.project.MutableProject;
import ca.phon.project.Project;
import ca.phon.util.CollatorFactory;
import ca.phon.worker.PhonWorker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class RenameCorpusAction extends ProjectWindowAction {
	
	public RenameCorpusAction(ProjectWindow projectWindow) {
		super(projectWindow);
	
		putValue(NAME, "Rename corpus...");
		putValue(SHORT_DESCRIPTION, "Rename selected corpus");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final Project project = getWindow().getProject();
		final String corpus = getWindow().getSelectedCorpus();
		
		final RenameCorpusDialog dialog = new RenameCorpusDialog(project, corpus);
		dialog.setModal(true);
		dialog.pack();
		dialog.setLocationRelativeTo(getWindow());
		dialog.setVisible(true);
		
		if(!dialog.wasCanceled()) {
			final String corpusName = dialog.getCorpusName();
			final String newCorpusName = dialog.getNewCorpusName();
			
			// rename corpus
			if (newCorpusName == null || newCorpusName.length() == 0) {
				showMessage(
					"Rename Corpus",
					"Please enter corpus name");
				return;
			}
	
			if (project.hasCorpus(newCorpusName)) {
				showMessage(
					"Rename Corpus",
					"The new corpus name you specified already exists!");
				return;
			}

			final MutableProject mutableProject = project.getExtension(MutableProject.class);
			if (mutableProject == null) {
				showMessage(
					"Rename Corpus",
					"Project does not support renaming corpora.");
				return;
			}
			// Create new corpus, transfer sessions over to it and delete
			// the oldLoc corpus
			try {
				mutableProject.renameCorpus(corpusName, newCorpusName);

				PhonWorker.getInstance().invokeLater(() -> {
					final Iterator<String> corpusIter = project.getCorpusIterator();
					int idx = 0;
					while(corpusIter.hasNext()) {
						final String corpusNameIter = corpusIter.next();
						if(corpusName.equals(corpusNameIter)) {
							break;
						}
						++idx;
					}
					if(idx >= 0) {
						final int finalIdx = idx;
						SwingUtilities.invokeLater(() -> {
							getWindow().getCorpusList().setSelectedIndex(finalIdx);
						});
					}
				});
			} catch(IOException e) {
				showMessage("Rename Corpus", 
						"Failed to rename corpus " + corpusName + ". Reason: " + e.getMessage());
				Toolkit.getDefaultToolkit().beep();
				LogUtil.warning(e);
			}
		}
	}

}
