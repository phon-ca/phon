package ca.phon.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import ca.phon.extensions.IExtendable;
import ca.phon.session.Session;

/**
 * Interface for a phon project.
 * Projects are responsible for corpus and session
 * lists as well as managing serialization for
 * sessions.
 * 
 */
public interface Project extends IExtendable {

	/**
	 * The name of the project.
	 * 
	 * @return project name
	 */
	public String getName();
	
	/**
	 * Set project name
	 * 
	 * @param name must match pattern '[ \w\d-]+'
	 */
	public void setName(String name);
	
	/**
	 * Project UUID
	 * 
	 * UUID for the project
	 * 
	 * @return uuid
	 */
	public UUID getUUID();
	
	/**
	 * Set project UUID
	 * 
	 * @param uuid
	 */
	public void setUUID(UUID uuid);
	
	/**
	 * Get the list of corpora in this project.  Corpus names
	 * are returned in alphabetical order.
	 * 
	 * @return list of corpora
	 */
	public List<String> getCorpora();
	
	/**
	 * Add a new corpus with the specified name.
	 * 
	 * @param corpus
	 * @param description
	 * @throws IOException if the corpus could not be
	 *  created
	 */
	public void addCorpus(String name, String description)
		throws IOException;
	
	/**
	 * Rename a corpus
	 * 
	 * @param corpus
	 * @param newName
	 * 
	 * @throws IOException if the corpus could not be
	 *  renamed
	 */
	public void reameCorpus(String corpus, String newName) 
		throws IOException;
	
	/**
	 * Delete the specified corpus and all sessions it contains.
	 * 
	 * @param corpus
	 * 
	 * @throws IOException if the corpus could not be deleted
	 */
	public void removeCorpus(String corpus)
		throws IOException;
	
	/**
	 * Get the description of the specified corpus.
	 * 
	 * @param corpus the corpus name
	 */
	public String getCorpusDescription(String corpus);
	
	/**
	 * Set the description for the specified corpus.
	 * 
	 * @param corpus
	 * @param description
	 */
	public void setCorpusDescription(String corpus, String description);
	
	/**
	 * Get the session names contained in a corpus in alphabetical
	 * order.
	 * 
	 * @param corpus
	 * 
	 * @return the list of sessions in the specified corpus
	 */
	public List<String> getCorpusSessions(String corpus);
	
	/**
	 * Open the specified session.  This will create a new session
	 * object with the data currently on the storage device.
	 * 
	 * @param corpus
	 * @param session
	 * 
	 * @return the session
	 * 
	 * @throws IOException
	 */
	public Session openSession(String corpus, String session)
		throws IOException;
	
	/**
	 * Get a write lock for a session.  Before writing a write lock
	 * must be obtained from the project.  
	 * 
	 * @param session
	 * 
	 * @return the session write lock or < 0 if a write lock
	 *  was not obtained
	 */
	public UUID getSessionWriteLock(Session session);
	
	/**
	 * Get a write lock for a session.  Before writing a write lock
	 * must be obtained from the project.  
	 * 
	 * @param corpus
	 * @param session
	 * 
	 * @return the session write lock or <code>null</code>
	 */
	public UUID getSessionWriteLock(String corpus, String session);
	
	/**
	 * Release the write lock for a session.
	 * 
	 * @param session
	 * @param writeLock
	 */
	public void releaseSessionWriteLock(Session session, UUID writeLock);
	
	/**
	 * Release the write lock for a session.
	 * 
	 * @param session
	 * @param writeLock
	 */
	public void releaseSessionWriteLock(String corpus, String session, UUID writeLock);
	
	/**
	 * Save a session
	 * 
	 * @param session
	 * @param writeLock
	 * 
	 * @throws IOException
	 */
	public void saveSession(Session session, UUID writeLock)
		throws IOException;
	
	/**
	 * Save a session to the specified corpus and new
	 * sessionName.
	 * 
	 * @param corpus
	 * @param sessionName
	 * @param session
	 * @param writeLock
	 * 
	 * @throws IOException
	 */
	public void saveSession(String corpus, String sessionName, Session session, UUID writeLock)
		throws IOException;
	
	/**
	 * Remove a session from the project.  The writeLock 
	 * for the session is also released.
	 * 
	 * @param session
	 * @param writeLock
	 * 
	 * @throws IOException
	 */
	public void removeSession(Session sesion, UUID writeLock)
		throws IOException;
	
	/**
	 * Remove a session from the project.  The writeLock 
	 * for the session is also released.
	 * 
	 * @parma corpus
	 * @param session
	 * @param writeLock
	 * 
	 * @throws IOException
	 */
	public void removeSession(String corpus, String sesion, UUID writeLock)
		throws IOException;
	
	/**
	 * Add a project listener.
	 * 
	 * @param listener
	 */
	public void addProjectListener(ProjectListener listener);
	
	/**
	 * Remove a project listener.
	 * 
	 * @param listener
	 */
	public void removeProjectListener(ProjectListener listener);
	
	/**
	 * Get the (immutable) list of project listeners.
	 * 
	 * @return project listeners
	 */
	public List<ProjectListener> getProjectListeners();
	
	/**
	 * Fire a project event to all listeners
	 * 
	 * @param event
	 */
	public void fireProjectEvent(ProjectEvent event);
	
	/**
	 * Get an input stream for the specified project resource.
	 * The resource name should be a relative path including filename.
	 * E.g., 'ca.phon.myplugin/module/corpus/session.dat'
	 * 
	 * @param resourceName
	 * 
	 * @return an input stream for the specified resource
	 * 
	 * @throws IOException
	 */
	public InputStream getResourceInputStream(String resourceName)
		throws IOException;
	
	/**
	 * Get an output stream for the specified resource.  If the resource
	 * does not exist it is created.  If the resource already exists
	 * it is overwritten.
	 * 
	 * @param resourceName
	 * 
	 * @return output stream for the specified resource
	 * 
	 * @throws IOException
	 */
	public OutputStream getResourceOutputStream(String resourceName)
		throws IOException;
}
