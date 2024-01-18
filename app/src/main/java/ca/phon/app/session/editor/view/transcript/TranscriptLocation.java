package ca.phon.app.session.editor.view.transcript;

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

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(obj == null || !(obj instanceof TranscriptLocation)) return false;

        TranscriptLocation loc = (TranscriptLocation)obj;
        return (loc.elementIndex == elementIndex && loc.posInTier == posInTier && loc.label.equals(label));
    }

}
