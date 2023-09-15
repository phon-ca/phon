package ca.phon.orthography;

/**
 * Inlined dependent tier: scoped annotation that applies to a group.
 */
@CHATReference("https://talkbank.org/manuals/CHAT.html#Group_Scopes")
public final class GroupAnnotation extends AbstractOrthographyElement implements OrthographyAnnotation {

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
