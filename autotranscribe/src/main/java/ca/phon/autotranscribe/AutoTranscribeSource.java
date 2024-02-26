package ca.phon.autotranscribe;

/**
 * Source for automatic transcriptions
 */
@FunctionalInterface
public interface AutoTranscribeSource {

    /**
     * Return a set of possible transcriptions for the given text.
     *
     * @param text
     * @return a set of possible transcriptions or an empty set if no transcriptions are available
     */
    public String[] lookup(String text);

}
