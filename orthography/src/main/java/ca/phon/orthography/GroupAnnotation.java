package ca.phon.orthography;

public final class GroupAnnotation extends AbstractOrthographyElement {

    private final GroupAnnotationType type;

    private final String data;

    public GroupAnnotation(GroupAnnotationType type, String data) {
        super();
        this.type = type;
        this.data = data;
    }

    public GroupAnnotationType getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    @Override
    public String text() {
        return String.format("[%s %s]", getType().getPrefix(), getData());
    }

}
