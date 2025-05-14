package ca.phon.project;

import java.util.List;

/**
 * Interface for project events extension for {@link Project}s
 *
 * Projects which support event notifications should implement this
 * interface.
 */
public interface ProjectEvents {

    /**
     * Get the list of project listeners
     *
     * @return list of project listeners
     */
    public List<ProjectListener> getProjectListeners();

    /**
     * Add a project listener
     *
     * @param listener
     */
    public void addProjectListener(ProjectListener listener);

    /**
     * Remove a project listener
     * @param listener
     */
    public void removeProjectListener(ProjectListener listener);

    /**
     * Fire a project structure changed event
     *
     * @param pe
     */
    public void fireProjectStructureChanged(ProjectEvent pe);

    /**
     * Fire a project data changed event
     *
     * @param pe
     */
    public void fireProjectDataChanged(ProjectEvent pe);

    /**
     * Fire a project write locks changed event
     *
     * @param pe
     */
    public void fireProjectWriteLocksChanged(ProjectEvent pe);

}
