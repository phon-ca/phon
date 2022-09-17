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

import ca.phon.app.project.*;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class NewCorpusAction extends ProjectWindowAction {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(NewCorpusAction.class.getName());

	private static final long serialVersionUID = -4385987381468266104L;
	
	public String corpusName;
	
	public String description;
	
	public boolean corpusCreated = false;
	
	public NewCorpusAction(ProjectWindow projectWindow) {
		this(projectWindow, null, null);
	}
	
	public NewCorpusAction(ProjectWindow projectWindow, String corpusName) {
		this(projectWindow, corpusName, "");
	}

	public NewCorpusAction(ProjectWindow projectWindow, String corpusName, String description) {
		super(projectWindow);
		this.corpusName = corpusName;
		this.description = description;
		
		putValue(NAME, "New corpus...");
		putValue(SHORT_DESCRIPTION, "Add corpus to project");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		String corpusName = this.corpusName;
		String desc = this.description;
		
		if(corpusName == null) {
			// show new corpus dialog
			final NewCorpusDialog dlg = new NewCorpusDialog(getWindow());
			dlg.setVisible(true);
			
			if(dlg.wasCanceled()) return;
			corpusName = dlg.getCorpusName();
			desc = dlg.getCorpusDescription();
		}
		
		try {
			getWindow().getProject().addCorpus(corpusName, desc);
			this.corpusCreated = true;
			getWindow().refreshProject();
		} catch (IOException e) {
			showMessage("New Corpus", e.getLocalizedMessage());
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}
	
	public boolean isCorpusCreated() {
		return this.corpusCreated;
	}

}
