package ca.phon.session.spi;

import ca.phon.session.MediaUnit;
import ca.phon.session.IntervalTier;

import java.util.List;

public interface IntervalTiersSPI {

    public float getLength();

    public void setLength(float length);

    public MediaUnit getMediaUnit();

    public void setMediaUnit(MediaUnit mediaUnit);

    public List<String> getRecordTimelineTiers();

    public boolean addRecordTimelineTier(String tierName);

    public boolean removeRecordTimelineTier(String tierName);

    public List<IntervalTier> getTiers();

    /**
     * Remove session level interval tier
     *
     * @param tier
     * @return true if removed, false if not found in tier list
     */
    public boolean removeTier(IntervalTier tier);

    /**
     * Add timeline tier
     *
     * @param tier
     * @return true if added, false if not
     */
    public boolean addTier(IntervalTier tier);

}
