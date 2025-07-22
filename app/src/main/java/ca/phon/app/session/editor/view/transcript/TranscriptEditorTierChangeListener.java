package ca.phon.app.session.editor.view.transcript;

@FunctionalInterface
public interface TranscriptEditorTierChangeListener {

    /**
     * Called when a tier (including comments and gems) is changed in the transcript editor.
     * This method is invoked after the changes have been committed to the {@link TranscriptDocument}.
     *
     * @param elementIndex the index of the element in the transcript
     * @param tierName the name of the tier that changed
     * @param oldValue the old value of the tier
     * @param newValue the new value of the tier
     */
    void tierChanged(int elementIndex, String tierName, Object oldValue, Object newValue);

}
