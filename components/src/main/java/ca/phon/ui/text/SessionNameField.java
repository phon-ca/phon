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

import ca.phon.project.Project;

import javax.swing.*;
import java.util.Iterator;
import java.util.List;

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
	public SwingWorker getAutocompleteWorker() {
		if(getProject() == null) {
			return null;
		}
		if(getCompleterModel() == null) {
			this.completerModel = new DefaultTextCompleterModel();
		}
		final AutocompleteSetupWorker worker = new AutocompleteSetupWorker();
		return worker;
	}

	private class AutocompleteSetupWorker extends SwingWorker<Integer, String> {
		@Override
		protected Integer doInBackground() throws Exception {
			int result = 0;
			final Iterator<String> corpusItr = getProject().getCorpusIterator();
			while(corpusItr.hasNext()) {
				final String corpus = corpusItr.next();
				final Iterator<String> sessionItr = getProject().getSessionIterator(corpus);
				while(sessionItr.hasNext()) {
					final String session = sessionItr.next();
					publish(session);
					result++;
				}
			}
			return result;
		}

		@Override
		protected void process(List<String> chunks) {
			final DefaultTextCompleterModel completerModel = getCompleterModel();
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
