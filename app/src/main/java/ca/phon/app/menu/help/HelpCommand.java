package ca.phon.app.menu.help;

import javax.swing.Action;

import ca.phon.plugin.PluginAction;

/**
 * Show application help
 */
public class HelpCommand extends PluginAction {

	private static final long serialVersionUID = -2211753524151844715L;

	public HelpCommand() {
		super("Help");
		putValue(Action.NAME, "About Phon");
		putValue(Action.SHORT_DESCRIPTION, "View about dialog and licence agreement");
	}
	
}
