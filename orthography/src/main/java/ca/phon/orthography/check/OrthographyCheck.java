package ca.phon.orthography.check;

import ca.phon.orthography.Orthography;

/**
 * Interface for semantic check of Orthoghraphy
 *
 */
@FunctionalInterface
public interface OrthographyCheck {

    public void check(Orthography orthography, OrthographyCheckHandler handler);

}
