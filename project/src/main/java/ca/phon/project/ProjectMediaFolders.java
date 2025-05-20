package ca.phon.project;

import java.util.List;

/**
 * Interface for project media folders extension. This Project extension will provide additional
 * methods for managing media folders in a project. This is useful for projects that have custom
 * media folder structures or need to manage media files in a specific way.
 */
public interface ProjectMediaFolders {

    /**
     * Has a custom project media folder been assigned
     *
     * @return <code>true</code> if project folder has been customized
     *
     */
    boolean hasCustomProjectMediaFolder();

    /**
     * Get all media folders for the project
     *
     * @return list of media folders
     */
    List<String> getProjectMediaFolders();

    /**
     * Add a media folder to the project
     *
     * @param mediaFolder
     */
    void addProjectMediaFolder(String mediaFolder);

    /**
     * Add a media folder to the project at the specified index
     *
     * @param index
     * @param mediaFolder
     */
    void addProjectMediaFolder(int index, String mediaFolder);

    /**
     * Remove a media folder from the project
     *
     * @param mediaFolder
     */
    void removeProjectMediaFolder(String mediaFolder);

}
