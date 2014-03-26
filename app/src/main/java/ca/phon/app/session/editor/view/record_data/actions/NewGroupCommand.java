package ca.phon.app.session.editor.view.record_data.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.undo.AddGroupEdit;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.session.Record;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Action for creating a new group in a record.  This command will
 * create a group <i>after</i> the current group.
 */
public class NewGroupCommand extends RecordDataEditorViewAction {
	
	private static final long serialVersionUID = 4424323795875330824L;
	
	private static final String CMD_NAME = "New group after current";
	
	private final static String ICON = "actions/group_add";
	
	public NewGroupCommand(RecordDataEditorView editor) {
		super(editor);
		
		putValue(NAME, CMD_NAME);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}
	
	public NewGroupCommand(RecordDataEditorView editor, Record record, int index) {
		this(editor);
		setRecord(record);
		setIndex(index);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final AddGroupEdit edit = new AddGroupEdit(getEditorView().getEditor(), getRecord(), getIndex()+1);
		super.getEditorView().getEditor().getUndoSupport().postEdit(edit);
	}
	
}
