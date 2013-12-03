package ca.phon.syllabifier;

import java.util.ServiceLoader;

import ca.phon.util.resources.ResourceHandler;

/**
 * Interface used by {@link ServiceLoader} to automatically
 * find resource handlers for syllabifiers.
 */
public interface SyllabifierProvider extends ResourceHandler<Syllabifier> {

}
