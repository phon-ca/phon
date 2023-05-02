package ca.phon.orthography;

public class Nonvocal extends AbstractOrthographyElement  {

    public final static String NONVOCAL_START = "&{n=";

    public final static String NONVOCAL_END = "&}n=";

    private final BeginEndSimple beginEndSimple;

    private final String label;

    public Nonvocal(BeginEndSimple beginEndSimple, String label) {
        super();
        this.beginEndSimple = beginEndSimple;
        this.label = label;
    }

    public BeginEndSimple getBeginEndSimple() {
        return beginEndSimple;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String text() {
        final String start = getBeginEndSimple() == BeginEndSimple.END ? NONVOCAL_END : NONVOCAL_START;
        final String end = getBeginEndSimple() == BeginEndSimple.SIMPLE ? "}" : "";
        return String.format("%s%s%s", start, getLabel(), end);
    }

}
