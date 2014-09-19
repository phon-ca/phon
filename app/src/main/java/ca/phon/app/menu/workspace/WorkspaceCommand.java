package ca.phon.app.menu.workspace;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import ca.phon.plugin.PluginAction;

/**
 * Opens the workspace window.
 */
public class WorkspaceCommand extends PluginAction {

	private static final long serialVersionUID = -8057848271906537053L;

	private final static String EP = "Workspace";
	
	public WorkspaceCommand() {
		super(EP);
		putValue(Action.NAME, "Show Workspace window");
		putValue(Action.SHORT_DESCRIPTION, "Open the workspace dialog.");
		putValue(Action.ACCELERATOR_KEY, 
				KeyStroke.getKeyStroke(KeyEvent.VK_W, 
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK));
	}
	
}
