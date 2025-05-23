package ca.phon.project;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Extension for {@link Project}s.  This class will be used to
 * load, access, modify and save project properties in the form of
 * a JSON object.
 */
public interface ProjectProperties {

    /**
     * Get the project JSON object
     *
     * @return
     */
    public JSONObject getProjectJson();

    /**
     * Save the project JSON object
     *
     * @throws IOException on error.  If the project does not support
     * mutable properties, an IOException will be thrown as well.
     */
    public void saveProjectJson() throws IOException;

}
