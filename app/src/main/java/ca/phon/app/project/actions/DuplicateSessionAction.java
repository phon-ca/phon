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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import ca.phon.app.project.ProjectWindow;
import ca.phon.project.Project;
import ca.phon.util.CollatorFactory;

/**
 * Duplicate sessions selected in project window.  Session names
 * are suffixed with an index.
 * 
 * E.g., <code>MySession</code> becomes <code>MySession (1)</code>
 *
 */
public class DuplicateSessionAction extends ProjectWindowAction {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(DuplicateSessionAction.class.getName());

	private static final long serialVersionUID = 140755322409598286L;

	public DuplicateSessionAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Duplicate Session");
		putValue(SHORT_DESCRIPTION, "Duplicate selected session(s)");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		// duplicate all selected sessions
		final String corpus = getWindow().getSelectedCorpus();
		if(corpus == null) return;
		
		final List<String> sessionNames = getWindow().getSelectedSessionNames();
		final List<String> dupSessionNames = new ArrayList<>();
		final Project project = getWindow().getProject();
		for(String sessionName:sessionNames) {
			int idx = 0;
			String dupSessionName = sessionName + " (" + (++idx) + ")";
			while(project.getCorpusSessions(corpus).contains(dupSessionName)) {
				dupSessionName = sessionName + " (" + (++idx) + ")";
			}
			final File oldSessionFile = new File(project.getSessionPath(corpus, sessionName));
			final File dupSessionFile = new File(project.getSessionPath(corpus, dupSessionName));
			try {
				FileUtils.copyFile(oldSessionFile, dupSessionFile);
				dupSessionNames.add(dupSessionName);
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
				Toolkit.getDefaultToolkit().beep();
				showMessage("Duplicate Session", e.getLocalizedMessage());
			}
		}
		if(sessionNames.size() > 0) {
			int indices[] = new int[dupSessionNames.size()];
			getWindow().refreshProject();
			List<String> sessions = project.getCorpusSessions(getWindow().getSelectedCorpus());
			Collections.sort(sessions, CollatorFactory.defaultCollator());
			for(int i = 0; i < dupSessionNames.size(); i++) {
				String sessionName = dupSessionNames.get(i);
				indices[i] = sessions.indexOf(sessionName);
			}
			getWindow().getSessionList().setSelectedIndices(indices);
		}
	}

}
