package ca.phon.project;

import ca.phon.session.Session;

import java.io.IOException;
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

    /**
     * Returns the number of records in a session w/o opening
     * the session. This method is faster than using
     * openSession(corpus, session).numberOfRecords()
     *
     * @param session
     * @return number of records in the session
     * @throws IOException
     */
    public int numberOfRecordsInSession(String corpus, String session)
            throws IOException;

}
