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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import org.apache.commons.io.*;

import ca.phon.app.project.*;
import ca.phon.project.*;
import ca.phon.util.*;

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
		
		putValue(NAME, "Duplicate session");
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
