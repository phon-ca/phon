package ca.phon.app.session.editor.view.transcript;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.view.transcript.extensions.SyllabificationExtension;
import ca.phon.session.SystemTierType;
import ca.phon.session.position.TranscriptElementLocation;

import javax.swing.*;

/**
 * Hook for actions to be performed when the caret changes
 */
public class TierChangeCaretHook extends TranscriptEditorCaretHookAdapter {

    private TranscriptEditor editor;

    private String savedTierName = null;

    private TranscriptElementLocation gotoLocation = null;

    public TierChangeCaretHook(TranscriptEditor editor) {
        this.editor = editor;
        editor.getEventManager().registerActionForEvent(EditorEventType.TierChange, this::tierChanged);
    }

    @Override
    public boolean beforeSetDot(int oldDot, int newDot) {
        final TranscriptElementLocation oldLocation = editor.charPosToSessionLocation(oldDot);
        final TranscriptElementLocation newLocation = editor.charPosToSessionLocation(newDot);
        if(oldLocation.transcriptElementIndex() < 0) return true;
        if(oldLocation.tier() == null) return true;
        if(oldLocation.tier().equals(newLocation.tier())) return true;
        if(editor.tierHasUncommittedChanges(oldDot)) {
            editor.commitChanges(oldDot);
            this.savedTierName = oldLocation.tier();
            this.gotoLocation = newLocation;
            return false;
        }
        return true;
    }

    private void tierChanged(EditorEvent<EditorEventType.TierChangeData> ee) {
        if(this.gotoLocation == null) return;
        if(this.savedTierName == null) return;
//        if(!this.savedTierName.equals(ee.data().tier())) return;
        final int newDot = editor.sessionLocationToCharPos(gotoLocation);
        if(newDot >= 0) {
            SwingUtilities.invokeLater(() -> {
                editor.getTranscriptEditorCaret().setDot(newDot, true);
            });
        }
        this.gotoLocation = null;
        this.savedTierName = null;
    }

}
