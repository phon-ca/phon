package ca.phon.session.spi;

import ca.phon.session.IntervalTier;
import ca.phon.session.MediaUnit;

import java.util.List;

public interface IntervalTiersSPI {

    public float getLength();

    public void setLength(float length);

    public MediaUnit getMediaUnit();

    public void setMediaUnit(MediaUnit mediaUnit);

    public List<String> getRecordIntervalTiers();

    public boolean addRecordIntervalTier(String tierName);

    public boolean removeRecordIntervalTier(String tierName);

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
