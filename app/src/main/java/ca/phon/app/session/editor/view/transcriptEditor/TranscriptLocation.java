package ca.phon.app.session.editor.view.transcriptEditor;

/**
 * Represents a location in a transcript document.
 *
 * @param elementIndex
 * @param label
 * @param posInTier
 */
public final record TranscriptLocation(int elementIndex, String label, int posInTier) {

    @Override
    public String toString() {
        return "TranscriptLocation{" +
                "elementIndex=" + elementIndex +
                ", posInTier=" + posInTier +
                ", label='" + label + '\'' +
                '}';
    }

}
