package ca.phon.ui.ipa;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.ipa.SyllableConstituentType;
import ca.phon.ui.theme.UIDefaults;
import ca.phon.ui.theme.UIDefaultsHandler;

import javax.swing.*;
import java.awt.*;

public class IPAUIDefaults implements UIDefaultsHandler, IPluginExtensionPoint<UIDefaultsHandler> {

    /**
     * Syllable constituent type colors
     */
    public final static String SYLLABLE_CONSTITUENT_LEFT_APPENDIX_COLOR = "Phon.syllableConstituent.leftAppendixColor";
    public final static Color DEFAULT_SYLLABLE_CONSTITUENT_LEFT_APPENDIX_COLOR = Color.decode("0xD1B45F");

    public final static String SYLLABLE_CONSTITUENT_ONSET_COLOR = "Phon.syllableConstituent.onsetColor";
    public final static Color DEFAULT_SYLLABLE_CONSTITUENT_ONSET_COLOR = Color.decode("0x6A8EAE");

    public final static String SYLLABLE_CONSTITUENT_NUCLEUS_COLOR = "Phon.syllableConstituent.nucleusColor";
    public final static Color DEFAULT_SYLLABLE_CONSTITUENT_NUCLEUS_COLOR = Color.decode("0xB85C5C");

    public final static String SYLLABLE_CONSTITUENT_CODA_COLOR = "Phon.syllableConstituent.codaColor";
    public final static Color DEFAULT_SYLLABLE_CONSTITUENT_CODA_COLOR =  Color.decode("0x7A947A");

    public final static String SYLLABLE_CONSTITUENT_RIGHT_APPENDIX_COLOR = "Phon.syllableConstituent.rightAppendixColor";
    public final static Color DEFAULT_SYLLABLE_CONSTITUENT_RIGHT_APPENDIX_COLOR = Color.decode("0xD98C5F");

    public final static String SYLLABLE_CONSTITUENT_OEHS_COLOR = "Phon.syllableConstituent.oehsColor";
    public final static Color DEFAULT_SYLLABLE_CONSTITUENT_OEHS_COLOR = Color.decode("0x5F9EA0");

    public final static String SYLLABLE_CONSTITUENT_AMBISYLLABIC_COLOR = "Phon.syllableConstituent.ambisyllabicColor";
    public final static Color DEFAULT_SYLLABLE_CONSTITUENT_AMBISYLLABIC_COLOR = Color.decode("0x607D8B");

    public final static String SYLLABLE_CONSTITUENT_UNKNOWN_COLOR = "Phon.syllableConstituent.unknownColor";
    public final static Color DEFAULT_SYLLABLE_CONSTITUENT_UNKNOWN_COLOR = UIManager.getColor("text");

    public final static String SYLLABLE_CONSTITUENT_SYLLABLE_BOUNDARY_MARKER_COLOR = "Phon.syllableConstituent.syllableBoundaryMarkerColor";
    public final static Color DEFAULT_SYLLABLE_CONSTITUENT_SYLLABLE_BOUNDARY_MARKER_COLOR = UIManager.getColor("text");

    public final static String SYLLABLE_CONSTITUENT_SYLLABLE_STRESS_MARKER_COLOR = "Phon.syllableConstituent.syllableStressMarkerColor";
    public final static Color DEFAULT_SYLLABLE_CONSTITUENT_SYLLABLE_STRESS_MARKER_COLOR = UIManager.getColor("text");

    public final static String SYLLABLE_CONSTITUENT_WORD_BOUNDARY_MARKER_COLOR = "Phon.syllableConstituent.wordBoundaryMarkerColor";
    public final static Color DEFAULT_SYLLABLE_CONSTITUENT_WORD_BOUNDARY_MARKER_COLOR = UIManager.getColor("text");

    public final static String SYLLABLE_CONSTITUENT_TONENUMBER_COLOR = "Phon.syllableConstituent.toneNumberColor";
    public final static Color DEFAULT_SYLLABLE_CONSTITUENT_TONENUMBER_COLOR = UIManager.getColor("text");

    public IPAUIDefaults() {
        super();
    }

    @Override
    public void setupDefaults(UIDefaults defaults) {
        defaults.put(SYLLABLE_CONSTITUENT_LEFT_APPENDIX_COLOR, DEFAULT_SYLLABLE_CONSTITUENT_LEFT_APPENDIX_COLOR);
        defaults.put(SYLLABLE_CONSTITUENT_ONSET_COLOR, DEFAULT_SYLLABLE_CONSTITUENT_ONSET_COLOR);
        defaults.put(SYLLABLE_CONSTITUENT_NUCLEUS_COLOR, DEFAULT_SYLLABLE_CONSTITUENT_NUCLEUS_COLOR);
        defaults.put(SYLLABLE_CONSTITUENT_CODA_COLOR, DEFAULT_SYLLABLE_CONSTITUENT_CODA_COLOR);
        defaults.put(SYLLABLE_CONSTITUENT_RIGHT_APPENDIX_COLOR, DEFAULT_SYLLABLE_CONSTITUENT_RIGHT_APPENDIX_COLOR);
        defaults.put(SYLLABLE_CONSTITUENT_OEHS_COLOR, DEFAULT_SYLLABLE_CONSTITUENT_OEHS_COLOR);
        defaults.put(SYLLABLE_CONSTITUENT_AMBISYLLABIC_COLOR, DEFAULT_SYLLABLE_CONSTITUENT_AMBISYLLABIC_COLOR);
        defaults.put(SYLLABLE_CONSTITUENT_UNKNOWN_COLOR, DEFAULT_SYLLABLE_CONSTITUENT_UNKNOWN_COLOR);
        defaults.put(SYLLABLE_CONSTITUENT_SYLLABLE_BOUNDARY_MARKER_COLOR, DEFAULT_SYLLABLE_CONSTITUENT_SYLLABLE_BOUNDARY_MARKER_COLOR);
        defaults.put(SYLLABLE_CONSTITUENT_SYLLABLE_STRESS_MARKER_COLOR, DEFAULT_SYLLABLE_CONSTITUENT_SYLLABLE_STRESS_MARKER_COLOR);
        defaults.put(SYLLABLE_CONSTITUENT_WORD_BOUNDARY_MARKER_COLOR, DEFAULT_SYLLABLE_CONSTITUENT_WORD_BOUNDARY_MARKER_COLOR);
        defaults.put(SYLLABLE_CONSTITUENT_TONENUMBER_COLOR, DEFAULT_SYLLABLE_CONSTITUENT_TONENUMBER_COLOR);
    }

    public static Color getColorForConstituentType(SyllableConstituentType type) {
        return switch (type) {
            case LEFTAPPENDIX -> UIManager.getColor(SYLLABLE_CONSTITUENT_LEFT_APPENDIX_COLOR);
            case ONSET -> UIManager.getColor(SYLLABLE_CONSTITUENT_ONSET_COLOR);
            case NUCLEUS -> UIManager.getColor(SYLLABLE_CONSTITUENT_NUCLEUS_COLOR);
            case CODA -> UIManager.getColor(SYLLABLE_CONSTITUENT_CODA_COLOR);
            case RIGHTAPPENDIX -> UIManager.getColor(SYLLABLE_CONSTITUENT_RIGHT_APPENDIX_COLOR);
            case OEHS -> UIManager.getColor(SYLLABLE_CONSTITUENT_OEHS_COLOR);
            case AMBISYLLABIC -> UIManager.getColor(SYLLABLE_CONSTITUENT_AMBISYLLABIC_COLOR);
            case UNKNOWN -> UIManager.getColor(SYLLABLE_CONSTITUENT_UNKNOWN_COLOR);
            case SYLLABLEBOUNDARYMARKER -> UIManager.getColor(SYLLABLE_CONSTITUENT_SYLLABLE_BOUNDARY_MARKER_COLOR);
            case SYLLABLESTRESSMARKER -> UIManager.getColor(SYLLABLE_CONSTITUENT_SYLLABLE_STRESS_MARKER_COLOR);
            case WORDBOUNDARYMARKER -> UIManager.getColor(SYLLABLE_CONSTITUENT_WORD_BOUNDARY_MARKER_COLOR);
            case TONENUMBER -> UIManager.getColor(SYLLABLE_CONSTITUENT_TONENUMBER_COLOR);
        };
    }

    @Override
    public Class<?> getExtensionType() {
        return UIDefaultsHandler.class;
    }

    @Override
    public IPluginExtensionFactory<UIDefaultsHandler> getFactory() {
        return args -> this;
    }

}
