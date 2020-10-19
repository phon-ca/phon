package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class FindAndReplaceAction extends SessionEditorAction {

	private static final long serialVersionUID = -548370051934852629L;

	private final static String TXT = "Find & Replace";
	
	private final static String DESC = "Show Record Data view with Find & Replace UI visible";
	
	private final static String ICON_NAME = "actions/edit-find-replace";
	
	public FindAndReplaceAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON_NAME, IconSize.SMALL));
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		SessionEditor editor = getEditor();
		if(!editor.getViewModel().isShowing(RecordDataEditorView.VIEW_NAME)) {
			editor.getViewModel().showView(RecordDataEditorView.VIEW_NAME);
		}
		((RecordDataEditorView)editor.getViewModel().getView(RecordDataEditorView.VIEW_NAME)).setFindAndReplaceVisible(true);
	}

}
