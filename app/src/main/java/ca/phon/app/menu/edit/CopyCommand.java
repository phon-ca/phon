package ca.phon.app.menu.edit;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import ca.phon.plugin.PluginAction;

/**
 * 
 */
public class CopyCommand extends PluginAction {
	
	private static final long serialVersionUID = 4157826605621869802L;
	
	private final static String EP = "Copy";
	
	public CopyCommand() {
		super(EP);
		putValue(Action.NAME, "Copy");
		putValue(Action.SHORT_DESCRIPTION, "Edit: copy");
		putValue(Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}

}
