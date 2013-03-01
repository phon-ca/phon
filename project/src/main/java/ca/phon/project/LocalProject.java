package ca.phon.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.project.io.CorpusType;
import ca.phon.project.io.ObjectFactory;
import ca.phon.project.io.ProjectType;
import ca.phon.project.io.SessionType;
import ca.phon.session.Session;

/**
 * A local on-disk project
 * 
 */
public class LocalProject implements Project {
	
	/**
	 * Project folder
	 * 
	 */
	private final File projectFolder;
	
	/**
	 * project.xml data
	 */
	private ProjectType projectData;
	private final static String projectDataFile = "project.xml";
	
	/**
	 * Session write locks
	 */
	private final Map<String, UUID> sessionLocks = 
			Collections.synchronizedMap(new HashMap<String, UUID>());
	
	/**
	 * 
	 * @param url
	 */
	LocalProject(File projectFolder) {
		super();
		this.projectFolder = projectFolder;
		
		// load project data
		projectData = loadProjectData();
		final List<CorpusType> corpora = scanProjectFolder();
		projectData.getCorpus().clear();
		projectData.getCorpus().addAll(corpora);
	}
	
	/**
	 * Load project data from the project.xml file.
	 * If not found, empty project data is created.
	 * 
	 * @return projectData
	 */
	private ProjectType loadProjectData() {
		final ObjectFactory factory = new ObjectFactory();
		
		final File dataFile = new File(getFolder(), projectDataFile);
		
		if(dataFile.exists()) {
			try {
				final JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
				final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				
				final FileInputStream fin = new FileInputStream(dataFile);
				final XMLInputFactory inputFactory = XMLInputFactory.newFactory();
				final XMLEventReader eventReader = inputFactory.createXMLEventReader(fin);
				
				final JAXBElement<ProjectType> projectEle = 
						unmarshaller.unmarshal(eventReader, ProjectType.class);
				return projectEle.getValue();
			} catch (JAXBException jaxbEx) {
				jaxbEx.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}
		
		final ProjectType retVal = factory.createProjectType();
		final UUID projectUUID = UUID.randomUUID();
		retVal.setName(getFolder().getName());
		retVal.setUuid(projectUUID.toString());
		return retVal;
	}
	
	/**
	 * Save project data
	 * 
	 * @throws IOException
	 */
	private void saveProjectData() 
		throws IOException {
		final ObjectFactory factory = new ObjectFactory();
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			final Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			final File dataFile = new File(getFolder(), projectDataFile);
			
			final JAXBElement<ProjectType> projectDataEle = factory.createProject(getProjectData());
			marshaller.marshal(projectDataEle, dataFile);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * Scan the project folder and build the list of corpora/sessions
	 * available.  Local projects will always take the current list of
	 * corpora/sessions directly from the storage device.
	 * 
	 * @return list of corprora xml objects
	 */
	private List<CorpusType> scanProjectFolder() {
		final ObjectFactory factory = new ObjectFactory();
		final List<CorpusType> retVal = new ArrayList<CorpusType>();
		
		for(File f:getFolder().listFiles()) {
			if(f.isDirectory() 
					&& !f.getName().startsWith("~") 
					&& !f.getName().startsWith("__") 
					&& !f.isHidden()) {
				final String corpusName = f.getName();
				CorpusType ct = getCorpusInfo(corpusName);
				if(ct == null) {
					ct = factory.createCorpusType();
					ct.setName(corpusName);
					ct.setDescription("");
				}
				
				// look for all xml files inside corpus folder
				for(File xmlFile:f.listFiles()) {
					if(xmlFile.getName().endsWith(".xml")
							&& !f.getName().startsWith("~")
							&& !f.getName().startsWith("__")
							&& !f.isHidden()) {
						final String sessionName = f.getName().substring(0, f.getName().lastIndexOf('.'));
						
						SessionType sessionType = null;
						for(SessionType st:ct.getSession()) {
							if(st.getName().equals(sessionName)) {
								sessionType = st;
								break;
							}
						}
						
						if(sessionType == null) {
							sessionType = factory.createSessionType();
							sessionType.setName(sessionName);
							ct.getSession().add(sessionType);
						}
					}
				}
				
				retVal.add(ct);
			}
		}
		
		return retVal;
	}
	
	/**
	 * Get the current corpus xml object from project data.
	 * 
	 * @param corpus
	 * @return the corpus xml object or <code>null</code> if 
	 *  not found
	 */
	private CorpusType getCorpusInfo(String corpus) {
		final ProjectType projectData = getProjectData();
		for(CorpusType ct:projectData.getCorpus()) {
			if(ct.getName().equals(corpus)) {
				return ct;
			}
		}
		return null;
	}
	
	/**
	 * Get the current session xml object from project data.
	 * 
	 * @param corpus
	 * @param session
	 * @return the session xml object or <code>null</code> if
	 *  not found
	 */
	private SessionType getSessionInfo(String corpus, String session) {
		final CorpusType ct = getCorpusInfo(corpus);
		if(ct != null) {
			for(SessionType st:ct.getSession()) {
				if(st.getName().equals(session)) {
					return st;
				}
			}
		}
		return null;
	}
	
	private ProjectType getProjectData() {
		return this.projectData;
	}
	
	private File getFolder() {
		return this.projectFolder;
	}

	@Override
	public String getName() {
		return projectData.getName();
	}

	@Override
	public void setName(String name) {
		final String oldName = getName();
		projectData.setName(name);
		
		final ProjectEvent event = ProjectEvent.newNameChangedEvent(oldName, name);
		fireProjectEvent(event);
	}

	@Override
	public UUID getUUID() {
		final UUID uuid = UUID.fromString(getProjectData().getUuid());
		return uuid;
	}

	@Override
	public void setUUID(UUID uuid) {
		final UUID oldUUID = getUUID();
		projectData.setUuid(uuid.toString());
		
		final ProjectEvent event = ProjectEvent.newUUIDChangedEvent(oldUUID.toString(), uuid.toString());
		fireProjectEvent(event);
	}

	@Override
	public List<String> getCorpora() {
		final List<String> corpusList = new ArrayList<String>();
		
		for(CorpusType ct:getProjectData().getCorpus()) {
			corpusList.add(ct.getName());
		}
		
		return corpusList;
	}

	@Override
	public void addCorpus(String name, String description) throws IOException {
		final File corpusFolder = new File(getFolder(), name);
		if(corpusFolder.exists()) {
			throw new IOException("Corpus with name '" + name + "' already exists.");
		}
		
		if(!corpusFolder.mkdirs()) {
			throw new IOException("Unable to create corpus folder.");
		}
		
		final ObjectFactory factory = new ObjectFactory();
		CorpusType ct = getCorpusInfo(name);
		if(ct == null) {
			ct = factory.createCorpusType();
			ct.setName(name);
			getProjectData().getCorpus().add(ct);
		}
		ct.setDescription(description);
	}

	@Override
	public void reameCorpus(String corpus, String newName) throws IOException {
		// add new corpus first
		addCorpus(newName, getCorpusDescription(corpus));
		
		// copy sessions
		for(String sessionName:getCorpusSessions(corpus)) {
			final Session session = openSession(corpus, sessionName);
			
			final UUID writeLock = getSessionWriteLock(newName, sessionName);
			saveSession(newName, sessionName, session, writeLock);
			releaseSessionWriteLock(newName, sessionName, writeLock);
		}
		
		// remove old corpus
		removeCorpus(corpus);
	}

	@Override
	public void removeCorpus(String corpus) throws IOException {
		final File corpusFolder = new File(getFolder(), corpus);
		if(corpusFolder.exists()) {
			// attempt to delete all files in the folder
			for(File f:corpusFolder.listFiles()) {
				if(!f.delete()) {
					throw new IOException("Unable to remove file '" + f.getName() + "'");
				}
			}
			
			if(!corpusFolder.delete()) {
				throw new IOException("Unable to remove corpus folder.");
			}
		}
		
		// remove entry from project data
		final CorpusType ct = getCorpusInfo(corpus);
		if(ct != null) {
			getProjectData().getCorpus().remove(ct);
		}
	}

	@Override
	public String getCorpusDescription(String corpus) {
		final CorpusType ct = getCorpusInfo(corpus);
		return (ct == null ? "" : ct.getDescription());
	}

	@Override
	public void setCorpusDescription(String corpus, String description) {
		CorpusType ct = getCorpusInfo(corpus);
		if(ct == null) {
			ct = (new ObjectFactory()).createCorpusType();
			ct.setName(corpus);
			getProjectData().getCorpus().add(ct);
		}
		ct.setDescription(description);
	}

	@Override
	public List<String> getCorpusSessions(String corpus) {
		final List<String> retVal = new ArrayList<String>();
		
		final CorpusType ct = getCorpusInfo(corpus);
		if(ct != null) {
			for(SessionType st:ct.getSession()) {
				retVal.add(st.getName());
			}
		}
		
		return retVal;
	}
	
	private File getCorpusFolder(String corpus) {
		File retVal = new File(getFolder(), corpus);
		
		final CorpusType ct = getCorpusInfo(corpus);
		if(ct != null && ct.getLoc() != null) {
			try {
				final URI uri = new URI(ct.getLoc());
				if(uri.isAbsolute()) {
					retVal = new File(uri);
				} else {
					retVal = new File(getFolder(), uri.getPath());
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		
		return retVal;
	}
	
	private File getSessionFile(String corpus, String session) {
		File retVal = new File(getCorpusFolder(corpus), session);
		
		// check to see if session file path has been defined in xml
		final SessionType st = getSessionInfo(corpus,  session);
		if(st != null && st.getLoc() != null) {
			try {
				final URI uri = new URI(st.getLoc());
				
				if(uri.isAbsolute()) {
					retVal = new File(uri);
				} else {
					retVal = new File(getCorpusFolder(corpus), uri.getPath());
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		
		return retVal;
	}
	
	private String sessionProjectPath(String corpus, String session) {
		return corpus + "." + session;
	}

	@Override
	public Session openSession(String corpus, String session)
			throws IOException {
		return null;
	}

	@Override
	public UUID getSessionWriteLock(Session session) {
		return getSessionWriteLock(session.getCorpus(), session.getName());
	}

	@Override
	public UUID getSessionWriteLock(String corpus, String session) {
		final String key = sessionProjectPath(corpus, session);
		UUID currentLock = sessionLocks.get(key);
		
		// already locks
		if(currentLock != null) {
			return null;
		}
		
		final UUID lock = UUID.randomUUID();
		sessionLocks.put(key, lock);
		return lock;
	}

	@Override
	public void releaseSessionWriteLock(Session session, UUID writeLock) {
		releaseSessionWriteLock(session.getCorpus(), session.getName(), writeLock);
	}

	@Override
	public void releaseSessionWriteLock(String corpus, String session,
			int writeLock) {
		
	}

	@Override
	public void saveSession(Session session, int writeLock) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveSession(String corpus, String sessionName, Session session,
			int writeLock) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeSession(Session sesion, int writeLock) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeSession(String corpus, String sesion, int writeLock)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addProjectListener(ProjectListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeProjectListener(ProjectListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ProjectListener> getProjectListeners() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fireProjectEvent(ProjectEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InputStream getResourceInputStream(String resourceName)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream getResourceOutputStream(String resourceName)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Extension support
	 */
	private final ExtensionSupport extSupport = new ExtensionSupport(Project.class, this);
	
	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}
}
