package ca.phon.app.menu.file;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import ca.phon.plugin.PluginAction;

/**
 * Command for exiting the application.
 */
public class ExitCommand extends PluginAction {

	private static final long serialVersionUID = -2743379216250130058L;
	
	private final static String EP = "Exit";
	
	public ExitCommand() {
		super(EP);
		putValue(Action.NAME, "Exit");
		putValue(Action.SHORT_DESCRIPTION, "Exit the application.");
		putValue(Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}

}
