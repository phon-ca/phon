/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
