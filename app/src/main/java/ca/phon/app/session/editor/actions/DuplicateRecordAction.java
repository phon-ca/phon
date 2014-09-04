package ca.phon.app.session.editor.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddRecordEdit;
import ca.phon.session.Record;
import ca.phon.session.SessionFactory;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class DuplicateRecordAction extends SessionEditorAction {

	private static final long serialVersionUID = 8927870935233124123L;
	
	private final static String CMD_NAME = "Duplicate record";
	
	private final static String SHORT_DESC = "Duplicate record after current";
	
	private final static String ICON = "misc/record-duplicate";
	
	private final static KeyStroke KS = KeyStroke.getKeyStroke(
			KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

	public DuplicateRecordAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(ACCELERATOR_KEY, KS);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		final SessionFactory factory = SessionFactory.newFactory();
		final Record r = getEditor().currentRecord();
		if(r != null) {
			final Record dup = factory.cloneRecord(r);
			final AddRecordEdit edit = new AddRecordEdit(getEditor(), dup, getEditor().getCurrentRecordIndex()+1);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}

}
