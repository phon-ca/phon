package ca.phon.app.menu.tools;

import javax.swing.Action;

import ca.phon.app.help.LanguageCodeEP;
import ca.phon.plugin.PluginAction;

/**
 * Show the language code lookup window.
 *
 */
public class LanguageCodesCommand extends PluginAction {

	private static final long serialVersionUID = -7475966189706158219L;

	public LanguageCodesCommand() {
		super(LanguageCodeEP.EP_NAME);
		putValue(Action.NAME, "ISO-639-3 Language Codes");
		putValue(Action.SHORT_DESCRIPTION, "Standard 3 letter language codes");
	}	
	
}
