package ca.phon.session;

import ca.phon.GemType;
import ca.phon.extensions.ExtendableObject;

public final class Gem extends ExtendableObject {

    private final GemType type;

    private final String label;

    Gem(GemType type, String label) {
        super();
        this.type = type;
        this.label = label;
    }

    public GemType getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Gem)) return false;
        Gem b = (Gem) obj;
        return type == b.type && label.equals(b.label);
    }

}
