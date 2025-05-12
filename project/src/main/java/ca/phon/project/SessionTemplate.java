package ca.phon.project;

import ca.phon.session.Session;

import java.io.IOException;

/**
 * Interface for session template extension for {@link Project}s
 *
 * This interface is used to define a session template for corpora.
 * Session templates are used to define the initial state of a session
 * when a new session is created for a corpus.
 */
public interface SessionTemplate {

    /**
     * Get the Session template for the given corpus.
     *
     * @param corpus
     *
     * @return session template or <code>null</code> if not found
     * @throws IOException
     */
    public Session getSessionTemplate(String corpus)
            throws IOException;

    /**
     * Save the Session template for the given corpus.
     *
     * @param corpus
     * @param template
     *
     * @throws IOException
     */
    public void saveSessionTemplate(String corpus, Session template)
            throws IOException;

    /**
     * Create a new session from the corpus template (if it exists)
     * This method will also add the session to the specified corpus.
     *
     * @param corpus
     * @param session
     *
     * @return new Session object
     */
    public Session createSessionFromTemplate(String corpus, String session)
            throws IOException;

}
