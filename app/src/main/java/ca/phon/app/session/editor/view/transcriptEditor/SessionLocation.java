package ca.phon.app.session.editor.view.transcriptEditor;

public class SessionLocation {
    private final int elementIndex;
    private final int posInTier;
    private final String label;

    public SessionLocation(int elementIndex, String label, int posInTier) {
        this.elementIndex = elementIndex;
        this.posInTier = posInTier;
        this.label = label;
    }

    // region Getters

    public int getElementIndex() {
        return elementIndex;
    }

    public int getPosInTier() {
        return posInTier;
    }

    public String getLabel() {
        return label;
    }

    // endregion Getters

    @Override
    public String toString() {
        return "SessionLocation{" +
                "elementIndex=" + elementIndex +
                ", posInTier=" + posInTier +
                ", label='" + label + '\'' +
                '}';
    }
}