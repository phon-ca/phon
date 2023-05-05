package ca.phon.orthography.check;

import ca.phon.orthography.Orthography;

/**
 * Interface for OrthographhyCheck handlers which will be
 * executed when an issue is encountered.
 *
 */
@FunctionalInterface
public interface OrthographyCheckHandler {

    public void reportIssue(Orthography orthography, String message, int orthoIdx);

}
