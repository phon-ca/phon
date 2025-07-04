package ca.phon.syllabifier;

import ca.phon.phonex.PhonexPattern;
import ca.phon.phonex.PhonexPatternException;
import ca.phon.util.Language;
import ca.phon.util.resources.ClassLoaderHandler;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface used by {@link java.util.ServiceLoader} to automatically find resource handlers for syllabifiers.
 * JSON syllabifiers are read from a list of JSON files found at resource path 'syllabiifer/json.list'.
 *
 */
public class JsonSyllabifierProvider extends ClassLoaderHandler<Syllabifier> implements SyllabifierProvider {

    private final static String LIST = "syllabifier/json.list";

    /**
     * Constructor
     */
    public JsonSyllabifierProvider() {
        super();
        super.loadResourceFile(LIST);
    }

    @Override
    public Syllabifier loadFromURL(URL url) throws IOException {
        try {
            final JsonReader reader = Json.createReader(url.openStream());
            final JsonObject json = reader.readObject();
            return loadFromJson(json);
        } catch (IOException e) {
            throw e;
        }
    }

    private BasicSyllabifier loadFromJson(JsonObject json) throws IOException {
        final String name = json.getString("name");
        if(name == null) throw new IOException("Missing 'name' field in JSON");
        final String lang = json.getString("language");
        if(lang == null) throw new IOException("Missing 'language' field in JSON");
        final Language language = Language.parseLanguage(lang);

        final List<BasicSyllabifier.SonorityClass> sonorityClasses = new ArrayList<>();
        final List<BasicSyllabifier.SyllabifierStep> syllabifierSteps = new ArrayList<>();

        final JsonArray sonorityClassesObj = json.getJsonArray("sonority");
        if(sonorityClassesObj == null) throw new IOException("Missing 'sonority' field in JSON");
        for(JsonObject sonorityObj:sonorityClassesObj.getValuesAs(JsonObject.class)) {
            try {
                final PhonexPattern phonexPattern = PhonexPattern.compile(sonorityObj.getString("pattern"));
                final int sonority = sonorityObj.getInt("sonority");
                sonorityClasses.add(new BasicSyllabifier.SonorityClass(sonority, phonexPattern));
            } catch (PhonexPatternException e) {
                throw new IOException(e);
            }
        }

        final JsonArray rulesObj = json.getJsonArray("steps");
        if(rulesObj == null) throw new IOException("Missing 'steps' field in JSON");
        for(JsonObject ruleObj:rulesObj.getValuesAs(JsonObject.class)) {
            try {
                final String ruleName = ruleObj.getString("name");
                final PhonexPattern pattern = PhonexPattern.compile(ruleObj.getString("pattern"));
                final BasicSyllabifier.SyllabifierStep step = new BasicSyllabifier.SyllabifierStep(ruleName, pattern);
                syllabifierSteps.add(step);
            } catch (PhonexPatternException e) {
                throw new IOException(e);
            }
        }

        return new BasicSyllabifier(name, language, sonorityClasses, syllabifierSteps);
    }

}
