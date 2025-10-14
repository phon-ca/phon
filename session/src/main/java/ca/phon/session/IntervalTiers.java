package ca.phon.session;

import ca.phon.extensions.ExtendableObject;
import ca.phon.session.spi.IntervalTiersSPI;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class which holds information about session interval tiers including:
 * <ul>
 *     <li>length & media unit</li>
 *     <li>list of record tiers displayed in the timeline</li>
 *     <li>list of session-level interval tiers</li>
 * </ul>
 */
public final class IntervalTiers extends ExtendableObject {

    private final IntervalTiersSPI spi;

    IntervalTiers(IntervalTiersSPI spi) {
        super();
        this.spi = spi;
    }

    /**
     * Return the length of the timeline as a float, unit
     * is provided by getMediaUnit()
     *
     * @return length of timeline, this may be different from the actual media length
     */
    public float getLength() {
        return spi.getLength();
    }

    /**
     * Set the length of the timeline
     *
     * @param length
     *            length of timeline, this may be different from the actual media length
     *
     */
    public void setLength(float length) {
        spi.setLength(length);
    }

    /**
     * Get media unit for value provided by getLength()
     *
     * @return media unit
     */
    public MediaUnit getMediaUnit() {
        return spi.getMediaUnit();
    }

    /**
     * Set media unit for value provided by getLength()
     *
     * @param mediaUnit
     */
    public void setMediaUnit(MediaUnit mediaUnit) {
        spi.setMediaUnit(mediaUnit);
    }

    /**
     * Return the list of record data tiers displayed in the timeline view of
     * the session editor.
     *
     * @return list of record data tier names as an unmodifiable list
     */
    public List<String> getRecordIntervalTiers() {
        return Collections.unmodifiableList(spi.getRecordIntervalTiers());
    }

    /**
     * Add record interval tier
     *
     * @param tierName
     * @return true if added, false if already exists
     */
    public boolean addRecordIntervalTier(String tierName) {
        return spi.addRecordIntervalTier(tierName);
    }

    /**
     * Remove record interval tier
     *
     * @param tierName
     * @return true if removed, false if not found
     */
    public boolean removeRecordIntervalTier(String tierName) {
        return spi.removeRecordIntervalTier(tierName);
    }

    /**
     * Get session level interval tiers as an unmodifiable list
     *
     * @return list of interval tiers
     */
    public List<IntervalTier> getTiers() {
        return Collections.unmodifiableList(spi.getTiers());
    }

    /**
     * Get list of interval tier names
     *
     * @return list of session level interval tier names
     */
    public List<String> getTierNames() {
        return getTiers().stream().map(IntervalTier::getName).collect(Collectors.toList());
    }

    /**
     * Get interval tier with given name
     *
     * @param tierName
     * @return interval tier if exists, null otherwise
     */
    public IntervalTier getTier(String tierName) {
        return getTiers().stream()
                .filter(t -> t.getName().equals(tierName)).findAny().orElse(null);
    }

    /**
     * Remove session level interval tier
     *
     * @param tierName
     * @return the removed interval tier or null if not found or not removed
     */
    public IntervalTier removeTier(String tierName) {
        IntervalTier tier = getTier(tierName);
        if(tier != null) {
            if(removeTier(tier))
                return tier;
            else
                return null;
        }
        return null;
    }

    /**
     * Remove session level interval tier
     *
     * @param tier
     * @return true if removed, false if not found in tier list
     */
    public boolean removeTier(IntervalTier tier) {
        return spi.removeTier(tier);
    }

    /**
     * Add session level interval tier
     *
     * @param tierName
     * @return new IntervalTierImpl if the given name does not exist, existing IntervalTierImpl if it does
     */
    public IntervalTier addTier(String tierName) {
        final SessionFactory factory = SessionFactory.newFactory();
        final IntervalTier tier = factory.createTimelineTier(tierName);
        if(addTier(tier))
            return tier;
        else
            return getTier(tierName);
    }

    /**
     * Add session level interval tier
     *
     * @param tier
     * @return true if added, false if tier with given name already exists
     */
    public boolean addTier(IntervalTier tier) {
        return spi.addTier(tier);
    }

}
