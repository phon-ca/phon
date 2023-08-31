package ca.phon.session.impl;

import ca.phon.session.Gem;
import ca.phon.session.GemType;
import ca.phon.session.spi.GemSPI;

public class GemImpl implements GemSPI {

    private GemType gemType;

    private String label;

    public GemImpl() {
        this("");
    }

    public GemImpl(String label) {
        this(GemType.Lazy, label);
    }

    public GemImpl(GemType type, String label) {
        this.gemType = type;
        this.label = label;
    }

    @Override
    public GemType getType() {
        return this.gemType;
    }

    @Override
    public void setType(GemType gemType) {
        this.gemType = gemType;
    }

    @Override
    public String getLabel() {
        return  this.label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }
}
