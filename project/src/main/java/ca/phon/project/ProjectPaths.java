package ca.phon.project;

import ca.phon.session.Session;

/**
 * Interface providing project paths.  This is used to provide direct access project location
 * information.
 */
public interface ProjectPaths {

    /**
     * The location of the project.  Meaning is dependent on implementation.
     * For {@link LocalProject}s this is the path to the project on disk.
     *
     * @return the project location
     */
    String getLocation();

    /**
     * Return the path to the given corpus.
     *
     * @param corpus the corpus name
     *
     * @return the path to the corpus, usually relative to the project
     */
    String getCorpusPath(String corpus);

    /**
     * Set path of corpus.
     *
     * @param corpus the corpus name
     * @param path the new path for the corpus
     *
     * @throws UnsupportedOperationException if the project does not support
     * changing corpus paths
     */
    void setCorpusPath(String corpus, String path);

    /**
     * Get path to the given session.
     *
     * @param session
     *
     * @return path to given session
     */
    String getSessionPath(Session session);

    /**
     * Get path to the given session.
     *
     * @param corpus
     * @param session
     *
     * @return path to given session
     */
    String getSessionPath(String corpus, String session);

}
