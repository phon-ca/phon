package ca.phon.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for project resources extension for {@link Project}s
 */
public interface ProjectResources {

    /**
     * Get location of project resources folder.
     *
     * @return location of project resources folder
     */
    public String getResourceLocation();

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
     * does not exist, it is created.  If the resource already exists,
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
