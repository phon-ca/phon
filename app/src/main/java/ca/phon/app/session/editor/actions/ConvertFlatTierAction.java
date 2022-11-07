package ca.phon.app.session.editor.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.ConvertFlatTierEdit;

import java.awt.event.ActionEvent;

public class ConvertFlatTierAction extends SessionEditorAction {

	public final String TXT = "Convert to group tier";

	public final String DESC = "Convert flat tier to group tier";

	final String tierName;

	public ConvertFlatTierAction(SessionEditor editor, String tierName) {
		super(editor);

		this.tierName = tierName;

		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final ConvertFlatTierEdit edit = new ConvertFlatTierEdit(getEditor(), tierName);
		getEditor().getUndoSupport().postEdit(edit);
	}

}
