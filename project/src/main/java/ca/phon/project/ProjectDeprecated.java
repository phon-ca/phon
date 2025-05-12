package ca.phon.project;

import java.util.List;

/**
 * Interface for deprecated project API extension
 *
 * This interface is used to mark deprecated project API
 * which may still be implemented by some project types.
 */
public interface ProjectDeprecated {

    /**
     * Get the list of corpora in this project.  Corpus names
     * are returned in alphabetical order.
     *
     * @return list of corpora
     *
     * @deprecated use {@link Project#getCorpusIterator()} instead
     */
    @Deprecated
    public List<String> getCorpora();

    /**
     * Get media folder for the project, if any are set.  If multiple media folders
     * are set, the first one is returned.
     *
     * @deprecated Since Phon 4.0 use {@link Project#getProjectMediaFolders()} instead
     */
    @Deprecated
    public String getProjectMediaFolder();

    /**
     * Set media folder for project, if any are set.  If multiple media folders
     * are set, then all are removed and the new folder is added.
     *
     * @param mediaFolder If <code>null</code> sets the media folder
     * back to default.
     *
     * @deprecated Since Phon 4.0 use {@link Project#addProjectMediaFolder(String)} instead
     */
    @Deprecated
    public void setProjectMediaFolder(String mediaFolder);

    /**
     * Has a custom corpus media folder been assigned
     *
     * @param corpus
     * @return <code>true</code> if a custom media folder is assigned for the
     * given corpus
     *
     * @deprecated Since Phon 4.0 will always return <code>false</code>
     */
    @Deprecated
    public boolean hasCustomCorpusMediaFolder(String corpus);

    /**
     * Get the media folder for the specified corpus.
     *
     * @return mediaFolder or the project media folder if not specified
     *
     * @deprecated Since Phon 4.0 will always return null
     */
    @Deprecated
    public String getCorpusMediaFolder(String corpus);

    /**
     * Set the media folder for the specified corpus.
     *
     * @param mediaFolder
     *
     * @deprecated Since Phon 4.0 will do nothing
     */
    @Deprecated
    public void setCorpusMediaFolder(String corpus, String mediaFolder);

    /**
     * Get the session names contained in a corpus in alphabetical
     * order.
     *
     * @param corpus
     *
     * @return the list of sessions in the specified corpus
     *
     * @deprecated use {@link Project#getSessionIterator(String)} instead
     */
    @Deprecated
    public List<String> getCorpusSessions(String corpus);

}
