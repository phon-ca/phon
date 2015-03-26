package ca.phon.app.session.editor.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.DeleteRecordEdit;

public class CutRecordAction extends CopyRecordAction {

	private static final long serialVersionUID = 6386824370678974804L;
	
	private static final String CMD_NAME = "Cut record";
	
	private static final String SHORT_DESC = "Copy record to clipboard and remove record";
	
	private static final String ICON = "";
	
	private static final KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_X,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK);

	public CutRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		super.hookableActionPerformed(e);
		
		final DeleteRecordEdit edit = new DeleteRecordEdit(getEditor());
		getEditor().getUndoSupport().postEdit(edit);
	}

}
