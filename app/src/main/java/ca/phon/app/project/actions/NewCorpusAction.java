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

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.*;

import ca.phon.app.project.*;

public class NewCorpusAction extends ProjectWindowAction {
	
	private final static Logger LOGGER = Logger.getLogger(NewCorpusAction.class.getName());

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
		
		putValue(NAME, "New Corpus...");
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
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	public boolean isCorpusCreated() {
		return this.corpusCreated;
	}

}
