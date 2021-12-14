/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.project;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

import javax.xml.bind.*;
import javax.xml.parsers.*;
import javax.xml.stream.*;
import javax.xml.xpath.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import ca.phon.extensions.*;
import ca.phon.project.exceptions.*;
import ca.phon.project.io.*;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.io.*;
import ca.phon.util.*;

/**
 * A local on-disk project
 *
 */
public class LocalProject implements Project, ProjectRefresh {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(LocalProject.class.getName());

	/**
	 * Project folder
	 *
	 */
	private final File projectFolder;
	
	private Properties properties;

	/**
	 * @deprecated since 3.1.1
	 */
	@Deprecated
	public final static String PROJECT_XML_FILE = "project.xml";

	public final static String PREV_PROJECT_PROPERTIES_FILE = ".properties";
	public final static String PROJECT_PROPERTIES_FILE = "project.properties";

	public final static String PROJECT_MEDIAFOLDER_PROP = "project.mediaFolder";

	public final static String CORPUS_MEDIAFOLDER_PROP = "corpus.mediaFolder";
	
	public final static String PROJECT_NAME_PROP = "project.name";
	
	public final static String PROJECT_UUID_PROP = "project.uuid";

	private final static String sessionTemplateFile = "__sessiontemplate.xml";
	
	private final static String PROJECT_RES_FOLDER = "__res";
	
	private final static String CORPUS_DESC_FILE = "__description";

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
	 * @param projectFolder
	 */
	protected LocalProject(File projectFolder)
			throws ProjectConfigurationException {
		super();
		this.projectFolder = projectFolder;

		// Properties are loaded here if found
		extSupport.initExtensions();

		loadProperties();
		// if not found, create new properties
		if(!getExtensions().contains(Properties.class)) {
			putExtension(Properties.class, new Properties());
		}
		properties = getExtension(Properties.class);
		checkProperties();
		
		putExtension(ProjectRefresh.class, this);
	}
	
	private void loadProperties() {
		final File oldPropertiesFile = new File(getFolder(), PREV_PROJECT_PROPERTIES_FILE);
		File propsFile = new File(getFolder(), PROJECT_PROPERTIES_FILE);
		propsFile = (propsFile.exists() ? propsFile : oldPropertiesFile);

		if(propsFile.exists()) {
			// load properties
			Properties props = new Properties();
			try {
				props.load(new FileInputStream(propsFile));
			} catch (IOException e) {
				LOGGER.warn( "Could not load project properties. " + e.getLocalizedMessage(), e);
			}
			putExtension(Properties.class, props);
		}
	}
	
	/**
	 * Check project setup and copy information from project.xml file if exists
	 */
	private void checkProperties() {
		ProjectType pt = null;
		try {
			pt = loadProjectData();
		} catch(ProjectConfigurationException e) {
			LOGGER.warn(e);
		}
		
		boolean modified = false;
		if(!properties.containsKey(PROJECT_UUID_PROP)) {
			if(pt != null) {
				if(pt.getUuid() != null)
					properties.put(PROJECT_UUID_PROP, pt.getUuid());
				else
					properties.put(PROJECT_UUID_PROP, UUID.randomUUID().toString());
				
				// if UUID not found we likely need to upgrade this project
				// copy corpus descriptions if necessary
				for(String corpus:getCorpora()) {
					final File corpusFolder = getCorpusFolder(corpus);
					final File corpusInfoFile = new File(corpusFolder, CORPUS_DESC_FILE);
					
					for(CorpusType ct:pt.getCorpus()) {
						if(ct.getName().equals(corpus)) {
							if(ct.getDescription() != null 
									&& ct.getDescription().trim().length() > 0
									&& !corpusInfoFile.exists()) {
								try (PrintWriter out = new PrintWriter(corpusInfoFile)) {
									out.write(ct.getDescription());
									out.flush();
								} catch (IOException e) {
									LOGGER.warn(e);
								}
							}
							break;
						}
					}
				}
			} else {
				properties.put(PROJECT_UUID_PROP, UUID.randomUUID().toString());
			}
			modified = true;
		}
		if(!properties.containsKey(PROJECT_NAME_PROP)) {
			if(pt != null) {
				properties.put(PROJECT_NAME_PROP, pt.getName());
			} else {
				properties.put(PROJECT_NAME_PROP, projectFolder.getName());
			}
			modified = true;
		}
		if(modified) {
			try {
				saveProperties();
			} catch (IOException e) {
				LOGGER.error(e);
			}
		}
	}

	/**
	 * Load project data from the project.xml file.
	 * If not found, empty project data is created.
	 *
	 * @return projectData
	 * 
	 * @deprecated
	 */
	private ProjectType loadProjectData() throws ProjectConfigurationException {
		final File dataFile = new File(getFolder(), PROJECT_XML_FILE);
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
		} else {
			return null;
		}
	}

	/**
	 * Save project data
	 *
	 * @throws IOException
	 */
	protected void saveProjectData()
		throws IOException {
		saveProperties();
	}

	protected synchronized void saveProperties() throws IOException {
		final File oldPropsFile = new File(getFolder(), PREV_PROJECT_PROPERTIES_FILE);
		if(oldPropsFile.exists()) {
			Files.deleteIfExists(oldPropsFile.toPath());
		}

		// save properties
		final Properties properties = getExtension(Properties.class);
		if(properties != null) {
			final File propFile = new File(getFolder(), PROJECT_PROPERTIES_FILE);
			properties.store(new FileOutputStream(propFile), "Phon " + VersionInfo.getInstance().getLongVersion());
		}
	}

	private File getFolder() {
		return this.projectFolder;
	}

	@Override
	public String getName() {
		return properties.getProperty(PROJECT_NAME_PROP, getFolder().getName());
	}

	@Override
	public void setName(String name) {
		final String oldName = getName();
		properties.put(PROJECT_NAME_PROP, name);

		final ProjectEvent event = ProjectEvent.newNameChangedEvent(oldName, name);
		fireProjectDataChanged(event);
	}

	@Override
	public UUID getUUID() {
		final UUID uuid = UUID.fromString(properties.getProperty(PROJECT_UUID_PROP));
		return uuid;
	}

	@Override
	public void setUUID(UUID uuid) {
		final UUID oldUUID = getUUID();
		properties.put(PROJECT_UUID_PROP, uuid.toString());

		final ProjectEvent event = ProjectEvent.newUUIDChangedEvent(oldUUID.toString(), uuid.toString());
		fireProjectDataChanged(event);
	}

	@Override
	public List<String> getCorpora() {
		final List<String> corpusList = new ArrayList<String>();

		Path projectPath = getFolder().toPath();
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(projectPath)) {
			for(Path path:stream) {
				if(Files.isDirectory(path)) {
					if(!Files.isHidden(path)) {
						String folderName = path.getFileName().toString();
						if(!folderName.startsWith("~")
								&& !folderName.endsWith("~")
								&& !folderName.startsWith(".")
								&& !folderName.startsWith("__")) {
							corpusList.add(folderName);
						}	
					}
				}
			}
			
			Collections.sort(corpusList);
		} catch (IOException e) {
			LOGGER.error(e);
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

		final ProjectEvent pe = ProjectEvent.newCorpusAddedEvent(name);
		fireProjectStructureChanged(pe);
	}

	@Override
	public void renameCorpus(String corpus, String newName) throws IOException {
		final File corpusFolder = getCorpusFolder(corpus);
		final File newCorpusFolder = getCorpusFolder(newName);

		final String corpusMediaFolder = getCorpusMediaFolder(corpus);
		setCorpusMediaFolder(corpus, null);

		// rename folder
		corpusFolder.renameTo(newCorpusFolder);

		if(!corpusMediaFolder.equals(getProjectMediaFolder())) {
			setCorpusMediaFolder(newName, corpusMediaFolder);
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

		setCorpusMediaFolder(corpus, null);

		ProjectEvent pe = ProjectEvent.newCorpusRemovedEvent(corpus);
		fireProjectStructureChanged(pe);
	}

	@Override
	public String getCorpusDescription(String corpus) {
		final File corpusFolder = getCorpusFolder(corpus);
		final File corpusInfoFile = new File(corpusFolder, CORPUS_DESC_FILE);
		
		String retVal = "";
		if(corpusInfoFile.exists()) {
			try {
				retVal = Files.readString(corpusInfoFile.toPath());
			} catch (IOException e) {
				LOGGER.error(e);
			}
		}
		
		return retVal;
	}

	@Override
	public void setCorpusDescription(String corpus, String description) {
		String old = getCorpusDescription(corpus);
		
		final File corpusFolder = getCorpusFolder(corpus);
		final File corpusInfoFile = new File(corpusFolder, CORPUS_DESC_FILE);

		if(description != null && description.trim().length() > 0) {
			try {
				Files.write(corpusInfoFile.toPath(), description.trim().getBytes("UTF-8"));
			} catch (IOException e) {
				LOGGER.error(e);
			}
		} else {
			if(corpusInfoFile.exists()) {
				try {
					Files.deleteIfExists(corpusInfoFile.toPath());
				} catch (IOException e) {
					LOGGER.error(e);
				}
			}
		}

		ProjectEvent pe = ProjectEvent.newCorpusDescriptionChangedEvent(corpus, old, description);
		fireProjectDataChanged(pe);
	}
	
	@Override
	public boolean hasCustomProjectMediaFolder() {
		final Properties props = getExtension(Properties.class);
		return (props.getProperty(PROJECT_MEDIAFOLDER_PROP) != null);
	}

	@Override
	public String getProjectMediaFolder() {
		final Properties props = getExtension(Properties.class);
		String retVal = props.getProperty(PROJECT_MEDIAFOLDER_PROP, PROJECT_RES_FOLDER + File.separator + "media");
		return retVal;
	}

	@Override
	public void setProjectMediaFolder(String mediaFolder) {
		final String old = getProjectMediaFolder();
		final Properties props = getExtension(Properties.class);
		if(mediaFolder == null) {
			props.remove(PROJECT_MEDIAFOLDER_PROP);
		} else {
			props.setProperty(PROJECT_MEDIAFOLDER_PROP, mediaFolder);
		}

		try {
			saveProperties();
		} catch (IOException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}

		ProjectEvent pe = ProjectEvent.newProjectMediaFolderChangedEvent(old, mediaFolder);
		fireProjectDataChanged(pe);
	}

	@Override
	public boolean hasCustomCorpusMediaFolder(String corpus) {
		final String propName = CORPUS_MEDIAFOLDER_PROP + "." + corpus;
		final Properties props = getExtension(Properties.class);
		return (props.getProperty(propName) != null);
	}
	
	@Override
	public String getCorpusMediaFolder(String corpus) {
		final String propName = CORPUS_MEDIAFOLDER_PROP + "." + corpus;
		final Properties props = getExtension(Properties.class);
		return props.getProperty(propName, getProjectMediaFolder());
	}

	@Override
	public void setCorpusMediaFolder(String corpus, String mediaFolder) {
		final String old = getCorpusMediaFolder(corpus);
		final Properties props = getExtension(Properties.class);
		final String propName = CORPUS_MEDIAFOLDER_PROP + "." + corpus;

		if(mediaFolder == null) {
			props.remove(propName);
		} else {
			props.setProperty(propName, mediaFolder);
		}

		try {
			saveProperties();
		} catch (IOException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}

		ProjectEvent pe = ProjectEvent.newCorpusMediaFolderChangedEvent(corpus, old, mediaFolder);
		fireProjectDataChanged(pe);
	}

	@Override
	public List<String> getCorpusSessions(String corpus) {
		final List<String> retVal = new ArrayList<String>();
		
		Set<String> validexts = SessionInputFactory.getSessionExtensions();
		
		final File corpusFolder = getCorpusFolder(corpus);
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(corpusFolder.toPath())) {
			for(Path path:stream) {
				if(!Files.isDirectory(path) && !Files.isHidden(path)) {
					String filename = path.getFileName().toString();
					if(!filename.startsWith("~")
							&& !filename.endsWith("~")
							&& !filename.startsWith("__")) {
						int lastDot = filename.lastIndexOf('.');
						if(lastDot > 0) {
							String ext = filename.substring(lastDot+1);
							if(validexts.contains(ext)) {
								retVal.add(filename.substring(0, lastDot));
							}
						}
					}
				}
			}
			
			Collections.sort(retVal);
		} catch (IOException e) {
			LOGGER.error(e);
		}

		return retVal;
	}

	public File getCorpusFolder(String corpus) {
		File retVal = new File(getCorpusPath(corpus));
		return retVal;
	}

	public File getSessionFile(String corpus, String session) {
		final List<File> potentialFiles =
				SessionInputFactory.getSessionExtensions().stream()
					.map( (ext) -> new File(getCorpusFolder(corpus), session + "." + ext) )
					.collect( Collectors.toList() );
		final Optional<File> optionalFile = potentialFiles.stream()
					.filter( File::exists )
					.findFirst();
		File retVal = optionalFile.orElse(new File(getCorpusFolder(corpus), session + ".xml"));

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
			LOGGER.error( e.getLocalizedMessage(), e);
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

		final ProjectEvent pe = ProjectEvent.newSessionChangedEvent(corpus, session);
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

		final ProjectEvent pe = ProjectEvent.newSessionChangedEvent(corpus, session);
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

		File sessionFile = getSessionFile(corpus, sessionName);
		final boolean created = !sessionFile.exists();

		boolean needToDeleteExisting = false;
		File oldSessionFile = null;
		if(!created) {
			final OriginalFormat format = session.getExtension(OriginalFormat.class);
			if(format != null) {
				// check for extension change
				if(!sessionFile.getName().endsWith(format.getSessionIO().extension())) {
					needToDeleteExisting = true;
					oldSessionFile = new File(sessionFile.getAbsolutePath());
					sessionFile = new File(getCorpusFolder(corpus), sessionName + "." + format.getSessionIO().extension());
				}
			}
		}

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

			if(needToDeleteExisting) {
				oldSessionFile.delete();
			}
		} catch (IOException e) {
			// unable to write the session, bail!
			throw new IOException("Session not written to disk", e);
		}

		final FileOutputStream fOut  = new FileOutputStream(sessionFile);
		writer.writeSession(session, fOut);
		fOut.close();

		if(created && !sessionName.startsWith("__") && !sessionName.startsWith("~")) {
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
	public ZonedDateTime getSessionModificationTime(Session session) {
		return getSessionModificationTime(session.getCorpus(), session.getName());
	}

	@Override
	public ZonedDateTime getSessionModificationTime(String corpus, String session) {
		final File sessionFile = getSessionFile(corpus, session);

		Path nioPath = sessionFile.toPath();
		try {
			BasicFileAttributes fileAttribs = Files.readAttributes(nioPath, BasicFileAttributes.class);
			FileTime ft = fileAttribs.lastModifiedTime();
			LocalDateTime ldt = ft.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			ZonedDateTime zonedDate = ldt.atZone(ZoneId.systemDefault());
			return zonedDate;
		} catch (IOException e) {
			return ZonedDateTime.now();
		}
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

		if(sessionFile.exists() && sessionFile.getName().endsWith(".xml") ) {
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
		} else {
			final Session s = openSession(corpus, session);
			retVal = s.getRecordCount();
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
				LOGGER.warn( e.getLocalizedMessage(), e);
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
		return VersionInfo.getInstance().getVersion();
	}

	@Override
	public String getLocation() {
		return projectFolder.getAbsolutePath();
	}

	@Override
	public String getCorpusPath(String corpus) {
		return new File(getFolder(), corpus).getAbsolutePath();
	}

	@Override
	public void setCorpusPath(String corpus, String path) {
		throw new UnsupportedOperationException();
//		setCorpusFolder(corpus, new File(path));
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
		}

		final UUID writeLock = getSessionWriteLock(s);
		saveSession(s, writeLock);
		releaseSessionWriteLock(s, writeLock);

		return s;
	}

	@Override
	public void refresh() {
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getResourceLocation() {
		String retVal = this.resourceLocation;
		if(retVal == null) {
			retVal = (new File(getLocation(), PROJECT_RES_FOLDER)).getAbsolutePath();
		}
		return retVal;
	}

	@Override
	public void setRecourceLocation(String location) {
		this.resourceLocation = location;
	}

}
