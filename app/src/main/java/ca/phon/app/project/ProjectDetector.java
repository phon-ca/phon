/*
 * Copyright (C) 2005-2021 Gregory Hedlund & Yvan Rose
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

import ca.phon.app.log.LogUtil;
import ca.phon.project.*;
import ca.phon.session.io.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

/**
 * Class which attempts to detect if a given file folder
 * is an actual Phon project folder.  This class performs
 * the following checks and returns <code>true</code> if
 * two or more pass:
 * <ul>
 *     <li>check for a project.properties file</li>
 *     <li>check for a __res folder</li>
 *     <li>check for a backups.zip file</li>
 *     <li>check folders for a session file inside a corpus folder</li>
 * </ul>
 */
public class ProjectDetector {

	private final static int MIN_SCORE = 2;

	private boolean hasPropertiesFiles(File projectFolder) {
		final File oldPropsFile = new File(projectFolder, LocalProject.PREV_PROJECT_PROPERTIES_FILE);
		final File propsFile = new File(projectFolder, LocalProject.PROJECT_PROPERTIES_FILE);

		return propsFile.exists() || oldPropsFile.exists();
	}

	private boolean hasResourcesFolder(File projectFolder) {
		final File resFolder = new File(projectFolder, "__res");
		return resFolder.exists();
	}

	private boolean hasBackupsZip(File projectFolder) {
		final File backupZipFile = new File(projectFolder, "backups.zip");
		return backupZipFile.exists();
	}

	private boolean hasSessionFilesInChildFolders(File projectFolder) {
		if(!projectFolder.exists()) return false;
		if(!projectFolder.isDirectory()) return false;

		try(DirectoryStream<Path> projectFolderStream = Files.newDirectoryStream(projectFolder.toPath())) {
			for(Path p: projectFolderStream) {
				if(!Files.isDirectory(p)) continue;
				try (DirectoryStream<Path> stream = Files.newDirectoryStream(p)) {
					for (Path path : stream) {
						if (!Files.isDirectory(path) && !Files.isHidden(path)) {
							String filename = path.getFileName().toString();
							if (!filename.startsWith("~")
									&& !filename.endsWith("~")
									&& !filename.startsWith("__")) {
								int lastDot = filename.lastIndexOf('.');
								if (lastDot > 0) {
									String ext = filename.substring(lastDot + 1);
									if (SessionInputFactory.getSessionExtensions().contains(ext)) {
										SessionInputFactory factory = new SessionInputFactory();
										SessionReader reader = factory.createReaderForFile(path.toFile());
										return reader != null;
									}
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			LogUtil.severe(e);
		}
		return false;
	}

	public boolean isPhonProjectFolder(File folder) {
		List<Function<File, Boolean>> checks = new ArrayList<>();
		checks.add(this::hasPropertiesFiles);
		checks.add(this::hasResourcesFolder);
		checks.add(this::hasBackupsZip);
		checks.add(this::hasSessionFilesInChildFolders);

		int score = (int)checks.parallelStream().filter(check -> check.apply(folder)).count();
		return score >= MIN_SCORE;
	}

}
