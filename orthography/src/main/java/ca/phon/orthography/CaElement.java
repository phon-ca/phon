package ca.phon.orthography;

/**
 * CA subwords that must occur inside a word.
 * <p>
 *     <a href="https://talkbank.org/manuals/CHAT.html#CA_Subwords">CHAT manual section on
 *                     this topic...</a>
 * </p>
 */
@CHATReference("https://talkbank.org/manuals/CHAT.html#CA_Subwords")
public final class CaElement extends AbstractWordElement {

    private final CaElementType type;

    public CaElement(CaElementType type) {
        super();
        this.type = type;
    }

    public CaElementType getType() {
        return this.type;
    }

    @Override
    public String text() {
        return type.toString();
    }

}
