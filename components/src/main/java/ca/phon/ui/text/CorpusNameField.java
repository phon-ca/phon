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
package ca.phon.ui.text;

import ca.phon.orthography.InternalMedia;
import ca.phon.project.Project;

import javax.swing.*;
import java.util.Iterator;

/**
 * {@link PromptedTextField} for entering a corpus name.
 * If a project is provided, an autocomplete list of
 * current corpora is provided.
 */
public class CorpusNameField extends PromptedTextField {

	private static final long serialVersionUID = -1217905871664832312L;

	private Project project;

	protected DefaultTextCompleterModel completerModel;

	public CorpusNameField() {
		this(null);
	}

	public CorpusNameField(Project project) {
		super();

		setPrompt("Corpus name");
		setProject(project);
	}

	public void setProject(Project project) {
		this.project = project;
		setupAutocomplete();
	}

	public Project getProject() {
		return this.project;
	}

	public DefaultTextCompleterModel getCompleterModel() {
		return this.completerModel;
	}

	protected void setupAutocomplete() {
		if(this.completerModel == null) {
			this.completerModel = new DefaultTextCompleterModel();

			final TextCompleter completer = new TextCompleter(completerModel);
			completer.setUseDataForCompletion(true);
			completer.install(this);
		}
		final AutocompleterSetupWorker worker = new AutocompleterSetupWorker();
		worker.execute();
	}

	protected SwingWorker<?, ?> getAutocompleteWorker() {
		return new AutocompleterSetupWorker();
	}

	private class AutocompleterSetupWorker extends SwingWorker<Integer, String> {
		@Override
		protected Integer doInBackground() throws Exception {
			int result = 0;
			if(getProject() != null) {
				final Iterator<String> corpusIterator = getProject().getCorpusIterator();
				while(corpusIterator.hasNext()) {
					final String corpus = corpusIterator.next();
					publish(corpus);
					++result;
				}
			}
			return result;
		}

		@Override
		protected void process(java.util.List<String> chunks) {
			for(String chunk : chunks) {
				completerModel.addCompletion(chunk);
			}
		}

		@Override
		protected void done() {
			super.done();
		}
	}

}
