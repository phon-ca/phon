package ca.phon.project;

import ca.phon.session.Session;

import java.time.ZonedDateTime;

/**
 * Interface for session details extension for {@link Project}s
 */
public interface SessionDetails {

    /**
     * Returns the modification date for the given session
     *
     * @param session
     *
     * @return session modification date in system time zone
     */
    public ZonedDateTime getSessionModificationTime(Session session);

    /**
     * Returns the modification date for the specified session.
     *
     * @param corpus
     * @param session
     *
     * @return session modification date in system time zone
     */
    public ZonedDateTime getSessionModificationTime(String corpus, String session);

    /**
     * Returns the size on disk for the given session.
     *
     * @param session
     *
     * @return session size in bytes
     */
    public long getSessionByteSize(Session session);

    /**
     * Returns the size on disk for the given session.
     *
     * @param corpus
     * @param session
     *
     * @return session size in bytes
     */
    public long getSessionByteSize(String corpus, String session);

}
