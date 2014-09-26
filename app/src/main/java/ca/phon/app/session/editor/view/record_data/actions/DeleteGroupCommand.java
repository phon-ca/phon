package ca.phon.app.session.editor.view.record_data.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.undo.RemoveGroupEdit;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Removes specified group for all tiers.
 */
public class DeleteGroupCommand extends RecordDataEditorViewAction {
	
	private static final long serialVersionUID = 7588345374191473003L;

	private final static String CMD_NAME = "Delete current group";
	
	private final static String ICON = "actions/group_remove";
	
	public DeleteGroupCommand(RecordDataEditorView editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK | KeyEvent.ALT_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// don't delete last group!
		if(getRecord().numberOfGroups() == 1) return;
		final RemoveGroupEdit edit = new RemoveGroupEdit(getEditorView().getEditor(), getRecord(), getIndex());
		getEditorView().getEditor().getUndoSupport().postEdit(edit);
	}
	
}
