package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.SessionToHTMLWizard;
import ca.phon.app.session.editor.SessionEditor;

public class ExportAsHTMLAction extends SessionEditorAction {

	public ExportAsHTMLAction(SessionEditor editor) {
		super(editor);
		
		putValue(SessionEditorAction.NAME, "Export as HTML...");
		putValue(SessionEditorAction.SHORT_DESCRIPTION, "Export session as HTML");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final SessionToHTMLWizard wizard = new SessionToHTMLWizard("Export as HTML", getEditor().getSession());
		wizard.pack();
		wizard.setSize(1024, 768);
		wizard.centerWindow();
		wizard.setParentFrame(getEditor());
		wizard.setVisible(true);
	}

}
