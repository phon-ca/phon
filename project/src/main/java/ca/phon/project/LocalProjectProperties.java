package ca.phon.project;

import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.project.io.CorpusType;
import ca.phon.project.io.ProjectType;
import ca.phon.worker.PhonWorker;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extension for {@link LocalProject}s.  This class will be used to
 * load, access, modify and save project properties in the form of
 * a JSON object.
 */
public final class LocalProjectProperties implements ProjectProperties {

    /**
     * Media folders for project
     */
    @Deprecated
    public final static String PROJECT_MEDIAFOLDER_PROP = "project.mediaFolder";
    public final static String PROJECT_MEDIAFOLDERS_KEY = "mediaFolders";
    @Deprecated
    public final static String CORPUS_MEDIAFOLDER_PROP = "corpus.mediaFolder";
    /**
     * Project name property
     */
    @Deprecated
    public final static String PROJECT_NAME_PROP = "project.name";
    public final static String PROJECT_NAME_KEY = "name";
    /**
     * Project UUID property
     */
    @Deprecated
    public final static String PROJECT_UUID_PROP = "project.uuid";
    public final static String PROJECT_UUID_KEY = "uuid";

    private LocalProject localProject;

    /**
     * Project JSON
     */
    private JSONObject projectJson;

    /**
     * Lock for project properties
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public LocalProjectProperties(LocalProject localProject) {
        this.localProject = localProject;
        loadProjectData();
    }

    /**
     * Get the project JSON object
     *
     * @return
     */
    public JSONObject getProjectJson() {
        lock.readLock().lock();
        try {
            return projectJson != null ? new JSONObject(projectJson.toString()) : new JSONObject();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void modifyProjectJson(PropertyModifier modifier) throws IOException {
        lock.writeLock().lock();
        try {
            final JSONObject modifiedJson = modifier.modify(getProjectJson());
            // Update version and save
            this.projectJson = modifiedJson;
            persistToStorage();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Load project properties from Phon 3.x and earlier properties file.  If no properties file is found, the project
     * name and UUID will be set to the project folder name and a new UUID.
     * <p>
     * The old properties file will be deleted after the project data is loaded.
     */
    private void loadProperties() {
        final File oldPropertiesFile = new File(localProject.getLocation(), LocalProject.PREV_PROJECT_PROPERTIES_FILE);
        File propsFile = new File(localProject.getLocation(), LocalProject.PROJECT_PROPERTIES_FILE);
        propsFile = (propsFile.exists() ? propsFile : oldPropertiesFile);

        this.projectJson = new JSONObject();
        List<String> projectMediaFolders = new ArrayList<>();
        if (propsFile.exists()) {
            // load properties
            Properties props = new Properties();
            try (final FileInputStream fin = new FileInputStream(propsFile)) {
                props.load(fin);
            } catch (IOException e) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getLocalizedMessage(), e);
            }

            // convert properties to JSON and save, remove old properties file
            for (String propKey : props.stringPropertyNames()) {
                switch (propKey) {
                    case PROJECT_MEDIAFOLDER_PROP, CORPUS_MEDIAFOLDER_PROP -> {
                        projectMediaFolders.add(props.getProperty(propKey));
                    }

                    case PROJECT_NAME_PROP -> {
                        projectJson.put(PROJECT_NAME_KEY, props.getProperty(propKey));
                    }

                    case PROJECT_UUID_PROP -> {
                        projectJson.put(PROJECT_UUID_KEY, props.getProperty(propKey));
                    }

                    default -> {
                        projectJson.put(propKey, props.getProperty(propKey));
                    }
                }
            }
            if (!projectMediaFolders.isEmpty()) {
                projectJson.put(PROJECT_MEDIAFOLDERS_KEY, projectMediaFolders);
            }

            // add empty properties to avoid runtime issues with plugins
            localProject.putExtension(Properties.class, new Properties());
        } else {
            // add project name and UUID
            this.projectJson.put(PROJECT_NAME_KEY, (new File(localProject.getLocation())).getName());
            this.projectJson.put(PROJECT_UUID_KEY, UUID.randomUUID().toString());
        }
    }

    /**
     * Upgrade project properties from Phon 3.x and earlier properties file to a JSON file.
     *
     * @return true if the properties were upgraded, false otherwise
     */
    public boolean upgradeProjectProperties() {
        lock.writeLock().lock();
        try {
            final File oldPropertiesFile = new File(localProject.getLocation(), LocalProject.PREV_PROJECT_PROPERTIES_FILE);
            if (oldPropertiesFile.exists()) {
                persistToStorage();
                // delete old properties file
                if (!oldPropertiesFile.delete()) {
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unable to delete old properties file: " + oldPropertiesFile.getAbsolutePath());
                }
                return true;
            }
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getLocalizedMessage(), e);
        } finally {
            lock.writeLock().unlock();
        }
        return false;
    }

    private File getFolder() {
        return new File(localProject.getLocation());
    }

    /**
     * Load project data from disk
     * Project data is stored in a JSON file in the project folder with a file ext of .phonproj
     * If opening a Phon 3.x project, the project data will be loaded from the project.properties file
     * and saved to a .phonproj file.
     */
    private void loadProjectData() {
        if (this.projectJson == null) {
            // load json properties if file is found
            final File projectJsonFile = new File(getFolder(), getFolder().getName() + LocalProject.PROJECT_FILE_EXT);
            if (projectJsonFile.exists()) {
                loadProjectJson(projectJsonFile);
            } else {
                // load properties from Phon 3.x and earlier properties file
                loadProperties();
            }
        }
    }

    /**
     * Load project JSON data from a provided file
     *
     * @param projectJsonFile
     */
    protected void loadProjectJson(File projectJsonFile) {
        if (projectJsonFile.exists()) {
            try (final FileInputStream fin = new FileInputStream(projectJsonFile)) {
                // read fin into string
                final byte[] jsonBytes = fin.readAllBytes();
                final String jsonStr = new String(jsonBytes, StandardCharsets.UTF_8);
                this.projectJson = new JSONObject(jsonStr);

                // TODO check json for project properties
            } catch (IOException e) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getLocalizedMessage(), e);
            }
        }
        if (this.projectJson == null) {
            // add project name and UUID
            this.projectJson = new JSONObject();
            this.projectJson.put(PROJECT_NAME_KEY, getFolder().getName());
            this.projectJson.put(PROJECT_UUID_KEY, UUID.randomUUID().toString());
        }
    }

    private void persistToStorage() throws IOException {
        final File projectJsonFile = new File(localProject.getLocation(), localProject.getName() + LocalProject.PROJECT_FILE_EXT);
        try (final FileOutputStream fout = new FileOutputStream(projectJsonFile)) {
            final byte[] jsonBytes = projectJson.toString(2).getBytes(StandardCharsets.UTF_8);
            fout.write(jsonBytes);
            fout.flush();
        }
    }

    public void saveProjectJson() throws IOException {
        lock.writeLock().lock();
        try {
            persistToStorage();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Deprecated
    protected synchronized void saveProperties() throws IOException {
    }
}
