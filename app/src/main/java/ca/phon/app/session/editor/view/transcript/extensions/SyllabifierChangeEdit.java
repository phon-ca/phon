package ca.phon.app.session.editor.view.transcript.extensions;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.undo.SessionUndoableEdit;
import ca.phon.session.Session;
import ca.phon.session.SyllabifierOptions;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.util.Language;

public class SyllabifierChangeEdit extends SessionUndoableEdit {

    private final String tierName;

    private final String syllabifierName;

    private final String oldSyllabifierName;

    /**
     * Constructor
     *
     * @param session
     * @param editorEventManager
     */
    public SyllabifierChangeEdit(Session session, EditorEventManager editorEventManager, String tierName, String syllabifierName) {
        super(session, editorEventManager);
        this.tierName = tierName;
        this.syllabifierName = syllabifierName;
        this.oldSyllabifierName = SyllabifierOptions.getSyllabifierForTier(session, tierName);
    }

    @Override
    public void doIt() {
        SyllabifierOptions.setSyllabifierForTier(getSession(), tierName, syllabifierName);

        final EditorEvent<EditorEventType.SyllabifierChangeData> event = new EditorEvent<EditorEventType.SyllabifierChangeData>(
                EditorEventType.SyllabifierChange,
                getSource(),
                new EditorEventType.SyllabifierChangeData(tierName, syllabifierName == null ? null : Language.parseLanguage(syllabifierName)));
        getEditorEventManager().queueEvent(event);
    }

    @Override
    public void undo() {
        SyllabifierOptions.setSyllabifierForTier(getSession(), tierName, oldSyllabifierName);

        final EditorEvent<EditorEventType.SyllabifierChangeData> event = new EditorEvent<EditorEventType.SyllabifierChangeData>(
                EditorEventType.SyllabifierChange,
                getSource(),
                new EditorEventType.SyllabifierChangeData(tierName, oldSyllabifierName == null ? null : Language.parseLanguage(oldSyllabifierName)));
        getEditorEventManager().queueEvent(event);
    }

    @Override
    public String getPresentationName() {
        return "Change syllabifier for tier '" + tierName + "'";
    }

}
