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
package ca.phon.app.prefs;

import ca.phon.app.autosave.*;
import ca.phon.app.workspace.*;
import ca.phon.ipadictionary.*;
import ca.phon.media.*;
import ca.phon.syllabifier.*;
import ca.phon.ui.*;

/**
 * Property names used in the application.
 */
public class PhonProperties {

	public final static String DEBUG = "phon.debug";

	public final static Boolean DEFAULT_DEBUG = Boolean.FALSE;

	/**
	 * Autosave interval
	 */
	public final static String AUTOSAVE_INTERVAL = AutosaveManager.AUTOSAVE_INTERVAL_PROP;

	public final static Integer DEFAULT_AUTOSAVE_INTERVAL = 0;

	/**
	 * Media search paths
	 */
	public final static String MEDIA_INCLUDE_PATH = MediaLocator.MEDIA_INCLUDE_PATH_PROP;

	/**
	 * Store media paths relative to project
	 * 
	 * @deprecated since 3.1
	 */
	public final static String MEDIA_PATHS_RELATIVE = "ca.phon.project.mediaPathsRelative";

	/**
	 * @deprecated since 3.1
	 */
	public final static Boolean DEFAULT_MEDIA_PATHS_RELATIVE = Boolean.TRUE;

	/**
	 * Workspace folder
	 */
	public final static String WORKSPACE_FOLDER = Workspace.WORKSPACE_FOLDER;

	/**
	 * Default syllabifier language (IPA Target)
	 */
	public final static String SYLLABIFIER_LANGUAGE = SyllabifierLibrary.DEFAULT_SYLLABIFIER_LANG_PROP;

	/**
	 * Default syllabifier language
	 */
	public final static String DEFAULT_SYLLABIFIER_LANGUAGE = "eng";

	/**
	 * Default ipa dictionary language
	 */
	public final static String IPADICTIONARY_LANGUAGE = IPADictionaryLibrary.DEFAULT_IPA_DICTIONARY_PROP;

	/**
	 * Default dictionary lang
	 */
	public final static String DEFAULT_IPADICTIONARY_LANGUAGE = "eng";

	/**
	 * UI Theme
	 */
	public final static String UI_THEME = "ca.phon.app.theme";

	/**
	 * Enable fullscreen on mac
	 */
	public final static String MACOS_ENABLE_FULLSCREEN = CommonModuleFrame.MACOS_ENABLE_FULLSCREEN;

}
