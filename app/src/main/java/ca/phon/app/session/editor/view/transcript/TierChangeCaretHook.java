package ca.phon.app.session.editor.view.transcript;

import ca.phon.app.log.LogUtil;
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
        this.editor.addTierChangeListener(this::tierChanged);
    }

    @Override
    public boolean beforeSetDot(int oldDot, int newDot) {
        final TranscriptElementLocation oldLocation = editor.charPosToSessionLocation(oldDot);
        final TranscriptElementLocation newLocation = editor.charPosToSessionLocation(newDot);
        if(oldLocation.transcriptElementIndex() < 0) return true;
        if(oldLocation.tier() == null) return true;
        if(oldLocation.tier().equals(newLocation.tier())) return true;
        if(editor.tierHasUncommittedChanges(oldDot)) {
            this.savedTierName = oldLocation.tier();
            this.gotoLocation = newLocation;
            editor.commitChanges(oldDot);
            return false;
        }
        return true;
    }

    private void tierChanged(String tierName, Object oldValue, Object newValue) {
        if(this.gotoLocation == null) return;
        if(this.savedTierName == null) return;
        if(!this.savedTierName.equals(tierName)) return;
        SwingUtilities.invokeLater(() -> {
            final int newDot = editor.sessionLocationToCharPos(gotoLocation);
            this.gotoLocation = null;
            this.savedTierName = null;
            editor.getTranscriptEditorCaret().setDot(newDot);
        });
    }

}
