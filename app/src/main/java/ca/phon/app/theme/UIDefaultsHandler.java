package ca.phon.app.theme;

/**
 * Installs custom UI defaults to the java {@link javax.swing.UIManager} on application startup.
 */
public interface UIDefaultsHandler {

    /**
     * Install custom UI property key/value pairs (colors, fonts, etc.,)
     * during application startup to ensure a default value is available.
     */
    void setupDefaults();

}
