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
package ca.phon.app.project;

import ca.phon.project.*;
import ca.phon.project.exceptions.ProjectConfigurationException;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.*;
import java.util.UUID;

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

		ProjectEvent pe = ProjectEvent.newCorpusRemovedEvent(corpus);
		fireProjectStructureChanged(pe);
	}

	@Override
	public void removeSession(String corpus, String session, UUID writeLock) throws IOException {
		checkSessionWriteLock(corpus, session, writeLock);
		final String sessionPath = getSessionPath(corpus, session);

		final File[] toTrash = new File[] { new File(sessionPath) };
		moveToTrash(toTrash);

		final ProjectEvent pe = ProjectEvent.newSessionRemovedEvent(corpus, session);
		fireProjectStructureChanged(pe);
	}

	private void moveToTrash(File[] files) throws IOException {
		if(Desktop.isDesktopSupported()) {
			for(File file:files) {
				if(!Desktop.getDesktop().moveToTrash(file)) {
					throw new IOException("Unable to move file to trash: " + file.getAbsolutePath());
				}
			}
		} else {
			// delete
			for(File file:files) {
				FileUtils.forceDelete(file);
			}
		}
	}

}
