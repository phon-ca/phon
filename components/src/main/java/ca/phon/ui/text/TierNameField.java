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
package ca.phon.ui.text;

import java.util.Arrays;

import ca.phon.project.Project;
import ca.phon.session.SystemTierType;

/**
 * {@link PromptedTextField} for a tier name.  If a project is given,
 * tier names from sessions may be added to the autocompletion model.
 */
public class TierNameField extends PromptedTextField {

	private static final long serialVersionUID = 7768945922139068933L;

	private Project project;

	private DefaultTextCompleterModel completerModel;

	public TierNameField() {
		this(null);
	}

	public TierNameField(Project project) {
		super();

		setProject(project);
	}

	public Project getProject() {
		return this.project;
	}

	public void setProject(Project project) {
		this.project = project;
		setupAutocomplete();
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
		Arrays.stream(SystemTierType.values()).forEach( (tier) -> completerModel.addCompletion( tier.getName() ) );

		if(getProject() != null) {
			// TODO setup tier names
		}
	}

}
