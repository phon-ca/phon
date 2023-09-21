package ca.phon.session.spi;

import ca.phon.session.MediaUnit;
import ca.phon.session.TimelineTier;

import java.util.List;

public interface TimelineSPI {

    public float getLength();

    public MediaUnit getMediaUnit();

    public List<String> getRecordTimelineTiers();

    public List<TimelineTier> getTiers();

    /**
     * Remove session level timeline tier
     *
     * @param tier
     * @return true if removed, false if not found in tier list
     */
    public boolean removeTier(TimelineTier tier);

    /**
     * Add timeline tier
     *
     * @param tier
     * @return true if added, false if not
     */
    public boolean addTier(TimelineTier tier);

}
