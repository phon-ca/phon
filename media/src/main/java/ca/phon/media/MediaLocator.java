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
package ca.phon.media;

import ca.phon.project.Project;
import ca.phon.project.ProjectMediaFolders;
import ca.phon.project.ProjectPaths;
import ca.phon.session.Session;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.util.Queue;
import ca.phon.util.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Helper methods for locating and handling paths for media files.
 *
 * Also includes methods for setting up the media include paths.
 *
 */
public class MediaLocator {

	/**
	 * Media include path property.
	 * This value of this property should be a semicolon separated list of
	 * paths in order of search priority.
	 */
	public static final String MEDIA_INCLUDE_PATH_PROP = "ca.phon.media.util.MediaLocator.includepath";

	/**
	 * Set global media include paths for the user.
	 *
	 * @param paths
	 */
	public static void setMediaIncludePaths(List<String> paths) {
		String includePath = "";

		for(String path:paths) {
			includePath += path + ";";
		}

		PrefHelper.getUserPreferences().put(MEDIA_INCLUDE_PATH_PROP, includePath);
	}

	public static List<String> getMediaIncludePaths() {
		return parseMediaIncludePaths();
	}

	/**
	 * Get media include paths for the user and project.
	 *
	 * @param project
	 * @return
	 */
	public static List<String> getMediaIncludePaths(Project project) {
		return getMediaIncludePaths(project, null);
	}

	/**
	 * Get media include paths for the user and project including the given session folder.
	 *
	 * @param project a phon project
	 * @param sessionFolder a session folder
	 * @return
	 */
	public static List<String> getMediaIncludePaths(Project project, String sessionFolder) {
		List<String> retVal = new ArrayList<String>();

		if(project != null) {
			final ProjectPaths projectPaths = project.getExtension(ProjectPaths.class);
			if(projectPaths != null) {
				// add session folder
				if(sessionFolder != null) {
					retVal.add(projectPaths.getCorpusPath(sessionFolder));
				}

				final ProjectMediaFolders projectMediaFolders = project.getExtension(ProjectMediaFolders.class);
				if(projectMediaFolders != null) {
					for(String folder:projectMediaFolders.getProjectMediaFolders()) {
						final File projectMediaFolder = new File(folder);
						if(!projectMediaFolder.isAbsolute())
							folder = projectPaths.getLocation() + File.separator + folder;
						retVal.add(folder);
					}
				}
			}
		}

		// add global paths
		for(String mediaPath:getMediaIncludePaths()) {
			if(sessionFolder != null)
				retVal.add(mediaPath + File.separator + sessionFolder);
			retVal.add(mediaPath);
		}

		return retVal;
	}

	/**
	 * Get media include path as a list of
	 * paths.
	 *
	 * @return media include paths
	 */
	private static List<String> parseMediaIncludePaths() {
		List<String> retVal = new ArrayList<String>();

		final String includePath = PrefHelper.get(MEDIA_INCLUDE_PATH_PROP, null);
		if(includePath != null) {
			String[] paths = includePath.split(";");

			for(String path:paths) {
				if(StringUtils.strip(path).length() > 0) {
					retVal.add(path);
				}
			}
		}

		return retVal;
	}

	/**
	 * Find media given a project and session.
	 */
	public static File findMediaFile(Project project, Session session) {
		// build a list of possible media locations
		Queue<String> mediaLocations = new Queue<String>();
//		final PathExpander pe = new PathExpander();
		String mediaRef = session.getMediaLocation();
		if(mediaRef != null && mediaRef.length() > 0) {
//			mediaRef = pe.expandPath(mediaRef);
			mediaLocations.queue(mediaRef);
			// check for extension on media ref
			if(mediaRef.indexOf('.') < 0) {
				for(String ext:FileFilter.mediaFilter.getAllExtensions()) {
					mediaLocations.add(mediaRef + ext);
				}
			}
		} else {
			String baseName = session.getName();
			for(String ext:FileFilter.mediaFilter.getAllExtensions()) {
				mediaLocations.add(baseName + ext);
			}
		}

		File retVal = null;

		try {
			while((retVal == null) && (mediaLocations.peek() != null)) {
				String mediaLocation = mediaLocations.dequeue();
				retVal = findMediaFile(mediaLocation, project, session.getCorpus());
			}
		} catch (EmptyQueueException e) {
		}
		return retVal;
	}

	/**
	 * Search for a file in the media include paths.
	 *
	 * @param filename
	 * @return the file object for the file or null if not found
	 */
	public static File findMediaFile(String filename) {
		return findMediaFile(filename, null, null);
	}

	/**
	 * Search for a file in the media include path.
	 *
	 * Will look in the project resource directory first if
	 * project is not null.
	 *
	 * @param filename
	 * @param project (may be <code>null</code>)
	 * @param sessionFolder (may be <code>null</code>)
	 * @return the file object for the file or null if not found
	 */
	 public static File findMediaFile(String filename, Project project, String sessionFolder) {
		 Tuple<File, File> pathTuple = findMediaFileRelative(filename, project, sessionFolder);
		 if(pathTuple == null) return null;
		 else {
			 if(pathTuple.getObj1() == null) {
				 return pathTuple.getObj2();
			 } else {
				 return new File(pathTuple.getObj1(), pathTuple.getObj2().getPath());
			 }
		 }
	 }

	/**
	 * Search for a file in the media include path.
	 *
	 * @param filename
	 * @param project
	 * @param corpus
	 * @return
	 */
	 public static Tuple<File, File> findMediaFileRelative(String filename, Project project, String corpus) {
		 Tuple<File, File> retVal = null;

		 if(filename == null) return retVal;

		 final File file = new File(filename);
		 // if filename does not have an extension, go through the list
		 // of supported extensions and return the first found (if any)
		 int extIdx = file.getName().lastIndexOf('.');
		 if(extIdx < 0) {
			 FileFilter mediaFileFilter = FileFilter.mediaFilter;
			 for(String ext:mediaFileFilter.getAllExtensions()) {
				 String filenameToCheck = filename + "." + ext;
				 retVal = findMediaFileRelative(filenameToCheck, project, corpus);
				 if(retVal != null) break;
			 }
		 } else {
			 // do we already have an absolute path
			 final File mediaFile = new File(filename);
			 if(mediaFile.isAbsolute()) {
				 retVal = new Tuple<>(null, mediaFile);
			 } else {
				 final List<String> checkList = new ArrayList<String>();
				 checkList.add(filename);
	
				 // check project media folder, corpus media folder then global include paths
				 final List<String> mediaPaths = getMediaIncludePaths(project, corpus);
	
				 // look in rest of search paths
				 for(String path:mediaPaths) {
					 File parentPath = new File(path);
					 for(String checkName:checkList) {
						 final File checkFile = new File(path, checkName);
						 if(checkFile.exists()) {
							 retVal = new Tuple<>(parentPath, new File(checkName));
							 break;
						 }
					 }
					 if(retVal != null) break;
				 }
			 }
		 }

		 return retVal;
	 }

	/**
	 * Check to see if the provided absolute path is inside on of the
	 * media include paths.
	 *
	 * @param project the project to check
	 * @param path the absolute path to check
	 *
	 * @return the relative path to one of the media include paths or the
	 *  absolute path if it is not in a media include path
	 */
	public static String getRelativeMediaFilePath(Project project, String path) {
		final List<String> mediaFolderList = getMediaIncludePaths(project);
		for(String mediaFolder:mediaFolderList) {
			if(path.startsWith(mediaFolder)) {
				// use nio to get relative path
				final Path mediaPath = Paths.get(mediaFolder);
				final Path filePath = Paths.get(path);
				final Path relativePath = mediaPath.relativize(filePath);
				return relativePath.toString();
			}
		}
		return path;
	}

}
