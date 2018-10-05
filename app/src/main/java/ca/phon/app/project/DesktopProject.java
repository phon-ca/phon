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
package ca.phon.app.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.UUID;

import com.sun.jna.platform.FileUtils;

import ca.phon.app.prefs.PhonProperties;
import ca.phon.project.LocalProject;
import ca.phon.project.ProjectEvent;
import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.project.io.CorpusType;
import ca.phon.project.io.SessionType;
import ca.phon.util.PrefHelper;

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

	@Override
	protected void saveProperties() throws IOException {
		final Properties props = getExtension(Properties.class);
		if(props != null) {
			// adjust media paths as required by application settings
			final boolean makePathsRelative = PrefHelper.getBoolean(PhonProperties.MEDIA_PATHS_RELATIVE, PhonProperties.DEFAULT_MEDIA_PATHS_RELATIVE);

			if(props.containsKey(LocalProject.PROJECT_MEDIAFOLDER_PROP)) {
				String currentValue = props.getProperty(LocalProject.PROJECT_MEDIAFOLDER_PROP);
				File currentFile = new File(currentValue);
				if(!currentFile.isAbsolute()) {
					currentValue = getLocation() + File.separator + currentValue;
				}

				final Path projectFolder = Paths.get(getLocation());
				Path currentPath = Paths.get(currentValue);

				if(makePathsRelative) {
					if(projectFolder.getRoot().equals(currentPath.getRoot())) {
						currentPath = projectFolder.relativize(currentPath);
					} else {
						currentPath = currentPath.toRealPath();
					}
				} else {
					currentPath = currentPath.toRealPath();
				}
				props.setProperty(PROJECT_MEDIAFOLDER_PROP, currentPath.toString());
			}

			for(String corpus:getCorpora()) {
				final String corpusProp = LocalProject.CORPUS_MEDIAFOLDER_PROP + "." + corpus;
				if(props.containsKey(corpusProp)) {
					String currentValue = props.getProperty(corpusProp);
					File currentFile = new File(currentValue);
					// make current value absolute
					if(!currentFile.isAbsolute()) {
						currentValue = getLocation() + File.separator + currentValue;
					}

					final Path projectFolder = Paths.get(getLocation());
					Path currentPath = Paths.get(currentValue);

					if(makePathsRelative) {
						if(projectFolder.getRoot().equals(currentPath.getRoot())) {
							currentPath = projectFolder.relativize(currentPath);
						} else {
							currentPath = currentPath.toRealPath();
						}
					} else {
						currentPath = currentPath.toRealPath();
					}
					props.setProperty(corpusProp, currentPath.toString());
				}
			}
		}

		super.saveProperties();
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
