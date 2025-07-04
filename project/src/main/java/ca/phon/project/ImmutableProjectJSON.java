package ca.phon.project;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONPointer;

import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Immutable wrapper for a project JSON object.
 * This class is used to provide read-only access to the project JSON
 * while preventing any modifications.
 */
public class ImmutableProjectJSON extends JSONObject {

    private JSONObject projectJson;

    public ImmutableProjectJSON(JSONObject projectJson) {
        super();
        this.projectJson = projectJson;
    }

    @Override
    public Class<? extends Map> getMapType() {
        return projectJson.getMapType();
    }

    @Override
    public JSONObject accumulate(String key, Object value) throws JSONException {
        // Do nothing for write
        return this;
    }

    @Override
    public JSONObject append(String key, Object value) throws JSONException {
        // Do nothing for write
        return this;
    }

    @Override
    public Object get(String key) throws JSONException {
        return projectJson.get(key);
    }

    @Override
    public <E extends Enum<E>> E getEnum(Class<E> clazz, String key) throws JSONException {
        return projectJson.getEnum(clazz, key);
    }

    @Override
    public boolean getBoolean(String key) throws JSONException {
        return projectJson.getBoolean(key);
    }

    @Override
    public BigInteger getBigInteger(String key) throws JSONException {
        return projectJson.getBigInteger(key);
    }

    @Override
    public BigDecimal getBigDecimal(String key) throws JSONException {
        return projectJson.getBigDecimal(key);
    }

    @Override
    public double getDouble(String key) throws JSONException {
        return projectJson.getDouble(key);
    }

    @Override
    public float getFloat(String key) throws JSONException {
        return projectJson.getFloat(key);
    }

    @Override
    public Number getNumber(String key) throws JSONException {
        return projectJson.getNumber(key);
    }

    @Override
    public int getInt(String key) throws JSONException {
        return projectJson.getInt(key);
    }

    @Override
    public JSONArray getJSONArray(String key) throws JSONException {
        return projectJson.getJSONArray(key);
    }

    @Override
    public JSONObject getJSONObject(String key) throws JSONException {
        return projectJson.getJSONObject(key);
    }

    @Override
    public long getLong(String key) throws JSONException {
        return projectJson.getLong(key);
    }

    @Override
    public String getString(String key) throws JSONException {
        return projectJson.getString(key);
    }

    @Override
    public boolean has(String key) {
        return projectJson.has(key);
    }

    @Override
    public JSONObject increment(String key) throws JSONException {
        // Do nothing for write
        return this;
    }

    @Override
    public boolean isNull(String key) {
        return projectJson.isNull(key);
    }

    @Override
    public Iterator<String> keys() {
        return projectJson.keys();
    }

    @Override
    public Set<String> keySet() {
        return projectJson.keySet();
    }

    @Override
    protected Set<Map.Entry<String, Object>> entrySet() {
        return projectJson.toMap().entrySet();
    }

    @Override
    public int length() {
        return projectJson.length();
    }

    @Override
    public void clear() {
        // Do nothing for write
    }

    @Override
    public boolean isEmpty() {
        return projectJson.isEmpty();
    }

    @Override
    public JSONArray names() {
        return projectJson.names();
    }

    @Override
    public Object opt(String key) {
        return projectJson.opt(key);
    }

    @Override
    public <E extends Enum<E>> E optEnum(Class<E> clazz, String key) {
        return projectJson.optEnum(clazz, key);
    }

    @Override
    public <E extends Enum<E>> E optEnum(Class<E> clazz, String key, E defaultValue) {
        return projectJson.optEnum(clazz, key, defaultValue);
    }

    @Override
    public boolean optBoolean(String key) {
        return projectJson.optBoolean(key);
    }

    @Override
    public boolean optBoolean(String key, boolean defaultValue) {
        return projectJson.optBoolean(key, defaultValue);
    }

    @Override
    public Boolean optBooleanObject(String key) {
        return projectJson.optBooleanObject(key);
    }

    @Override
    public Boolean optBooleanObject(String key, Boolean defaultValue) {
        return projectJson.optBooleanObject(key, defaultValue);
    }

    @Override
    public BigDecimal optBigDecimal(String key, BigDecimal defaultValue) {
        return projectJson.optBigDecimal(key, defaultValue);
    }

    @Override
    public BigInteger optBigInteger(String key, BigInteger defaultValue) {
        return projectJson.optBigInteger(key, defaultValue);
    }

    @Override
    public double optDouble(String key) {
        return projectJson.optDouble(key);
    }

    @Override
    public double optDouble(String key, double defaultValue) {
        return projectJson.optDouble(key, defaultValue);
    }

    @Override
    public Double optDoubleObject(String key) {
        return projectJson.optDoubleObject(key);
    }

    @Override
    public Double optDoubleObject(String key, Double defaultValue) {
        return projectJson.optDoubleObject(key, defaultValue);
    }

    @Override
    public float optFloat(String key) {
        return projectJson.optFloat(key);
    }

    @Override
    public float optFloat(String key, float defaultValue) {
        return projectJson.optFloat(key, defaultValue);
    }

    @Override
    public Float optFloatObject(String key) {
        return projectJson.optFloatObject(key);
    }

    @Override
    public Float optFloatObject(String key, Float defaultValue) {
        return projectJson.optFloatObject(key, defaultValue);
    }

    @Override
    public int optInt(String key) {
        return projectJson.optInt(key);
    }

    @Override
    public int optInt(String key, int defaultValue) {
        return projectJson.optInt(key, defaultValue);
    }

    @Override
    public Integer optIntegerObject(String key) {
        return projectJson.optIntegerObject(key);
    }

    @Override
    public Integer optIntegerObject(String key, Integer defaultValue) {
        return projectJson.optIntegerObject(key, defaultValue);
    }

    @Override
    public JSONArray optJSONArray(String key) {
        return projectJson.optJSONArray(key);
    }

    @Override
    public JSONArray optJSONArray(String key, JSONArray defaultValue) {
        return projectJson.optJSONArray(key, defaultValue);
    }

    @Override
    public JSONObject optJSONObject(String key) {
        return projectJson.optJSONObject(key);
    }

    @Override
    public JSONObject optJSONObject(String key, JSONObject defaultValue) {
        return projectJson.optJSONObject(key, defaultValue);
    }

    @Override
    public long optLong(String key) {
        return projectJson.optLong(key);
    }

    @Override
    public long optLong(String key, long defaultValue) {
        return projectJson.optLong(key, defaultValue);
    }

    @Override
    public Long optLongObject(String key) {
        return projectJson.optLongObject(key);
    }

    @Override
    public Long optLongObject(String key, Long defaultValue) {
        return projectJson.optLongObject(key, defaultValue);
    }

    @Override
    public Number optNumber(String key) {
        return projectJson.optNumber(key);
    }

    @Override
    public Number optNumber(String key, Number defaultValue) {
        return projectJson.optNumber(key, defaultValue);
    }

    @Override
    public String optString(String key) {
        return projectJson.optString(key);
    }

    @Override
    public String optString(String key, String defaultValue) {
        return projectJson.optString(key, defaultValue);
    }

    @Override
    public JSONObject put(String key, boolean value) throws JSONException {
        // Do nothing for write
        return this;
    }

    @Override
    public JSONObject put(String key, Collection<?> value) throws JSONException {
        // Do nothing for write
        return this;
    }

    @Override
    public JSONObject put(String key, double value) throws JSONException {
        // Do nothing for write
        return this;
    }

    @Override
    public JSONObject put(String key, float value) throws JSONException {
        // Do nothing for write
        return this;
    }

    @Override
    public JSONObject put(String key, int value) throws JSONException {
        // Do nothing for write
        return this;
    }

    @Override
    public JSONObject put(String key, long value) throws JSONException {
        // Do nothing for write
        return this;
    }

    @Override
    public JSONObject put(String key, Map<?, ?> value) throws JSONException {
        // Do nothing for write
        return this;
    }

    @Override
    public JSONObject put(String key, Object value) throws JSONException {
        // Do nothing for write
        return this;
    }

    @Override
    public JSONObject putOnce(String key, Object value) throws JSONException {
        // Do nothing for write
        return this;
    }

    @Override
    public JSONObject putOpt(String key, Object value) throws JSONException {
        // Do nothing for write
        return this;
    }

    @Override
    public Object query(String jsonPointer) {
        return projectJson.query(jsonPointer);
    }

    @Override
    public Object query(JSONPointer jsonPointer) {
        return projectJson.query(jsonPointer);
    }

    @Override
    public Object optQuery(String jsonPointer) {
        return projectJson.optQuery(jsonPointer);
    }

    @Override
    public Object optQuery(JSONPointer jsonPointer) {
        return projectJson.optQuery(jsonPointer);
    }

    @Override
    public Object remove(String key) {
        // Do nothing for write
        return null;
    }

    @Override
    public boolean similar(Object other) {
        return projectJson.similar(other);
    }

    @Override
    public JSONArray toJSONArray(JSONArray names) throws JSONException {
        return projectJson.toJSONArray(names);
    }

    @Override
    public String toString() {
        return projectJson.toString();
    }

    @Override
    public String toString(int indentFactor) throws JSONException {
        return projectJson.toString(indentFactor);
    }

    @Override
    public Writer write(Writer writer) throws JSONException {
        return projectJson.write(writer);
    }

    @Override
    public Writer write(Writer writer, int indentFactor, int indent) throws JSONException {
        return projectJson.write(writer, indentFactor, indent);
    }

    @Override
    public Map<String, Object> toMap() {
        return projectJson.toMap();
    }
}
