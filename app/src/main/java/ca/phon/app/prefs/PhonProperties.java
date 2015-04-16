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
package ca.phon.app.prefs;

import ca.phon.app.autosave.AutosaveManager;
import ca.phon.ipadictionary.IPADictionaryLibrary;
import ca.phon.media.util.MediaLocator;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.workspace.Workspace;

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
	public final static String UI_THEME = "ca.phon.app.ui.theme";
	
}
