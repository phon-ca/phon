package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Session;

import javax.swing.undo.CannotUndoException;
import java.util.Map;

public class EditSessionMetadata extends SessionUndoableEdit {

    private final Map<String, String> metadata;

    private final Map<String, String> oldMetadata;

    /**
     *
     */
    public EditSessionMetadata(SessionEditor editor, Map<String, String> metadata) {
        this(editor.getSession(), editor.getEventManager(), metadata);
    }

    /**
     * Constructor
     *
     * @param session
     * @param editorEventManager
     */
    public EditSessionMetadata(Session session, EditorEventManager editorEventManager, Map<String, String> metadata) {
        super(session, editorEventManager);
        this.oldMetadata = Map.copyOf(session.getMetadata());
        this.metadata = metadata;
    }

    @Override
    public void undo() throws CannotUndoException {
        getSession().getMetadata().clear();
        getSession().getMetadata().putAll(this.oldMetadata);

        final EditorEvent<EditorEventType.SessionMetadataChangedData> ee =
                new EditorEvent<>(EditorEventType.SessionMetadataChanged, getSource(),
                        new EditorEventType.SessionMetadataChangedData(this.metadata, this.oldMetadata));
        getEditorEventManager().queueEvent(ee);
    }

    @Override
    public void doIt() {
        getSession().getMetadata().clear();
        getSession().getMetadata().putAll(this.metadata);

        final EditorEvent<EditorEventType.SessionMetadataChangedData> ee =
                new EditorEvent<>(EditorEventType.SessionMetadataChanged, getSource(),
                        new EditorEventType.SessionMetadataChangedData(this.oldMetadata, this.metadata));
        getEditorEventManager().queueEvent(ee);
    }

}
