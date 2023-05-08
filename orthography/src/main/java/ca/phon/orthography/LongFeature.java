package ca.phon.orthography;

public final class LongFeature extends AbstractOrthographyElement implements WordElement {

    public final static String LONG_FEATURE_START = "&{l=";

    public final static String LONG_FEATURE_END = "&}l=";

    private final BeginEnd beginEnd;

    private final String label;

    public LongFeature(BeginEnd beginEnd, String label) {
        super();
        this.beginEnd = beginEnd;
        this.label = label;
    }

    public BeginEnd getBeginEnd() {
        return beginEnd;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String text() {
        final String prefix = (getBeginEnd() == BeginEnd.BEGIN ? LONG_FEATURE_START : LONG_FEATURE_END);
        return String.format("%s%s", prefix, getLabel());
    }

}
