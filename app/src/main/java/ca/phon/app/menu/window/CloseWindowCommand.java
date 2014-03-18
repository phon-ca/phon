package ca.phon.app.menu.window;

import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

/**
 * Close the specified window, prompting a save dialog
 * if necessary.
 *
 */
public class CloseWindowCommand extends AbstractAction {

	private final Window window;
	
	public CloseWindowCommand(Window window) {
		super();
		this.window = window;
		putValue(NAME, "Close");
		putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		window.setVisible(false);
		window.dispose();
	}
	
}
