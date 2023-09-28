package ca.phon.session.tierdata;

/**
 * Link to external content inside a tier with optional label
 */
public final class TierLink implements TierElement {

    public final static String LINK_PREFIX = "\uD83D\uDD17";

    public final static String LINK_SUFFIX = LINK_PREFIX;

    private final String href;

    private final String label;

    public TierLink(String href) {
        this(href, null);
    }

    public TierLink(String href, String label) {
        this.href = href;
        this.label = label;
    }

    public String getHref() {
        return href;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String text() {
        final StringBuilder builder = new StringBuilder();
        builder.append(LINK_PREFIX);
        if(getLabel() != null && !getLabel().isBlank())
            builder.append(getLabel()).append(" ");
        builder.append(href);
        builder.append(LINK_SUFFIX);
        return builder.toString();
    }

    @Override
    public String toString() {
        return text();
    }

}
