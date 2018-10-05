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
package ca.phon.app.corpus;

import java.io.IOException;
import java.net.URL;

import ca.phon.app.session.editor.RecordEditorPerspective;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.project.Project;
import ca.phon.session.Session;

public class CorpusTemplateEditor extends SessionEditor {

	private static final long serialVersionUID = -5676865486332827373L;

	private final static String PERSPECTIVE_NAME = "CorpusTemplate";
	private final static String PERSPECTIVE_FILE = "CorpusTemplate.xml";
	
	public CorpusTemplateEditor(Project project, Session session) {
		super(project, session, null);
		
		// setup custom perspective
		final URL perspectiveURL = getClass().getResource(PERSPECTIVE_FILE);
		if(perspectiveURL != null) {
			final RecordEditorPerspective perspective = new RecordEditorPerspective(PERSPECTIVE_NAME, perspectiveURL);
			getViewModel().setupWindows(perspective);
			getViewModel().applyPerspective(perspective);
		}
		
		getToolbar().setVisible(false);
	}
	
	@Override
	public String getTitle() {
		return "Session Template : " + getSession().getCorpus();
	}
	
	@Override
	public boolean saveData() 
			throws IOException {
		final Project project = getProject();
		project.saveSessionTemplate(getSession().getCorpus(), getSession());
		setModified(false);
		return true;
	}

}
