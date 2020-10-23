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

import ca.phon.project.*;

/**
 * {@link PromptedTextField} for entering a corpus name.
 * If a project is provided, an autocomplete list of
 * current corpora is provided.
 */
public class CorpusNameField extends PromptedTextField {

	private static final long serialVersionUID = -1217905871664832312L;

	private Project project;

	private DefaultTextCompleterModel completerModel;

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
		if(getProject() != null) {
			getProject().getCorpora().forEach( (corpus) -> completerModel.addCompletion(corpus) );
		}
	}

}
