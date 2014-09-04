package ca.phon.app.session.editor.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddRecordEdit;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Add a new record to the current session.  The record
 * is placed <em>after</em> the current record.
 */
public class NewRecordAction extends SessionEditorAction {
	
	private static final long serialVersionUID = 8872975443044409650L;

	private final static String CMD_NAME = "New record";
	
	private final static String SHORT_DESC = "New record after current";
	
	private final static String ICON = "misc/record-add";
	
	private final static KeyStroke KS = KeyStroke.getKeyStroke(
			KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	
	public NewRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(ACCELERATOR_KEY, KS);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final AddRecordEdit edit = new AddRecordEdit(getEditor());
		getEditor().getUndoSupport().postEdit(edit);
	}

}
