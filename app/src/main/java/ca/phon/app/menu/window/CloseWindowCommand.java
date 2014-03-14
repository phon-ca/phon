package ca.phon.app.menu.window;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

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
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		window.setVisible(false);
		window.dispose();
	}
	
}
