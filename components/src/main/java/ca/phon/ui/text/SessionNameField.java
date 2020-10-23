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
 * {@link PromptedTextField} for session names.
 */
public class SessionNameField extends CorpusNameField {

	private static final long serialVersionUID = -4011963535518289390L;

	public SessionNameField() {
		this(null);
	}

	public SessionNameField(Project project) {
		super(project);

		setPrompt("Session name");
	}

	@Override
	protected void setupAutocomplete() {
		super.setupAutocomplete();

		if(getProject() != null) {
			for(String corpus:getProject().getCorpora()) {
				for(String sessionName:getProject().getCorpusSessions(corpus)) {
					getCompleterModel().addCompletion( sessionName, sessionName );
				}
			}
		}
	}

}
