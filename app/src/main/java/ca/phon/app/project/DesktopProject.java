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
package ca.phon.app.project;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import com.sun.jna.platform.FileUtils;

import ca.phon.project.LocalProject;
import ca.phon.project.ProjectEvent;
import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.project.io.CorpusType;
import ca.phon.project.io.SessionType;

/**
 * Local project which will send files to trash instead of
 * deleting them during corpus/session removal.
 *
 */
public class DesktopProject extends LocalProject {

	public DesktopProject(File projectFolder) throws ProjectConfigurationException {
		super(projectFolder);
	}

	@Override
	public void removeCorpus(String corpus) throws IOException {
		// move folder to trash
		final String corpusPath = getCorpusPath(corpus);

		final File[] toTrash = new File[] { new File(corpusPath) };
		moveToTrash(toTrash);
		
		// remove entry from project data
		final CorpusType ct = getCorpusInfo(corpus);
		if(ct != null) {
			getProjectData().getCorpus().remove(ct);
		}
		
		saveProjectData();
		
		ProjectEvent pe = ProjectEvent.newCorpusRemovedEvent(corpus);
		fireProjectStructureChanged(pe);
	}

	@Override
	public void removeSession(String corpus, String session, UUID writeLock) throws IOException {
		checkSessionWriteLock(corpus, session, writeLock);
		final String sessionPath = getSessionPath(corpus, session);
		
		final File[] toTrash = new File[] { new File(sessionPath) };
		moveToTrash(toTrash);
		
		final CorpusType ct = getCorpusInfo(corpus);
		if(ct != null) {
			final SessionType st = getSessionInfo(corpus, session);
			if(st != null) {
				ct.getSession().remove(st);
			}
		}
		
		saveProjectData();
		
		final ProjectEvent pe = ProjectEvent.newSessionRemovedEvent(corpus, session);
		fireProjectStructureChanged(pe);
	}
	
	private void moveToTrash(File[] files) throws IOException {
		FileUtils fileUtils = FileUtils.getInstance();
		if(fileUtils.hasTrash()) {
			fileUtils.moveToTrash(files);
		} else {
			// delete
			for(File file:files) {
				org.apache.commons.io.FileUtils.forceDelete(file);
			}
		}
	}

}
