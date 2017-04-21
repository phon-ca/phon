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
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.project.ProjectWindow;
import ca.phon.app.project.RenameCorpusDialog;
import ca.phon.project.Project;
import ca.phon.util.CollatorFactory;

public class RenameCorpusAction extends ProjectWindowAction {
	
	private final static Logger LOGGER = Logger.getLogger(RenameCorpusAction.class.getName());

	private static final long serialVersionUID = 1699053938382896780L;

	public RenameCorpusAction(ProjectWindow projectWindow) {
		super(projectWindow);
	
		putValue(NAME, "Rename Corpus...");
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
					"You must specify a non-empty corpus name!");
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
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

}
