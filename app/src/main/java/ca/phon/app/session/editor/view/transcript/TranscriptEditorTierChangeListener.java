package ca.phon.app.session.editor.view.transcript;

@FunctionalInterface
public interface TranscriptEditorTierChangeListener {

    /**
     * Called when the tier changes.
     *
     * @param tierName the name of the tier that changed
     * @param oldValue the old value of the tier
     * @param newValue the new value of the tier
     */
    void tierChanged(String tierName, Object oldValue, Object newValue);

}
