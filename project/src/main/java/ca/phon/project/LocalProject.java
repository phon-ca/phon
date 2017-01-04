/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.project.io.CorpusType;
import ca.phon.project.io.ObjectFactory;
import ca.phon.project.io.ProjectType;
import ca.phon.project.io.SessionType;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SessionPath;
import ca.phon.session.io.OriginalFormat;
import ca.phon.session.io.SessionIO;
import ca.phon.session.io.SessionInputFactory;
import ca.phon.session.io.SessionOutputFactory;
import ca.phon.session.io.SessionReader;
import ca.phon.session.io.SessionWriter;

/**
 * A local on-disk project
 * 
 */
public class LocalProject implements Project, ProjectRefresh {
	
	private final static Logger LOGGER = Logger.getLogger(LocalProject.class.getName());
	
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
	
	private final static String sessionTemplateFile = "__sessiontemplate.xml";
	
	/**
	 * Session write locks
	 */
	private final Map<String, UUID> sessionLocks = 
			Collections.synchronizedMap(new HashMap<String, UUID>());
	
	private final List<ProjectListener> projectListeners = 
			Collections.synchronizedList(new ArrayList<ProjectListener>());
	
	private String resourceLocation = null;

	/**
	 * Extension support
	 */
	private final ExtensionSupport extSupport = new ExtensionSupport(Project.class, this);
	
	/**
	 * 
	 * @param url
	 */
	protected LocalProject(File projectFolder) 
			throws ProjectConfigurationException {
		super();
		this.projectFolder = projectFolder;
		
		extSupport.initExtensions();
		
		// load project data
		projectData = loadProjectData();
		refresh();
		
		putExtension(ProjectRefresh.class, this);
	}
	
	/**
	 * Load project data from the project.xml file.
	 * If not found, empty project data is created.
	 * 
	 * @return projectData
	 */
	private ProjectType loadProjectData() throws ProjectConfigurationException {
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
				throw new ProjectConfigurationException(jaxbEx);
			} catch (FileNotFoundException e) {
				throw new ProjectConfigurationException(e);
			} catch (XMLStreamException e) {
				throw new ProjectConfigurationException(e);
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
	protected void saveProjectData() 
		throws IOException {
		final ObjectFactory factory = new ObjectFactory();
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			final Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			final File dataFile = new File(getFolder(), projectDataFile);
			
			final ProjectType projectData = getProjectData();
			// HACK to ensure compatibility with Phon 1.6.2
			projectData.setAppid("1.5");
			projectData.setVersion("1.5");
			
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
					&& !f.getName().endsWith("~") 
					&& !f.getName().startsWith("__") 
					&& !f.getName().startsWith(".")
					&& !f.isHidden()) {
				final String corpusName = f.getName();
				CorpusType ct = getCorpusInfo(corpusName);
				if(ct == null) {
					ct = factory.createCorpusType();
					ct.setName(corpusName);
					ct.setDescription("");
				}
				
				final List<SessionType> toRemove = new ArrayList<SessionType>(ct.getSession());
				// look for all xml files inside corpus folder
				for(File xmlFile:f.listFiles()) {
					if(xmlFile.getName().endsWith(".xml")
							&& !xmlFile.getName().startsWith("~")
							&& !xmlFile.getName().endsWith("~")
							&& !xmlFile.getName().startsWith("__")
							&& !xmlFile.getName().startsWith(".")
							&& !xmlFile.isHidden()) {
						final String sessionName = xmlFile.getName().substring(0, xmlFile.getName().lastIndexOf('.'));
						
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
						} else {
							toRemove.remove(sessionType);
						}
					}
				}
				for(SessionType st:toRemove) {
					ct.getSession().remove(st);
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
	protected CorpusType getCorpusInfo(String corpus) {
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
	protected SessionType getSessionInfo(String corpus, String session) {
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
	
	protected ProjectType getProjectData() {
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
		fireProjectDataChanged(event);
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
		fireProjectDataChanged(event);
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
		
		saveProjectData();
		
		final ProjectEvent pe = ProjectEvent.newCorpusAddedEvent(name);
		fireProjectStructureChanged(pe);
	}

	@Override
	public void renameCorpus(String corpus, String newName) throws IOException {
		final File corpusFolder = getCorpusFolder(corpus);
		final File newCorpusFolder = getCorpusFolder(newName);
		
		// rename folder
		corpusFolder.renameTo(newCorpusFolder);

		final CorpusType ct = getCorpusInfo(corpus);
		if(ct != null) {
			ct.setName(newName);
		}
		
		ProjectEvent pe = ProjectEvent.newCorpusRemovedEvent(corpus);
		fireProjectStructureChanged(pe);
		pe = ProjectEvent.newCorpusAddedEvent(newName);
		fireProjectStructureChanged(pe);
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
		
		saveProjectData();
		
		ProjectEvent pe = ProjectEvent.newCorpusRemovedEvent(corpus);
		fireProjectStructureChanged(pe);
	}

	@Override
	public String getCorpusDescription(String corpus) {
		final CorpusType ct = getCorpusInfo(corpus);
		return (ct == null ? "" : ct.getDescription());
	}

	@Override
	public void setCorpusDescription(String corpus, String description) {
		CorpusType ct = getCorpusInfo(corpus);
		String old = null;
		if(ct == null) {
			old = ct.getDescription();
			ct = (new ObjectFactory()).createCorpusType();
			ct.setName(corpus);
			getProjectData().getCorpus().add(ct);
		}
		ct.setDescription(description);
		
		try {
			saveProjectData();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		ProjectEvent pe = ProjectEvent.newCorpusDescriptionChangedEvent(corpus, old, description);
		fireProjectDataChanged(pe);
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
	
	private void setCorpusFolder(String corpus, File folder) {
		CorpusType ct = getCorpusInfo(corpus);
		if(ct == null) {
			ct = (new ObjectFactory()).createCorpusType();
			ct.setName(corpus);
			ct.setDescription("");
			
			projectData.getCorpus().add(ct);
		}
		ct.setLoc(folder.toURI().toString());
	}
	
	private File getSessionFile(String corpus, String session) {
		File retVal = new File(getCorpusFolder(corpus), session + ".xml");
		
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
		final File sessionFile = getSessionFile(corpus, session);
		final URI uri = sessionFile.toURI();
		final SessionInputFactory inputFactory = new SessionInputFactory();
		final SessionReader reader = inputFactory.createReaderForFile(sessionFile);
		if(reader == null) {
			throw new IOException("No session reader available for " + uri.toASCIIString());
		}
		return openSession(corpus, session, reader);
	}

	@Override
	public Session openSession(String corpus, String session, SessionReader reader)
			throws IOException {
		final File sessionFile = getSessionFile(corpus, session);
		final URI uri = sessionFile.toURI();
		
		try(InputStream in = uri.toURL().openStream()) {
			final Session retVal = reader.readSession(in);
			
			// make sure corpus and session match the expected values, these
			// can change if the session file has been manually moved
			if(!retVal.getCorpus().equals(corpus)) {
				retVal.setCorpus(corpus);
			}
			if(retVal.getName() == null || !retVal.getName().equals(session)) {
				retVal.setName(session);
			}
			
			return retVal;
		} catch (Exception e) {
			// catch all exceptions
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new IOException(e);
		}
	}

	@Override
	public UUID getSessionWriteLock(Session session)
		throws IOException {
		return getSessionWriteLock(session.getCorpus(), session.getName());
	}

	@Override
	public UUID getSessionWriteLock(String corpus, String session) 
		throws IOException {
		final String key = sessionProjectPath(corpus, session);
		UUID currentLock = sessionLocks.get(key);
		
		// already locks
		if(currentLock != null) {
			throw new IOException("Session '" + key + "' is already locked.");
		}
		
		final UUID lock = UUID.randomUUID();
		sessionLocks.put(key, lock);
		
		final ProjectEvent pe = ProjectEvent.newSessionChagnedEvent(corpus, session);
		fireProjectWriteLocksChanged(pe);
		
		return lock;
	}
	
	/**
	 * Checks given session write lock
	 * 
	 * @throws IOException if the write lock is not valid
	 */
	protected void checkSessionWriteLock(String corpus, String session, UUID writeLock)
		throws IOException {
		final String sessionLoc = sessionProjectPath(corpus, session);
		final UUID uuid = sessionLocks.get(sessionLoc);
		if(uuid == null) {
			throw new IOException("Session '" + sessionLoc + "' is not locked.");
		}
		if(!uuid.equals(writeLock)) {
			throw new IOException("Given writeLock for '" + sessionLoc + "' does not match project lock.");
		}
	}

	@Override
	public void releaseSessionWriteLock(Session session, UUID writeLock)
		throws IOException {
		releaseSessionWriteLock(session.getCorpus(), session.getName(), writeLock);
	}

	@Override
	public void releaseSessionWriteLock(String corpus, String session,
			UUID writeLock) throws IOException {
		final String sessionLoc = sessionProjectPath(corpus, session);
		checkSessionWriteLock(corpus, session, writeLock);
		sessionLocks.remove(sessionLoc);
		
		final ProjectEvent pe = ProjectEvent.newSessionChagnedEvent(corpus, session);
		fireProjectWriteLocksChanged(pe);
	}

	@Override
	public void saveSession(Session session, UUID writeLock) throws IOException {
		saveSession(session.getCorpus(), session.getName(), session, writeLock);
	}

	@Override
	public void saveSession(String corpus, String sessionName, Session session,
			UUID writeLock) throws IOException {
		final SessionOutputFactory outputFactory = new SessionOutputFactory();
		
		// get default writer
		SessionWriter writer = outputFactory.createWriter();
		// look for an original format, if found save in the same format
		final OriginalFormat origFormat = session.getExtension(OriginalFormat.class);
		if(origFormat != null) {
			writer = outputFactory.createWriter(origFormat.getSessionIO());
		}

		saveSession(corpus, sessionName, session, writer, writeLock);
	}
	
	@Override
	public void saveSession(String corpus, String sessionName, Session session, SessionWriter writer,
			UUID writeLock) throws IOException {
		checkSessionWriteLock(corpus, sessionName, writeLock);
		
		final File sessionFile = getSessionFile(corpus, sessionName);
		final boolean created = !sessionFile.exists();
		
		// XXX safety checks, make sure we can read back in what we write.
		// also make sure the number of records has not changed between
		// the original and serialized session
		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			writer.writeSession(session, bout);
			
			final SessionReader reader = (new SessionInputFactory()).createReader(
					writer.getClass().getAnnotation(SessionIO.class));
			final Session testSession = reader.readSession(new ByteArrayInputStream(bout.toByteArray()));
			if(testSession.getRecordCount() != session.getRecordCount()) {
				throw new IOException("Session serialization failed.");
			}
			bout.close();
		} catch (IOException e) {
			// unable to write the session, bail!
			throw new IOException("Session not written to disk", e);
		}
		
		final FileOutputStream fOut  = new FileOutputStream(sessionFile);
		writer.writeSession(session, fOut);
		fOut.close();
		
		if(created) {
			final ObjectFactory xmlFactory = new ObjectFactory();
			final SessionType st = xmlFactory.createSessionType();
			st.setName(sessionName);
			final CorpusType ct = getCorpusInfo(corpus);
			if(ct != null) {
				ct.getSession().add(st);
			}
			
			saveProjectData();
			
			final ProjectEvent pe = ProjectEvent.newSessionAddedEvent(corpus, sessionName);
			fireProjectStructureChanged(pe);
		}
	}

	@Override
	public void removeSession(Session session, UUID writeLock) throws IOException {
		removeSession(session.getCorpus(), session.getName(), writeLock);
	}

	@Override
	public void removeSession(String corpus, String session, UUID writeLock)
			throws IOException {
		checkSessionWriteLock(corpus, session, writeLock);
		
		final File sessionFile = getSessionFile(corpus, session);
		
		if(!sessionFile.exists()) {
			throw new FileNotFoundException(sessionFile.getAbsolutePath());
		}
		
		if(!sessionFile.canWrite()) {
			throw new IOException("Unable to delete " + sessionFile.getAbsolutePath() + ", file is read-only.");
		}
		
		if(!sessionFile.delete()) {
			throw new IOException("Unable to delete " + sessionFile.getAbsolutePath() + ".");
		}
		
		final CorpusType ct = getCorpusInfo(corpus);
		if(ct != null) {
			final SessionType st = getSessionInfo(corpus, session);
			if(st != null) {
				ct.getSession().remove(st);
			}
		}
		
		saveProjectData();
		
		final ProjectEvent pe = ProjectEvent.newSessionRemovedEvent(corpus, session);
		fireProjectStructureChanged(pe);
	}

	@Override
	public InputStream getResourceInputStream(String resourceName)
			throws IOException {
		final File resFolder = new File(getResourceLocation());
		final File resFile = new File(resFolder, resourceName);
		
		return new FileInputStream(resFile);
	}

	@Override
	public OutputStream getResourceOutputStream(String resourceName)
			throws IOException {
		final File resFolder = new File(getResourceLocation());
		final File resFile = new File(resFolder, resourceName);
		
		// make parent folders as necessary
		if(!resFile.getParentFile().exists()) {
			resFile.getParentFile().mkdirs();
		}
		
		return new FileOutputStream(resFile);
	}
	
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

	@Override
	public List<ProjectListener> getProjectListeners() {
		return Collections.unmodifiableList(projectListeners);
	}

	@Override
	public void addProjectListener(ProjectListener listener) {
		if(!projectListeners.contains(listener)) {
			projectListeners.add(listener);
		}
	}

	@Override
	public void removeProjectListener(ProjectListener listener) {
		projectListeners.remove(listener);
	}

	@Override
	public void fireProjectStructureChanged(ProjectEvent pe) {
		final List<ProjectListener> listeners = getProjectListeners();
		for(ProjectListener listener:listeners) {
			listener.projectStructureChanged(pe);
		}
	}

	@Override
	public void fireProjectDataChanged(ProjectEvent pe) {
		final List<ProjectListener> listeners = getProjectListeners();
		for(ProjectListener listener:listeners) {
			listener.projectDataChanged(pe);
		}
	}

	@Override
	public void fireProjectWriteLocksChanged(ProjectEvent pe) {
		final List<ProjectListener> listeners = getProjectListeners();
		for(ProjectListener listener:listeners) {
			listener.projectWriteLocksChanged(pe);
		}
	}

	@Override
	public LocalDateTime getSessionModificationTime(Session session) {
		return getSessionModificationTime(session.getCorpus(), session.getName());
	}

	@Override
	public LocalDateTime getSessionModificationTime(String corpus, String session) {
		final File sessionFile = getSessionFile(corpus, session);
		long modTime = 0L;
		if(sessionFile.exists()) {
			modTime = sessionFile.lastModified();
		}
		return LocalDateTime.ofEpochSecond(modTime/1000, (int)(modTime%1000), ZoneOffset.UTC);
	}
	
	@Override
	public long getSessionByteSize(Session session) {
		return getSessionByteSize(session.getCorpus(), session.getName());
	}

	@Override
	public long getSessionByteSize(String corpus, String session) {
		long size = 0L;
	
		final File sessionFile = getSessionFile(corpus, session);
		if(sessionFile.exists() && sessionFile.canRead()) {
			size = sessionFile.length();
		}
		
		return size;
	}

	@Override
	public int numberOfRecordsInSession(String corpus, String session)
			throws IOException {
		final File sessionFile = getSessionFile(corpus, session);
		int retVal = 0;
		if(sessionFile.exists()) {
			// it's faster to use an xpath expression
			// to determine the number of records.
			String xpathPattern = "//u";
			// open as dom file first
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(false);
			DocumentBuilder builder;
			try {
				builder = domFactory.newDocumentBuilder();
				Document doc = builder.parse(sessionFile);
				
				XPathFactory xpathFactory = XPathFactory.newInstance();
				XPath xpath = xpathFactory.newXPath();
				XPathExpression expr = xpath.compile(xpathPattern);
				
				Object result = expr.evaluate(doc, XPathConstants.NODESET);
				NodeList nodes = (NodeList) result;
				retVal = nodes.getLength();
			} catch (ParserConfigurationException e) {
				throw new IOException(e);
			} catch (SAXException e) {
				throw new IOException(e);
			} catch (XPathExpressionException e) {
				throw new IOException(e);
			}
		}
		
		return retVal;
	}
	
	@Override
	public Set<Participant> getParticipants(Collection<SessionPath> sessions) {
		final Comparator<Participant> comparator = (p1, p2) -> {
			int retVal = p1.getId().compareTo(p2.getId());
			if(retVal == 0) {
				final String p1Name = (p1.getName() == null ? "" : p1.getName());
				final String p2Name = (p2.getName() == null ? "" : p2.getName());
				retVal = p1Name.compareTo(p2Name);
				if(retVal == 0) {
					retVal = p1.getRole().compareTo(p2.getRole());
				}
			}
			return retVal;
		};
		final Set<Participant> retVal = new TreeSet<>(comparator);
		
		for(SessionPath sessionPath:sessions) {
			try {
				Session session = openSession(sessionPath.getCorpus(), sessionPath.getSession());
				Collection<Participant> participants = new ArrayList<>();
				
				participants.add( SessionFactory.newFactory().cloneParticipant(Participant.UNKNOWN) );
				session.getParticipants().forEach( (p) -> participants.add(p) );
				
				for(Participant participant:participants) {
					Participant speaker = null;
					if(retVal.contains(participant)) {
						for(Participant p:retVal) {
							if(comparator.compare(participant, p) == 0) {
								speaker = p;
								break;
							}
						}
					} else {
						speaker = SessionFactory.newFactory().cloneParticipant(participant);
					}
					
					// get record count
					int count = 0;
					for(Record r:session.getRecords()) {
						if(comparator.compare(r.getSpeaker(), participant) == 0) ++count;
					}
					
					if(speaker != null) {
						if(count == 0 && comparator.compare(Participant.UNKNOWN, speaker) == 0) {
							// do not add unknown speaker if there are no records
						} else {
							ParticipantHistory history = speaker.getExtension(ParticipantHistory.class);
							if(history == null) {
								history = new ParticipantHistory();
								speaker.putExtension(ParticipantHistory.class, history);
							}
							Period age = 
									(participant != null ? participant.getAge(session.getDate()) : null);
							history.setAgeForSession(sessionPath, age);
							history.setNumberOfRecordsForSession(sessionPath, count);
						}
					}
					
					if(!retVal.contains(speaker)) {
						if(comparator.compare(Participant.UNKNOWN, speaker) == 0) {
							if(count > 0)
								retVal.add(speaker);
						} else {
							retVal.add(speaker);
						}
					}
					
				}
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
			}
		}
		
		return retVal;
	}

	@Override
	public boolean isSessionLocked(Session session) {
		return isSessionLocked(session.getCorpus(), session.getName());
	}

	@Override
	public boolean isSessionLocked(String corpus, String session) {
		final String sessionLoc = sessionProjectPath(corpus, session);
		return this.sessionLocks.containsKey(sessionLoc);
	}

	@Override
	public String getVersion() {
		return projectData.getVersion();
	}

	@Override
	public String getLocation() {
		return projectFolder.getAbsolutePath();
	}
	
	@Override
	public String getCorpusPath(String corpus) {
		final File corpusFolder = getCorpusFolder(corpus);
		return (corpusFolder == null ? corpus : corpusFolder.getAbsolutePath());
	}
	
	@Override
	public void setCorpusPath(String corpus, String path) {
		setCorpusFolder(corpus, new File(path));
	}

	@Override
	public String getSessionPath(Session session) {
		return getSessionPath(session.getCorpus(), session.getName());
	}

	@Override
	public String getSessionPath(String corpus, String session) {
		final File sessionFile = getSessionFile(corpus, session);
		return (sessionFile == null ? corpus + File.separator + session : sessionFile.getAbsolutePath());
	}

	@Override
	public Session getSessionTemplate(String corpus) throws IOException {
		final File corpusFolder = new File(getLocation(), corpus);
		final File templateFile = new File(corpusFolder, sessionTemplateFile);
		
		if(templateFile.exists()) {
			final SessionInputFactory inputFactory = new SessionInputFactory();
			// TODO use method to find which reader will work for the file
			final SessionReader reader = inputFactory.createReader("phonbank", "1.2");
			if(reader == null) {
				throw new IOException("No session reader available for " + templateFile.toURI().toASCIIString());
			}
			final Session retVal = reader.readSession(templateFile.toURI().toURL().openStream());
			return retVal;
		} else {
			throw new FileNotFoundException(templateFile.getAbsolutePath());
		}
	}

	@Override
	public void saveSessionTemplate(String corpus, Session template)
			throws IOException {
		final File corpusFolder = new File(getLocation(), corpus);
		final File templateFile = new File(corpusFolder, sessionTemplateFile);
		
		final SessionOutputFactory outputFactory = new SessionOutputFactory();
		final SessionWriter writer = outputFactory.createWriter();
		
		final FileOutputStream fOut  = new FileOutputStream(templateFile);
		writer.writeSession(template, fOut);
	}

	@Override
	public Session createSessionFromTemplate(String corpus, String session)
			throws IOException {
		if(getCorpusSessions(corpus).contains(session)) {
			throw new IOException("Session named " + corpus + "." + session + " already exists.");
		}
		
		Session template = null;
		try {
			template = getSessionTemplate(corpus);
		} catch (IOException e) { // do nothing 
		}

		final SessionFactory factory = SessionFactory.newFactory();
		Session s = null;
		if(template != null) {
			s = template;
			s.setCorpus(corpus);
			s.setName(session);
		} else {
			s = factory.createSession(corpus, session);
			
			final Record r = factory.createRecord();
			r.addGroup();
			s.addRecord(r);
		}
		
		final UUID writeLock = getSessionWriteLock(s);
		saveSession(s, writeLock);
		releaseSessionWriteLock(s, writeLock);
		
		return s;
	}

	@Override
	public void refresh() {
		final List<CorpusType> corpora = scanProjectFolder();
		
		projectData.getCorpus().clear();
		projectData.getCorpus().addAll(corpora);
		
		try {
			saveProjectData();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getResourceLocation() {
		String retVal = this.resourceLocation;
		if(retVal == null) {
			retVal = (new File(getLocation(), "__res")).getAbsolutePath();
		}
		return retVal;
	}

	@Override
	public void setRecourceLocation(String location) {
		this.resourceLocation = location;
	}
	
}
