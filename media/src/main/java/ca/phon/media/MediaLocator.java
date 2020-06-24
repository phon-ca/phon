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
package ca.phon.media;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.util.EmptyQueueException;
import ca.phon.util.PrefHelper;
import ca.phon.util.Queue;
import ca.phon.util.Tuple;

/**
 * Helper methods for locating and handling paths for media files.
 *
 * Also includes methods for setting up the media include paths.
 *
 */
public class MediaLocator {

	/**
	 * Media include path property.
	 * This value of this property should be a semi-colon separated list of
	 * paths in order of search priority.
	 */
	public static final String MEDIA_INCLUDE_PATH_PROP = "ca.phon.media.util.MediaLocator.includepath";

	/**
	 * Set media include path as a list of
	 * paths.
	 *
	 * @param paths
	 * @param props
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
	 * @param project
	 * @return
	 */
	public static List<String> getMediaIncludePaths(Project project) {
		return getMediaIncludePaths(project, null);
	}

	public static List<String> getMediaIncludePaths(Project project, String corpus) {
		List<String> retVal = new ArrayList<String>();

		if(project != null) {
			String projectMediaPath = project.getProjectMediaFolder();
			
			if(corpus != null) {
				String corpusMediaPath = project.getCorpusMediaFolder(corpus);
				final File corpusMediaFolder = new File(corpusMediaPath);
				if(!corpusMediaFolder.isAbsolute())
					corpusMediaPath = project.getLocation() + File.separator + corpusMediaPath;
				if(!corpusMediaPath.equals(projectMediaPath))
					retVal.add(corpusMediaPath);
				
				String defaultCorpusMediaPath = projectMediaPath + File.separator + corpus;
				if(!projectMediaPath.equals(defaultCorpusMediaPath) && !corpusMediaPath.equals(defaultCorpusMediaPath))
					retVal.add(defaultCorpusMediaPath);
			}
			
			final File projectMediaFolder = new File(projectMediaPath);
			if(!projectMediaFolder.isAbsolute())
				projectMediaPath = project.getLocation() + File.separator + projectMediaPath;
			retVal.add(projectMediaPath);
		}

		// add global paths
		for(String mediaPath:getMediaIncludePaths()) {
			if(corpus != null)
				retVal.add(mediaPath + File.separator + corpus);
			retVal.add(mediaPath);
		}

		return retVal;
	}

	/**
	 * Get media include path as a list of
	 * paths.
	 *
	 * @param props
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
	 * @param corpus (may be <code>null</code>)
	 * @return the file object for the file or null if not found
	 */
	 public static File findMediaFile(String filename, Project project, String corpus) {
		 Tuple<File, File> pathTuple = findMediaFileRelative(filename, project, corpus);
		 if(pathTuple == null) return null;
		 else {
			 if(pathTuple.getObj1() == null) {
				 return pathTuple.getObj2();
			 } else {
				 return new File(pathTuple.getObj1(), pathTuple.getObj2().getPath());
			 }
		 }
	 }
	 
	 public static Tuple<File, File> findMediaFileRelative(String filename, Project project, String corpus) {
		 Tuple<File, File> retVal = null;

		 if(filename == null) return retVal;

		 // if filename does not have an extension, go through the list
		 // of supported extensions and return the first found (if any)
		 int extIdx = filename.lastIndexOf('.');
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
	 
}
