package ca.phon.session;

import ca.phon.extensions.ExtendableObject;
import ca.phon.session.spi.GemSPI;

public final class Gem extends ExtendableObject {

    private final GemSPI spi;

    Gem(GemSPI spi) {
        this.spi = spi;
    }

    public GemType getType() {
        return spi.getType();
    }

    public void setType(GemType gemType) {
        spi.setType(gemType);
    }

    public String getLabel() {
        return spi.getLabel();
    }

    public void setLabel(String label) {
        spi.setLabel(label);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Gem)) return false;
        Gem b = (Gem) obj;
        return getType() == b.getType() && getLabel().equals(b.getLabel());
    }

}
