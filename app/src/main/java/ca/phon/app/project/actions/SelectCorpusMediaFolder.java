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

import ca.phon.app.project.ProjectWindow;
import ca.phon.project.Project;
import ca.phon.ui.nativedialogs.*;
import ca.phon.worker.PhonWorker;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class SelectCorpusMediaFolder extends ProjectWindowAction {

	private final static String TXT = "Select media folder...";

	private final static String DESC = "Select corpus media folder.";

	public SelectCorpusMediaFolder(ProjectWindow projectWindow) {
		super(projectWindow);

		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final Project project = getWindow()	.getProject();
		final List<String> corpora = getWindow().getSelectedCorpora();
		if(corpora.size() == 0) return;

		if(corpora.size() == 1) {
			final String corpus = corpora.get(0);
			final String defaultMediaFolder = project.getProjectMediaFolder();
			final String currentMediaFolder = project.getCorpusMediaFolder(corpus);
			if(currentMediaFolder.equals(defaultMediaFolder)) {
				browseForMediaFolder();
			} else {
				final MessageDialogProperties props = new MessageDialogProperties();
				props.setParentWindow(getWindow());
				props.setRunAsync(true);
				final String[] options = {"Cancel", "Reset to project default", "Browse for folder..."};
				props.setOptions(options);
				props.setDefaultOption(options[0]);
				props.setMessage("Select media folder for corpus '" + corpus + "'");
				props.setHeader("Select corpus media folder");
				props.setListener( (e) -> {
					int result = e.getDialogResult();
					if(result == 0) {
						return;
					} else if(result == 1) {
						PhonWorker.getInstance().invokeLater( this::resetCorpusMediaFolder );
					} else if(result == 2) {
						SwingUtilities.invokeLater( this::browseForMediaFolder );
					}
				});
				NativeDialogs.showMessageDialog(props);
			}
		} else {
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setParentWindow(getWindow());
			props.setRunAsync(true);
			final String[] options = {"Cancel", "Reset to project default", "Browse for folder..."};
			props.setOptions(options);
			props.setDefaultOption(options[0]);
			props.setMessage("Select media folder for corpora");
			props.setHeader("Select media folder for corpora");
			props.setListener( (e) -> {
				int result = e.getDialogResult();
				if(result == 0) {
					return;
				} else if(result == 1) {
					PhonWorker.getInstance().invokeLater( this::resetCorpusMediaFolder );
				} else if(result == 2) {
					SwingUtilities.invokeLater( this::browseForMediaFolder );
				}
			});
			NativeDialogs.showMessageDialog(props);
		}
	}
	
	private void resetCorpusMediaFolder() {
		final Project project = getWindow().getProject();
		List<String> corpora = getWindow().getSelectedCorpora();
		
		for(String corpus:corpora) {
			project.setCorpusMediaFolder(corpus, null);
		}
	}

	private void browseForMediaFolder() {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(getWindow());
		props.setRunAsync(true);
		props.setCanChooseDirectories(true);
		props.setCanChooseFiles(true);
		props.setAllowMultipleSelection(false);
		props.setPrompt("Select Folder");
		props.setTitle("Corpus Media Folder");
		props.setListener( (e) -> {
			if(e.getDialogData() == null) return;
			final String selectedFolder = e.getDialogData().toString();
			
			PhonWorker.getInstance().invokeLater( () -> {
				final Project project = getWindow().getProject();
				for(String corpus:getWindow().getSelectedCorpora()) {
					project.setCorpusMediaFolder(corpus, selectedFolder);
				}
			});
		});
		NativeDialogs.showOpenDialog(props);
	}

}
