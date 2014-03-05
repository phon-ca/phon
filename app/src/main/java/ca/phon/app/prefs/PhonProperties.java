package ca.phon.app.prefs;

import ca.phon.app.autosave.AutosaveManager;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.IPADictionaryLibrary;
import ca.phon.media.util.MediaLocator;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.workspace.Workspace;

/**
 * Property names used in the application.
 */
public class PhonProperties {
	
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
	 * Default syllabifier language
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
	 * Transcription font
	 */
	public final static String IPA_TRANSCRIPT_FONT = IPATranscript.class.getName() + ".transcriptFont";
	
	/**
	 * Default transcription font
	 */
	public final static String DEFAULT_IPA_TRANSCRIPT_FONT = "Charis SIL-12-PLAIN";
	
	/**
	 * UI transcript font
	 */
	public final static String IPA_UI_FONT = IPATranscript.class.getName() + ".uiFont";
	
	/**
	 * Default UI font
	 */
	public final static String DEFAULT_IPA_UI_FONT = "Charis SIL Compact-12-PLAIN"; 
	
	/**
	 * UI Theme
	 */
	public final static String UI_THEME = "ca.phon.app.ui.theme";
	
}
