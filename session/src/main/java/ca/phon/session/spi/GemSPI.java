package ca.phon.session.spi;

import ca.phon.session.GemType;

public interface GemSPI {

    public GemType getType();

    public void setType(GemType gemType);

    public String getLabel();

    public void setLabel(String label);

}
