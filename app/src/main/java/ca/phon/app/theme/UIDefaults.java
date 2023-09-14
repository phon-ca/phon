package ca.phon.app.theme;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stores key/value UI default pairs for colours/fonts/etc.,
 * These values are setup
 */
public final class UIDefaults {

    private static UIDefaults _instance;

    public static UIDefaults getInstance() {
        if(_instance == null) {
            _instance = new UIDefaults();
        }
        return _instance;
    }

    private final HashMap<String, Object> defaultMap = new LinkedHashMap<>();

    private UIDefaults() {}

    public void put(String key, Object value) {
        defaultMap.put(key, value);
        UIManager.put(key, value);
    }

    public Set<String> getKeys() {
        return defaultMap.keySet();
    }

    public Set<String> getColorKeys() {
        return defaultMap.entrySet().stream().filter(es -> es.getValue() instanceof Color)
                .map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    public Object getDefaultValue(String key) {
        return defaultMap.get(key);
    }

}
