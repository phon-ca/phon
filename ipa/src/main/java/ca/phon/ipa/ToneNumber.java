package ca.phon.ipa;

import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.ipa.features.FeatureSet;

/**
 * Represents a single tone number in an IPA transcription.
 * This is a number from 0 to 9 in superscript form, indicating
 * the tone of a syllable.
 */
public class ToneNumber extends IPAElement {

    private final char toneNumber;

    /**
     * Get tone number character from integer value.
     * Valid values are 0 to 9.
     *
     * @param number the tone number
     * @return the corresponding superscript character
     */
    public static char fromNumber(int number) {
        if(number == 0) return '\u2070';
        else if(number == 1) return '\u00B9';
        else if(number == 2) return '\u00B2';
        else if(number == 3) return '\u00B3';
        else if(number >= 4 && number <= 9) return (char)('\u2070' + number);
        else throw new IllegalArgumentException("Tone number must be between 0 and 9");
    }

    public ToneNumber(int number) {
        this(fromNumber(number));
    }

    public ToneNumber(char toneNumber) {
        super(null, null);
        this.toneNumber = toneNumber;
    }

    public ToneNumber(char toneNumber, FeatureSet overrideFeatureSet, SyllableInfo syllableInfo) {
        super(overrideFeatureSet, syllableInfo != null ? syllableInfo : new SyllableInfo(SyllableConstituentType.TONENUMBER));
        this.toneNumber = toneNumber;
    }


    @Override
    protected FeatureSet _getFeatureSet() {
        return FeatureMatrix.getInstance().getFeatureSet(toneNumber);
    }

    @Override
    public String getText() {
        return String.valueOf(toneNumber);
    }

}
