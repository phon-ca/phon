package ca.phon.app.session.editor.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.DeleteRecordEdit;

/**
 * Delete the current record.
 */
public class DeleteRecordAction extends SessionEditorAction {

	private static final long serialVersionUID = -6995854542145591135L;
	
	private final static String CMD_NAME = "Delete record";
	
	private final static String SHORT_DESC = "Delete current record";
	
	private final static String ICON = "";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

	public DeleteRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final DeleteRecordEdit edit = new DeleteRecordEdit(getEditor());
		getEditor().getUndoSupport().postEdit(edit);
	}

}
