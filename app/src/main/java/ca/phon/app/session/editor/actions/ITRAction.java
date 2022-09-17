package ca.phon.app.session.editor.actions;

import ca.phon.app.session.editor.*;

import java.awt.event.ActionEvent;

public class ITRAction extends SessionEditorAction {

    private final static String TITLE = ITRWizard.TITLE;
    private final static String DESC = "Show " + ITRWizard.TITLE + " wizard";

    public ITRAction(SessionEditor editor) {
        super(editor);

        putValue(NAME, TITLE);
        putValue(SHORT_DESCRIPTION, DESC);
    }

    @Override
    public void hookableActionPerformed(ActionEvent ae) {
        final ITRWizard wizard = new ITRWizard(getEditor().getProject(), getEditor().getSession());
        wizard.pack();
        wizard.setSize(1024, 768);
        wizard.centerWindow();
        wizard.setVisible(true);
    }

}
