package ca.phon.orthography;

public final class Quotation extends AbstractOrthographyElement {

    public final static String QUOTATION_BEGIN = "\u201c";

    public final static String QUOTATION_END = "\u201d";

    private final BeginEnd beginEnd;

    public Quotation(BeginEnd beginEnd) {
        super();
        this.beginEnd = beginEnd;
    }

    public BeginEnd getBeginEnd() {
        return beginEnd;
    }

    @Override
    public String text() {
        return getBeginEnd() == BeginEnd.BEGIN ? QUOTATION_BEGIN : QUOTATION_END;
    }

}
