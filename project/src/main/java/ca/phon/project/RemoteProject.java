package ca.phon.project;

import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.project.io.*;
import ca.phon.session.*;
import ca.phon.session.io.*;

import java.io.*;
import java.net.*;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Access remote project using https
 * URI provided to constructor may have https or phon scheme
 *  e.g., https://www.phon.ca/project/MyProject
 *    or  phon://www.phon.ca/project/MyProject
 */
public class RemoteProject extends AbstractProject {

	private final URI projectURI;

	private ProjectType projectData;

	public RemoteProject(URI projectURI) throws ProjectConfigurationException {
		super();

		if(!"https".equals(projectURI.getScheme()) && !"phon".equals(projectURI.getScheme()))
			throw new ProjectConfigurationException(new IllegalArgumentException(projectURI.toString()));

		this.projectURI = projectURI;
		try {
			this.projectData = loadProjectData();
		} catch (IOException e) {
			throw new ProjectConfigurationException(e);
		}
	}

	private URL projectURL() throws MalformedURLException {
		// turns project uri into remote url
		final String protocol = "https";
		final String host = projectURI.getHost();
		final String remotePath = projectURI.getPath();
		return new URL(protocol, host, remotePath);
	}

	private URL projectFileURL(String relativePath) throws MalformedURLException {
		final URL projectURL = projectURL();
		final String newPath = projectURL.getPath() + "/" + relativePath;
		return new URL(projectURL.getProtocol(), projectURL.getHost(), newPath);
	}

	private ProjectType loadProjectData() throws IOException, ProjectConfigurationException {
		final URL projectXmlURL = projectFileURL(PROJECT_XML_FILE);
		return super.loadProjectData(projectXmlURL.openStream());
	}

	@Override
	public String getVersion() {
		return projectData.getVersion();
	}

	@Override
	public String getLocation() {
		return projectURI.toString();
	}

	@Override
	public String getName() {
		return projectData.getName();
	}

	@Override
	public void setName(String name) {}

	@Override
	public UUID getUUID() {
		return UUID.fromString(projectData.getUuid());
	}

	@Override
	public void setUUID(UUID uuid) {}

	@Override
	public List<String> getCorpora() {
		final List<String> retVal = new ArrayList<>();
		for(CorpusType ct:projectData.getCorpus()) {
			retVal.add(ct.getName());
		}
		return Collections.unmodifiableList(retVal);
	}

	@Override
	public void addCorpus(String name, String description) throws IOException {
		throw new IOException(new UnsupportedOperationException());
	}

	@Override
	public void renameCorpus(String corpus, String newName) throws IOException {
		throw new IOException(new UnsupportedOperationException());
	}

	@Override
	public void removeCorpus(String corpus) throws IOException {
		throw new IOException(new UnsupportedOperationException());
	}

	@Override
	public String getCorpusDescription(String corpus) {
		for(CorpusType ct:projectData.getCorpus()) {
			if(ct.getName().equals(corpus))
				return ct.getDescription();
		}
		return "";
	}

	@Override
	public void setCorpusDescription(String corpus, String description) {}

	@Override
	public boolean hasCustomProjectMediaFolder() {
		return true;
	}

	@Override
	public String getProjectMediaFolder() {
		return null;
	}

	@Override
	public void setProjectMediaFolder(String mediaFolder) {

	}

	@Override
	public boolean hasCustomCorpusMediaFolder(String corpus) {
		return false;
	}

	@Override
	public String getCorpusMediaFolder(String corpus) {
		return null;
	}

	@Override
	public void setCorpusMediaFolder(String corpus, String mediaFolder) {

	}

	@Override
	public Session getSessionTemplate(String corpus) throws IOException {
		return null;
	}

	@Override
	public void saveSessionTemplate(String corpus, Session template) throws IOException {

	}

	@Override
	public Session createSessionFromTemplate(String corpus, String session) throws IOException {
		return null;
	}

	@Override
	public List<String> getCorpusSessions(String corpus) {
		return null;
	}

	@Override
	public String getCorpusPath(String corpus) {
		return null;
	}

	@Override
	public void setCorpusPath(String corpus, String path) {

	}

	@Override
	public int numberOfRecordsInSession(String corpus, String session) throws IOException {
		return 0;
	}

	@Override
	public Set<Participant> getParticipants(Collection<SessionPath> sessions) {
		return null;
	}

	@Override
	public Session openSession(String corpus, String session) throws IOException {
		return null;
	}

	@Override
	public Session openSession(String corpus, String session, SessionReader reader) throws IOException {
		return null;
	}

	@Override
	public String getSessionPath(Session session) {
		return null;
	}

	@Override
	public String getSessionPath(String corpus, String session) {
		return null;
	}

	@Override
	public UUID getSessionWriteLock(Session session) throws IOException {
		return null;
	}

	@Override
	public UUID getSessionWriteLock(String corpus, String session) throws IOException {
		return null;
	}

	@Override
	public void releaseSessionWriteLock(Session session, UUID writeLock) throws IOException {

	}

	@Override
	public void releaseSessionWriteLock(String corpus, String session, UUID writeLock) throws IOException {

	}

	@Override
	public boolean isSessionLocked(Session session) {
		return false;
	}

	@Override
	public boolean isSessionLocked(String corpus, String session) {
		return false;
	}

	@Override
	public void saveSession(Session session, UUID writeLock) throws IOException {

	}

	@Override
	public void saveSession(String corpus, String sessionName, Session session, UUID writeLock) throws IOException {

	}

	@Override
	public void saveSession(String corpus, String sessionName, Session session, SessionWriter writer, UUID writeLock) throws IOException {

	}

	@Override
	public void removeSession(Session session, UUID writeLock) throws IOException {

	}

	@Override
	public void removeSession(String corpus, String session, UUID writeLock) throws IOException {

	}

	@Override
	public ZonedDateTime getSessionModificationTime(Session session) {
		return null;
	}

	@Override
	public ZonedDateTime getSessionModificationTime(String corpus, String session) {
		return null;
	}

	@Override
	public long getSessionByteSize(Session session) {
		return 0;
	}

	@Override
	public long getSessionByteSize(String corpus, String session) {
		return 0;
	}

	@Override
	public String getResourceLocation() {
		return null;
	}

	@Override
	public void setResourceLocation(String location) {

	}

	@Override
	public InputStream getResourceInputStream(String resourceName) throws IOException {
		return null;
	}

	@Override
	public OutputStream getResourceOutputStream(String resourceName) throws IOException {
		return null;
	}

}
