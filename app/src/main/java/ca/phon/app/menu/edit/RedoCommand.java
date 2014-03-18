package ca.phon.app.menu.edit;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * Handle the redo command for a given {@link UndoManager}
 */
public class RedoCommand extends AbstractAction {

	private static final long serialVersionUID = 9100941568698565601L;

	private UndoManager undoManager;
	
	private final static String PREFIX = "Redo";
	
	public RedoCommand(UndoManager manager) {
		super();
		this.undoManager = manager;
		
		putValue(NAME, PREFIX + manager.getPresentationName());
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		undoManager.redo();
	}
	
}
