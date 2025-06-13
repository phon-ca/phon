package ca.phon.project;

import java.util.UUID;

/**
 * Lock for writing to a session.  This is used to prevent
 * multiple threads from writing to the same session at the
 * same time.
 */
public final class SessionWriteLock implements AutoCloseable {

    private final MutableProject project;

    private final UUID writeLockId;

    private final String corpus;

    private final String session;

    SessionWriteLock(MutableProject project, String corpus, String session, UUID writeLockId) {
        this.project = project;
        this.corpus = corpus;
        this.session = session;
        this.writeLockId = writeLockId;
    }

    public MutableProject getProject() {
        return project;
    }

    public UUID getWriteLockId() {
        return writeLockId;
    }

    public String getCorpus() {
        return corpus;
    }

    public String getSession() {
        return session;
    }

    @Override
    public void close() throws Exception {
        project.releaseSessionWriteLock(corpus, session, this);
    }

    @Override
    public String toString() {
        return "SessionWriteLock [corpus=" + corpus + ", session=" + session + ", writeLockId=" + writeLockId + "]";
    }

}
