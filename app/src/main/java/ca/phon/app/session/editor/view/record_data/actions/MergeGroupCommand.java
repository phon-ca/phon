package ca.phon.app.session.editor.view.record_data.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import ca.phon.app.session.editor.undo.MergeGroupEdit;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.session.Record;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Action for merging two groups in a record.  This action will
 * merge the specified group with the one after it (if found)
 */
public class MergeGroupCommand extends RecordDataEditorViewAction {
	
	private static final long serialVersionUID = -2409569868503667376L;

	private final static String ICON = "actions/group_merge";

	private final RecordDataEditorView editor;
	
	public MergeGroupCommand(RecordDataEditorView editor) {
		super(editor);
		this.editor = editor;

		putValue(NAME, "Merge group with next");
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.ALT_MASK));
	}

	public MergeGroupCommand(RecordDataEditorView editor, Record record, int index) {
		this(editor);
		
		setRecord(record);
		setIndex(index);
//		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Record r = getRecord();
		if(r == null) return;
		int idx = getIndex();
		if(idx < r.numberOfGroups() - 1) {
			final MergeGroupEdit edit = new MergeGroupEdit(getEditorView().getEditor(), r, idx);
			getEditorView().getEditor().getUndoSupport().postEdit(edit);
		}
	}
	
}
