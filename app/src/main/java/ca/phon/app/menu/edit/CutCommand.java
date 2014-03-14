package ca.phon.app.menu.edit;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import ca.phon.plugin.PluginAction;

/**
 * Perform the standard 'cut' operation on components which support it.
 */
public class CutCommand extends PluginAction {

	private static final long serialVersionUID = -1665767135021065301L;
	
	private final static String EP = "Cut";
	
	public CutCommand() {
		super(EP);
		putValue(Action.NAME, "Cut");
		putValue(Action.SHORT_DESCRIPTION, "Edit: cut");
		putValue(Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}

}
