package ca.phon.session.impl;

import ca.phon.session.MediaUnit;
import ca.phon.session.TimelineTier;
import ca.phon.session.spi.TimelineSPI;

import java.util.List;

/**
 * Default implementation of a timeline.
 */
public final class TimelineImpl implements TimelineSPI {

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
    private List<TimelineTier> timelineTiers;

    public TimelineImpl() {
        super();
        this.mediaUnit = MediaUnit.Second;
        this.recordTimelineTiers = List.of();
        this.timelineTiers = List.of();
    }

    public TimelineImpl(float length, MediaUnit mediaUnit, List<String> recordTimelineTiers, List<TimelineTier> timelineTiers) {
        this.length = length;
        this.mediaUnit = mediaUnit;
        this.recordTimelineTiers = recordTimelineTiers;
        this.timelineTiers = timelineTiers;
    }

    @Override
    public float getLength() {
        return 0;
    }

    @Override
    public MediaUnit getMediaUnit() {
        return this.mediaUnit;
    }

    @Override
    public List<String> getRecordTimelineTiers() {
        return List.copyOf(this.recordTimelineTiers);
    }

    @Override
    public List<TimelineTier> getTiers() {
        return List.copyOf(this.timelineTiers);
    }

    @Override
    public boolean removeTier(TimelineTier tier) {
        return this.timelineTiers.remove(tier);
    }

    @Override
    public boolean addTier(TimelineTier tier) {
        if(!this.timelineTiers.contains(tier)) {
            this.timelineTiers.add(tier);
            return true;
        } else {
            return false;
        }
    }
}
