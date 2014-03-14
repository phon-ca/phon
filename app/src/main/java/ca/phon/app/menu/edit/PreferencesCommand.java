package ca.phon.app.menu.edit;

import javax.swing.Action;

import ca.phon.plugin.PluginAction;

/**
 * Open preferences dialog
 */
public class PreferencesCommand extends PluginAction {

	private static final long serialVersionUID = 4295409487719796945L;
	
	private final static String EP = "Preferences";
	
	public PreferencesCommand() {
		super(EP);
		putValue(Action.NAME, "Preferences...");
		putValue(Action.SHORT_DESCRIPTION, "Edit application preferences");
	}
	
}
