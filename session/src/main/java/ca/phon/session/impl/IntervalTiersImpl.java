package ca.phon.session.impl;

import ca.phon.session.IntervalTier;
import ca.phon.session.MediaUnit;
import ca.phon.session.spi.IntervalTiersSPI;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of a timeline.
 */
public class IntervalTiersImpl implements IntervalTiersSPI {

    /**
     * Length of timeline in specified media units
     */
    private float length;

    /**
     * Media unit associated with this timeline
     */
    private MediaUnit mediaUnit;

    /**
     * List of record timeline tiers
     */
    private List<String> recordTimelineTiers;

    /**
     * List of session-level timeline tiers
     */
    private List<IntervalTier> intervalTiers;

    public IntervalTiersImpl() {
        super();
        this.mediaUnit = MediaUnit.Second;
        this.recordTimelineTiers = new ArrayList<>();
        this.intervalTiers = new ArrayList<>();
    }

    public IntervalTiersImpl(float length, MediaUnit mediaUnit, List<String> recordTimelineTiers, List<IntervalTier> intervalTiers) {
        this.length = length;
        this.mediaUnit = mediaUnit;
        this.recordTimelineTiers = recordTimelineTiers;
        this.intervalTiers = intervalTiers;
    }

    @Override
    public float getLength() {
        return this.length;
    }

    @Override
    public void setLength(float length) {
        this.length = length;
    }

    @Override
    public MediaUnit getMediaUnit() {
        return this.mediaUnit;
    }

    @Override
    public void setMediaUnit(MediaUnit mediaUnit) {
        this.mediaUnit = mediaUnit;
    }

    @Override
    public List<String> getRecordTimelineTiers() {
        return List.copyOf(this.recordTimelineTiers);
    }

    @Override
    public boolean addRecordTimelineTier(String tierName) {
        return this.recordTimelineTiers.add(tierName);
    }

    @Override
    public boolean removeRecordTimelineTier(String tierName) {
        return this.recordTimelineTiers.remove(tierName);
    }

    @Override
    public List<IntervalTier> getTiers() {
        return List.copyOf(this.intervalTiers);
    }

    @Override
    public boolean removeTier(IntervalTier tier) {
        return this.intervalTiers.remove(tier);
    }

    @Override
    public boolean addTier(IntervalTier tier) {
        if(!this.intervalTiers.contains(tier)) {
            this.intervalTiers.add(tier);
            return true;
        } else {
            return false;
        }
    }
}
