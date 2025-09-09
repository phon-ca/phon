# User Preferences Guide

This guide explains how user preferences are implemented and used throughout the Phon codebase. Understanding this system is essential for developers who need to store and retrieve application settings, configuration values, and user customizations.

## Overview

Phon's preference system is built around the Java `Preferences` API and provides a centralized way to manage user settings across the application. The system supports multiple data types, command-line overrides, and a hierarchical preference structure.

## Architecture

### Core Components

1. **`PrefHelper`** (`core/src/main/java/ca/phon/util/PrefHelper.java`) - Central utility class for preference access
2. **`PhonProperties`** (`app/src/main/java/ca/phon/app/prefs/PhonProperties.java`) - Constants for property keys
3. **`PrefsPanel`** (`app/src/main/java/ca/phon/app/prefs/PrefsPanel.java`) - Base class for preference UI panels
4. **`PrefsDialog`** (`app/src/main/java/ca/phon/app/prefs/PrefsDialog.java`) - Main preferences dialog

### Preference Hierarchy

The preference system searches for values in the following order:

1. **System Properties** - Command-line arguments (`-Dkey=value`)
2. **User Preferences** - Java Preferences API user node
3. **Default Values** - Hardcoded fallback values

This hierarchy allows for flexible configuration where system administrators can override settings via command-line arguments, users can customize through the UI, and developers provide sensible defaults.

## Key Classes and Methods

### PrefHelper Class

The `PrefHelper` class is the primary interface for preference operations:

```java
// Root preference node
public final static String PREF_ROOT = "/ca/phon4/prefs";

// Get user preferences node
public static Preferences getUserPreferences()

// Basic getters with type conversion
public static String get(String key, String def)
public static Integer getInt(String key, Integer def)
public static Boolean getBoolean(String key, Boolean def)
public static Float getFloat(String key, Float def)
public static Double getDouble(String key, Double def)
public static Long getLong(String key, Long def)

// Advanced getters
public static <T extends Enum<?>> T getEnum(Class<T> enumClazz, String key, T def)
public static Color getColor(String key, Color def)
public static Font getFont(String key, Font def)
public static byte[] getByteArray(String key, byte[] def)
public static <T extends Serializable> T getSerializedObject(String key, Class<T> type, T def)
public static Object getFormattedObject(String key, Format format, Object def)
```

### PhonProperties Class

Defines standard property keys used throughout the application:

```java
public class PhonProperties {
    public final static String DEBUG = "phon.debug";
    public final static String AUTOSAVE_INTERVAL = AutosaveManager.AUTOSAVE_INTERVAL_PROP;
    public final static String WORKSPACE_FOLDER = Workspace.WORKSPACE_FOLDER;
    public final static String SYLLABIFIER_LANGUAGE = SyllabifierLibrary.DEFAULT_SYLLABIFIER_LANG_PROP;
    public final static String IPADICTIONARY_LANGUAGE = IPADictionaryLibrary.DEFAULT_IPA_DICTIONARY_PROP;
    // ... more constants
}
```

## Data Type Support

### Primitive Types

```java
// String preferences
String value = PrefHelper.get("my.string.key", "default");
PrefHelper.getUserPreferences().put("my.string.key", "new value");

// Integer preferences
Integer count = PrefHelper.getInt("my.int.key", 42);
PrefHelper.getUserPreferences().putInt("my.int.key", 100);

// Boolean preferences
Boolean enabled = PrefHelper.getBoolean("my.bool.key", true);
PrefHelper.getUserPreferences().putBoolean("my.bool.key", false);

// Float preferences
Float scale = PrefHelper.getFloat("my.float.key", 1.0f);
PrefHelper.getUserPreferences().putFloat("my.float.key", 1.5f);
```

### Complex Types

```java
// Enum preferences
MyEnum value = PrefHelper.getEnum(MyEnum.class, "my.enum.key", MyEnum.DEFAULT);
// Enums are stored as strings using toString()

// Color preferences
Color color = PrefHelper.getColor("my.color.key", Color.BLACK);
// Colors are stored as hex strings and decoded using Color.decode()

// Font preferences
Font font = PrefHelper.getFont("my.font.key", Font.decode("Arial-PLAIN-12"));
// Fonts are stored as strings using Font.decode() format

// Serializable objects
MyObject obj = PrefHelper.getSerializedObject("my.object.key", MyObject.class, defaultObj);
// Objects are base64-encoded and stored as byte arrays
```

## Common Usage Patterns

### Reading Preferences

```java
// Simple string preference with default
String workspace = PrefHelper.get(PhonProperties.WORKSPACE_FOLDER,
                                  Workspace.defaultWorkspaceFolder().getAbsolutePath());

// Boolean preference with system property override
boolean debug = PrefHelper.getBoolean("phon.debug", false);

// Integer preference for UI settings
Integer autosaveInterval = PrefHelper.getInt(PhonProperties.AUTOSAVE_INTERVAL, 0);
```

### Writing Preferences

```java
// Direct preference storage
PrefHelper.getUserPreferences().put("key", "value");
PrefHelper.getUserPreferences().putBoolean("enabled", true);
PrefHelper.getUserPreferences().putInt("count", 42);

// Complex object storage
Font selectedFont = new Font("Arial", Font.PLAIN, 14);
PrefHelper.getUserPreferences().put("ui.font", FontFormatter.format(selectedFont));
```

### Preference-Backed Configuration Classes

Many classes provide convenience methods that wrap preference access:

```java
public class FontPreferences {
    private static final String TIER_FONT = "ca.phon.ui.fonts.tierFont";
    private static final String DEFAULT_TIER_FONT = "Noto Sans-PLAIN-14";

    public static Font getTierFont() {
        return PrefHelper.getFont(TIER_FONT, Font.decode(DEFAULT_TIER_FONT));
    }

    public static void setTierFont(Font font) {
        PrefHelper.getUserPreferences().put(TIER_FONT, fontToString(font));
    }
}
```

## Platform-Specific Behavior

### File System Locations

```java
// User data folders (platform-specific)
public static String getUserDataFolder() {
    String userHomePath = System.getProperty("user.home");

    if(OSInfo.isMacOs()) {
        return userHomePath + "/Library/Application Support/Phon4";
    } else if(OSInfo.isWindows()) {
        return System.getenv("APPDATA") + "/Phon4";
    } else {
        return userHomePath + "/.phon4";
    }
}

// Document folders
public static String getUserDocumentsPhonFolder() {
    File phonDocs = new File(getUserDocumentsFolder(), "Phon4");
    return phonDocs.getAbsolutePath();
}
```

## UI Integration

### Preference Panels

Preference panels extend `PrefsPanel` and can be added to the main preferences dialog:

```java
public class MyPrefsPanel extends PrefsPanel {
    public MyPrefsPanel() {
        super("My Settings"); // Tab title
        init();
    }

    private void init() {
        // Create UI components that read/write preferences
        JCheckBox enabledBox = new JCheckBox("Enable Feature");
        enabledBox.setSelected(PrefHelper.getBoolean("my.feature.enabled", false));
        enabledBox.addActionListener(e ->
            PrefHelper.getUserPreferences().putBoolean("my.feature.enabled",
                                                      enabledBox.isSelected()));
    }
}
```

### Plugin Extension Points

Preference panels can be added via the plugin system:

```java
@PhonPlugin(name="MyPrefs")
public class MyPrefsPanelExtension implements IPluginExtensionPoint<PrefsPanel> {
    @Override
    public Class<?> getExtensionType() {
        return PrefsPanel.class;
    }

    @Override
    public IPluginExtensionFactory<PrefsPanel> getFactory() {
        return (args) -> new MyPrefsPanel();
    }
}
```

## Best Practices

### Naming Conventions

1. **Use reverse domain notation**: `ca.phon.module.settingName`
2. **Group related settings**: `ca.phon.ui.fonts.tierFont`, `ca.phon.ui.fonts.monospaceFont`
3. **Include class name for class-specific settings**: `MyClass.class.getName() + ".property"`

### Property Key Management

```java
public class MyConfigClass {
    // Centralize property keys as constants
    private static final String ENABLED_PROP = MyClass.class.getName() + ".enabled";
    private static final String COUNT_PROP = MyClass.class.getName() + ".count";

    // Provide default values as constants
    private static final boolean DEFAULT_ENABLED = true;
    private static final int DEFAULT_COUNT = 10;

    public static boolean isEnabled() {
        return PrefHelper.getBoolean(ENABLED_PROP, DEFAULT_ENABLED);
    }

    public static void setEnabled(boolean enabled) {
        PrefHelper.getUserPreferences().putBoolean(ENABLED_PROP, enabled);
    }
}
```

### Command-Line Override Support

Most preference getters automatically check system properties first, allowing command-line overrides:

```bash
# Override workspace location
java -Dca.phon.app.workspace.Workspace.workspaceFolder=/path/to/workspace -jar phon.jar

# Enable debug mode
java -Dphon.debug=true -jar phon.jar

# Set autosave interval
java -Dca.phon.app.autosave.AutosaveManager.autosaveInterval=5 -jar phon.jar
```

### Error Handling

```java
// Always provide defaults and handle exceptions gracefully
Font font = PrefHelper.getFont("my.font", Font.decode("Arial-PLAIN-12"));

// The PrefHelper methods handle NumberFormatException internally
Integer value = PrefHelper.getInt("my.number", 42); // Never throws

// For complex operations, add additional validation
String path = PrefHelper.get("my.path", defaultPath);
if (!new File(path).exists()) {
    path = defaultPath; // Fallback to default if invalid
}
```

### Memory and Performance

1. **Preferences are cached** - Multiple calls to the same key are efficient
2. **Sync is automatic** - `PrefHelper.getUserPreferences()` calls `sync()` internally
3. **Avoid frequent writes** - Batch preference updates when possible

```java
// Good: Batch multiple updates
Preferences prefs = PrefHelper.getUserPreferences();
prefs.put("key1", "value1");
prefs.put("key2", "value2");
prefs.putInt("key3", 42);

// Avoid: Frequent individual writes in loops
for (int i = 0; i < 1000; i++) {
    PrefHelper.getUserPreferences().put("item" + i, "value" + i); // Inefficient
}
```

## Example Implementation

Here's a complete example of a preference-backed configuration class:

```java
package ca.phon.example;

import ca.phon.util.PrefHelper;
import java.awt.Color;
import java.awt.Font;

public class ExamplePreferences {

    // Property key constants
    private static final String WINDOW_WIDTH = ExamplePreferences.class.getName() + ".windowWidth";
    private static final String WINDOW_HEIGHT = ExamplePreferences.class.getName() + ".windowHeight";
    private static final String BACKGROUND_COLOR = ExamplePreferences.class.getName() + ".backgroundColor";
    private static final String UI_FONT = ExamplePreferences.class.getName() + ".uiFont";
    private static final String AUTO_SAVE = ExamplePreferences.class.getName() + ".autoSave";

    // Default values
    private static final int DEFAULT_WINDOW_WIDTH = 800;
    private static final int DEFAULT_WINDOW_HEIGHT = 600;
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    private static final Font DEFAULT_UI_FONT = Font.decode("SansSerif-PLAIN-12");
    private static final boolean DEFAULT_AUTO_SAVE = true;

    // Getters
    public static int getWindowWidth() {
        return PrefHelper.getInt(WINDOW_WIDTH, DEFAULT_WINDOW_WIDTH);
    }

    public static int getWindowHeight() {
        return PrefHelper.getInt(WINDOW_HEIGHT, DEFAULT_WINDOW_HEIGHT);
    }

    public static Color getBackgroundColor() {
        return PrefHelper.getColor(BACKGROUND_COLOR, DEFAULT_BACKGROUND_COLOR);
    }

    public static Font getUIFont() {
        return PrefHelper.getFont(UI_FONT, DEFAULT_UI_FONT);
    }

    public static boolean isAutoSaveEnabled() {
        return PrefHelper.getBoolean(AUTO_SAVE, DEFAULT_AUTO_SAVE);
    }

    // Setters
    public static void setWindowWidth(int width) {
        PrefHelper.getUserPreferences().putInt(WINDOW_WIDTH, width);
    }

    public static void setWindowHeight(int height) {
        PrefHelper.getUserPreferences().putInt(WINDOW_HEIGHT, height);
    }

    public static void setBackgroundColor(Color color) {
        // Colors are stored as hex strings
        PrefHelper.getUserPreferences().put(BACKGROUND_COLOR,
            String.format("#%06X", color.getRGB() & 0xFFFFFF));
    }

    public static void setUIFont(Font font) {
        // Fonts are stored using the Font.decode() format
        PrefHelper.getUserPreferences().put(UI_FONT,
            font.getName() + "-" +
            (font.isBold() ? "BOLD" : font.isItalic() ? "ITALIC" : "PLAIN") +
            "-" + font.getSize());
    }

    public static void setAutoSaveEnabled(boolean enabled) {
        PrefHelper.getUserPreferences().putBoolean(AUTO_SAVE, enabled);
    }

    // Utility method to reset all preferences to defaults
    public static void resetToDefaults() {
        Preferences prefs = PrefHelper.getUserPreferences();
        prefs.putInt(WINDOW_WIDTH, DEFAULT_WINDOW_WIDTH);
        prefs.putInt(WINDOW_HEIGHT, DEFAULT_WINDOW_HEIGHT);
        prefs.put(BACKGROUND_COLOR, String.format("#%06X", DEFAULT_BACKGROUND_COLOR.getRGB() & 0xFFFFFF));
        prefs.put(UI_FONT, DEFAULT_UI_FONT.getName() + "-PLAIN-" + DEFAULT_UI_FONT.getSize());
        prefs.putBoolean(AUTO_SAVE, DEFAULT_AUTO_SAVE);
    }
}
```

## Common Preference Categories

The Phon application organizes preferences into several categories:

### General Preferences

- Application updates checking
- Workspace folder location
- Debug mode settings
- UI themes

### Session Editor Preferences

- Dictionary languages
- Syllabifier settings
- Autosave intervals
- Backup options
- Media file checking

### Font Preferences

- Tier fonts (IPA transcription display)
- Monospace fonts (console output)
- UI fonts (general interface)
- Font size scaling

### Media Preferences

- Media search paths
- Audio/video file handling
- Media file validation

### Query Preferences

- Report loading strategies
- History management
- Default query settings

## Testing Preferences

When testing code that uses preferences:

```java
@Test
public void testPreferenceBehavior() {
    // Save original value
    String original = PrefHelper.get("test.key", null);

    try {
        // Set test value
        PrefHelper.getUserPreferences().put("test.key", "test-value");

        // Test your code
        assertEquals("test-value", MyClass.getTestSetting());

    } finally {
        // Restore original value
        if (original != null) {
            PrefHelper.getUserPreferences().put("test.key", original);
        } else {
            PrefHelper.getUserPreferences().remove("test.key");
        }
    }
}
```

## Migration and Compatibility

When changing preference keys or formats:

1. **Provide migration code** for existing installations
2. **Support old keys temporarily** during transition periods
3. **Document breaking changes** in release notes
4. **Test with empty preference stores** for new installations

```java
// Example migration from old to new preference key
public static String getMigratedSetting() {
    String newValue = PrefHelper.get(NEW_KEY, null);
    if (newValue != null) {
        return newValue;
    }

    // Check for old preference and migrate
    String oldValue = PrefHelper.get(OLD_KEY, null);
    if (oldValue != null) {
        PrefHelper.getUserPreferences().put(NEW_KEY, oldValue);
        PrefHelper.getUserPreferences().remove(OLD_KEY); // Clean up
        return oldValue;
    }

    return DEFAULT_VALUE;
}
```

This comprehensive preference system provides the foundation for all user customization and configuration in Phon, ensuring consistent behavior across the application while supporting both simple and complex data types.
