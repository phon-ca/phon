package ca.phon.app.menu.help;

import javax.swing.Action;

import ca.phon.app.log.LogEP;
import ca.phon.plugin.PluginAction;

/**
 * Show log viewer window
 */
public class LogCommand extends PluginAction {

	private static final long serialVersionUID = 6579257273862388225L;

	public LogCommand() {
		super(LogEP.EP_NAME);
		putValue(Action.NAME, "Application log...");
		putValue(Action.SHORT_DESCRIPTION, "View application error log");
	}
	
}
