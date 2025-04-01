package ca.phon.session.spi;

import ca.phon.session.MediaUnit;
import ca.phon.session.TimelineTier;

import java.util.List;

public interface TimelineSPI {

    public float getLength();

    public void setLength(float length);

    public MediaUnit getMediaUnit();

    public void setMediaUnit(MediaUnit mediaUnit);

    public List<String> getRecordTimelineTiers();

    public boolean addRecordTimelineTier(String tierName);

    public boolean removeRecordTimelineTier(String tierName);

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
