package ca.phon.ipa;

import ca.phon.ipa.features.FeatureSet;

/**
 * A geminate consists of two identical phones.
 */
public class Geminate extends Phone {

    /**
     * First phone
     */
    private final Phone firstPhone;

    /**
     * Second phone
     */
    private final Phone secondPhone;

    public Geminate() {
        this(new Phone(Character.valueOf('\u25cc')),
                new Phone(Character.valueOf('\u25cc')),
                null,
                new SyllableInfo()
        );
    }

    /**
     * Constructor
     *
     * @param firstPhone
     * @param secondPhone
     * @param overrideFeatureSet
     * @param syllableInfo
     */
    Geminate(Phone firstPhone, Phone secondPhone, FeatureSet overrideFeatureSet, SyllableInfo syllableInfo) {
        super('\u0000', overrideFeatureSet, syllableInfo != null ? syllableInfo : new SyllableInfo());
        this.firstPhone = firstPhone;
        this.secondPhone = secondPhone;
    }

    Geminate(Phone firstPhone, Phone secondPhone,
                  Diacritic[] prefix, Diacritic[] combining, Diacritic[] suffix,
                  FeatureSet overrideFeatureSet, SyllableInfo syllableInfo) {
        super(prefix, '\u0000', combining, suffix, overrideFeatureSet, syllableInfo != null ? syllableInfo : new SyllableInfo());
        this.firstPhone = firstPhone;
        this.secondPhone = secondPhone;
    }

    /**
     * Constructor
     *
     * @param firstPhone
     * @param secondPhone
     * @param ligature
     */
    Geminate(Phone firstPhone, Phone secondPhone) {
        this(firstPhone, secondPhone, null, new SyllableInfo());
    }

    /**
     * Get the first phone in this geminate
     *
     * @return the first phone
     */
    public Phone getFirstPhone() {
        return this.firstPhone;
    }

    /**
     * Get second phone in this geminate
     *
     * @return the second phone
     */
    public Phone getSecondPhone() {
        return this.secondPhone;
    }

    @Override
    protected FeatureSet _getFeatureSet() {
        FeatureSet retVal = FeatureSet.union(firstPhone.featureSet(), secondPhone.featureSet());
        retVal = FeatureSet.union(retVal, getPrefixFeatures());
        retVal = FeatureSet.union(retVal, getCombiningFeatures());
        retVal = FeatureSet.union(retVal, getSuffixFeatures());

        retVal = FeatureSet.union(retVal, FeatureSet.singletonFeature("geminate"));

        return retVal;
    }

    @Override
    public String getText() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getPrefix());
        sb.append(getFirstPhone().getText());
        sb.append(getSecondPhone().getText());
        sb.append(getSuffix());
        return sb.toString();
    }

}
