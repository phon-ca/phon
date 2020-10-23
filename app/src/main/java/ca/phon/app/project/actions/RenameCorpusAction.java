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
package ca.phon.app.project.actions;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import ca.phon.app.project.*;
import ca.phon.project.*;
import ca.phon.util.*;

public class RenameCorpusAction extends ProjectWindowAction {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(RenameCorpusAction.class.getName());

	private static final long serialVersionUID = 1699053938382896780L;

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
	
			if (project.getCorpora().contains(newCorpusName)) {
				showMessage(
					"Rename Corpus",
					"The new corpus name you specified already exists!");
				return;
			}
	
			// Create new corpus, transfer sessions over to it and delete
			// the old corpus
			try {
				project.renameCorpus(corpusName, newCorpusName);
				
				final List<String> corpora = project.getCorpora();
				Collections.sort(corpora, CollatorFactory.defaultCollator());
				int idx = corpora.indexOf(newCorpusName);
				if(idx >= 0) {
					getWindow().getCorpusList().setSelectedIndex(idx);
				}
			} catch(IOException e) {
				showMessage("Rename Corpus", 
						"Failed to rename corpus " + corpusName + ". Reason: " + e.getMessage());
				Toolkit.getDefaultToolkit().beep();
				LOGGER.error( e.getMessage(), e);
			}
		}
	}

}
