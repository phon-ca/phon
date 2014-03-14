package ca.phon.app.menu.edit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.undo.UndoManager;

/**
 * Undo command template.  Requires a {@link UndoManager} be provided.
 *
 */
public class UndoCommand extends AbstractAction {

	private static final long serialVersionUID = 9100941568698565601L;

	private UndoManager undoManager;
	
	private final static String PREFIX = "Undo";
	
	public UndoCommand(UndoManager manager) {
		super();
		putValue(NAME, PREFIX);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		undoManager.undo();
	}
	
}
