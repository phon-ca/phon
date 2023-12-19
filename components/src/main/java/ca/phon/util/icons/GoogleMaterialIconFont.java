package ca.phon.util.icons;

import jiconfont.DefaultIconCode;
import jiconfont.IconCode;
import jiconfont.IconFont;
import jiconfont.swing.IconFontSwing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class GoogleMaterialIconFont implements IconFont {

    public static Map<GoogleMaterialStaticFonts, GoogleMaterialIconFont> staticFonts = new HashMap<>();

    public static void registerFonts() {
        for(GoogleMaterialStaticFonts font:GoogleMaterialStaticFonts.values()) {
            final GoogleMaterialIconFont iconFont = new GoogleMaterialIconFont(font);
            IconFontSwing.register(iconFont);
            staticFonts.put(font, iconFont);
        }
    }

    public static GoogleMaterialIconFont getIconFont(GoogleMaterialStaticFonts font) {
        return staticFonts.get(font);
    }

    private final GoogleMaterialStaticFonts font;

    private final Map<String, Character> codepoints;

    private GoogleMaterialIconFont(GoogleMaterialStaticFonts font) {
        this.font = font;
        this.codepoints = readCodepoints();
    }

    @Override
    public InputStream getFontInputStream() {
        return GoogleMaterialIconFont.class.getClassLoader().getResourceAsStream(font.getFontFile());
    }

    @Override
    public String getFontFamily() {
        return font.getFontName();
    }

    /**
     * Read codepoints from file
     *
     * @return map of icon names to codepoints
     */
    public Map<String, Character> readCodepoints() {
        final Map<String, Character> retVal = new HashMap<>();
        try {
            final InputStream is = GoogleMaterialIconFont.class.getClassLoader().getResourceAsStream(font.getCodepointFile());
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while((line = reader.readLine()) != null) {
                final String[] parts = line.split(" ");
                retVal.put(parts[0], (char)Integer.parseInt(parts[1], 16));
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return retVal;
    }

    public IconCode getIconCode(String iconName) {
        return new DefaultIconCode(font.getFontName(), codepoints.get(iconName.toLowerCase()));
    }

}
