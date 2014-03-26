package ca.phon.app.session.editor.view.record_data.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.undo.SplitGroupEdit;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.session.Record;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class SplitGroupCommand extends RecordDataEditorViewAction {
	
	private final static String ICON = "actions/group_split";
	
	private int wordIndex = -1;

	public SplitGroupCommand(RecordDataEditorView editor) {
		super(editor);
		
		putValue(NAME, "Split group");
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.ALT_MASK));
	}
	
	public int getWordIndex() {
		return wordIndex;
	}

	public void setWordIndex(int wordIndex) {
		this.wordIndex = wordIndex;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Record r = getRecord();
		if(r == null) return;
		
		final int grp = getIndex();
		
		final SplitGroupEdit edit = new SplitGroupEdit(getEditorView().getEditor(), r, grp, getWordIndex());
		getEditorView().getEditor().getUndoSupport().postEdit(edit);
	}

}
