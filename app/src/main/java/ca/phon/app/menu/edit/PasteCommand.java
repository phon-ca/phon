package ca.phon.app.menu.edit;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import ca.phon.plugin.PluginAction;

/**
 * 
 */
public class PasteCommand extends PluginAction {

	private static final long serialVersionUID = -3389448241139697134L;
	
	private final static String EP = "Paste";
	
	public PasteCommand() {
		super(EP);
		putValue(Action.NAME, "Paste");
		putValue(Action.SHORT_DESCRIPTION, "Edit: paste");
		putValue(Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}
	
}
