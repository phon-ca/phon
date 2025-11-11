package ca.phon.project;

import ca.phon.session.Session;
import ca.phon.session.io.SessionWriter;

import java.io.IOException;
import java.util.UUID;

/**
 * Interface for Project mutability. This is an extension for Projects which allow
 * modification of the project data.
 */
public interface MutableProject {

    /**
     * Set project name
     *
     * @param name must match pattern '[ \w\d-]+'
     */
    void setName(String name);

    /**
     * Set project UUID
     *
     * @param uuid UUID for the project
     */
    void setUUID(UUID uuid);

    /**
     * Add corpus folder with given name
     *
     * @param name the name of the corpus
     * @throws IOException
     */
    void addCorpus(String name) throws IOException;

    /**
     * Add a new corpus with the specified name.
     *
     * @param name the name of the corpus
     * @param description the description of the corpus
     * @throws IOException if the corpus could not be
     *  created
     */
    void addCorpus(String name, String description)
            throws IOException;

    /**
     * Rename a corpus
     *
     * @param corpus the corpus to rename
     * @param newName the new name for the corpus
     *
     * @throws IOException if the corpus could not be
     *  renamed
     */
    void renameCorpus(String corpus, String newName)
            throws IOException;

    /**
     * Delete the specified corpus and all sessions it contains.
     *
     * @param corpus the corpus to delete
     *
     * @throws IOException if the corpus could not be deleted
     */
    void removeCorpus(String corpus)
            throws IOException;

    /**
     * Set the description for the specified corpus.
     *
     * @param corpus the corpus name
     * @param description the description of the corpus
     */
    void setCorpusDescription(String corpus, String description);

    /**
     * Get a write lock for a session.  Before writing a write lock
     * must be obtained from the project.
     *
     * Example:
     * <pre>
     *     try(SessionWriteLock writeLock = project.getSessionWriteLock(session)) {
     *      // do some work with the session
     *     } catch (IOException e) {
     *      // handle exception
     *     }
     * </pre>
     *
     * @param session the session to lock
     *
     * @return the session write lock or &lt; 0 if a write lock
     *  was not obtained
     * @throws IOException
     */
    SessionWriteLock getSessionWriteLock(Session session)
            throws IOException;

    /**
     * Get a write lock for a session.  Before writing a write lock
     * must be obtained from the project.
     *
     * See {@link #getSessionWriteLock(Session)} for more
     * information on how to use the write lock.
     *
     * @param corpus the corpus name
     * @param session the session to lock
     *
     * @return the session write lock or <code>null</code>
     * @throws IOException
     */
    SessionWriteLock getSessionWriteLock(String corpus, String session)
            throws IOException;

    /**
     * Release the write lock for a session.
     *
     * See {@link #getSessionWriteLock(Session)} for more
     * information on how to use the write lock.
     *
     * @param session the session to unlock
     * @param writeLock the write lock to release
     *
     * @throws IOException
     */
    void releaseSessionWriteLock(Session session, SessionWriteLock writeLock)
            throws IOException;

    /**
     * Release the write lock for a session.
     *
     * See {@link #releaseSessionWriteLock(Session, SessionWriteLock)} for more
     * information on how to use the write lock.
     *
     * @param session the session to unlock
     * @param writeLock the write lock to release
     *
     * @throws IOException
     */
    void releaseSessionWriteLock(String corpus, String session, SessionWriteLock writeLock)
            throws IOException;

    /**
     * Tells whether the given session is locked
     *
     * @param session the session to check
     * @return <code>true</code> if session is locked, <code>false</code>
     *  otherwise
     */
    boolean isSessionLocked(Session session);

    /**
     * Tells wheater the given session is locked
     *
     * @param corpus the corpus name
     * @param session the session name
     *
     * @return <code>true</code> if the session is locked, <code>false</code>
     *  otherwise
     */
    boolean isSessionLocked(String corpus, String session);

    /**
     * Save a session with the provided writeLock.
     *
     * Example:
     * <pre>
     *     try(SessionWriteLock writeLock = project.getSessionWriteLock(session)) {
     *      project.saveSession(session, writeLock);
     *     } catch (IOException e) {
     *      // handle exception
     *     }
 *     </pre>
     *
     * The write lock is auto-closeable and will be released
     * when the try block is exited.  To manually release the
     * write lock, call the {@link #releaseSessionWriteLock(Session, SessionWriteLock)}
     * method or use the {@code close()} method on the write lock.
     *
     * @param session the session to save
     * @param writeLock the write lock for the session
     *
     * @throws IOException
     */
    void saveSession(Session session, SessionWriteLock writeLock)
            throws IOException;

    /**
     * Save a session to the specified corpus and new
     * sessionName.  See {@link #saveSession(Session, SessionWriteLock)}
     * for more information on how to use the write lock.
     *
     * @param corpus the corpus name
     * @param sessionName the name of the session
     * @param session the session to save
     * @param writeLock the write lock for the session
     *
     * @throws IOException
     */
    void saveSession(String corpus, String sessionName, Session session, SessionWriteLock writeLock)
            throws IOException;

    /**
     * Save a session writing the file using the given writer.
     * See {@link #saveSession(Session, SessionWriteLock)} for more
     * information on how to use the write lock.
     *
     * @param corpus the corpus name
     * @param sessionName the name of the session
     * @param session the session to save
     * @param writer the session writer to use
     * @param writeLock the write lock for the session
     *
     * @throws IOException
     */
    void saveSession(String corpus, String sessionName, Session session, SessionWriter writer, SessionWriteLock writeLock)
            throws IOException;

    /**
     * Remove a session from the project.  The writeLock
     * for the session is also released.
     *
     * See {@link #removeSession(Session, SessionWriteLock)} for more
     * information on how to use the write lock.
     *
     * @param session the session to remove
     * @param writeLock the write lock for the session
     *
     * @throws IOException
     */
    void removeSession(Session session, SessionWriteLock writeLock)
            throws IOException;

    /**
     * Remove a session from the project.  The writeLock
     * for the session is also released.
     *
     * See {@link #removeSession(Session, SessionWriteLock)} for more
     * information on how to use the write lock.
     *
     * @param corpus
     * @param session the session to remove
     * @param writeLock the write lock for the session
     *
     * @throws IOException
     */
    void removeSession(String corpus, String session, SessionWriteLock writeLock)
            throws IOException;
}
