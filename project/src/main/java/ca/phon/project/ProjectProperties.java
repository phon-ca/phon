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
     * Get the project JSON object.
     *
     * @return a copy of the project JSON object
     */
    public JSONObject getProjectJson();

    /**
     * Atomically modify the project JSON object
     *
     * @param modifier function that receives current JSON and returns modified JSON
     * @throws IOException on error
     */
    public void modifyProjectJson(PropertyModifier modifier) throws IOException;

    /**
     * Functional interface for atomic property modifications
     */
    @FunctionalInterface
    public interface PropertyModifier {
        JSONObject modify(JSONObject currentJson);
    }

}
