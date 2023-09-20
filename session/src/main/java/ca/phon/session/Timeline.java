package ca.phon.session;

import ca.phon.extensions.ExtendableObject;
import ca.phon.session.spi.TimelineSPI;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class which holds information about session timeline including:
 * <ul>
 *     <li>length & media unit</li>
 *     <li>list of record tiers displayed in the timeline</li>
 *     <li>list of session-level timeline tiers</li>
 * </ul>
 */
public final class Timeline extends ExtendableObject {

    private final TimelineSPI spi;

    Timeline(TimelineSPI spi) {
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
     * Get media unit for value provided by getLength()
     *
     * @return media unit
     */
    public MediaUnit getMediaUnit() {
        return spi.getMediaUnit();
    }

    /**
     * Return the list of record data tiers displayed in the timeline view of
     * the session editor.
     *
     * @return list of record data tier names as an unmodifiable list
     */
    public List<String> getRecordTimelineTiers() {
        return Collections.unmodifiableList(spi.getRecordTimelineTiers());
    }

    /**
     * Get session level timeline tiers as an unmodifiable list
     *
     * @return list of timeline tiers
     */
    public List<TimelineTier> getTiers() {
        return Collections.unmodifiableList(spi.getTiers());
    }

    /**
     * Get list of timeline tier names
     *
     * @return list of session level timeline tier names
     */
    public List<String> getTierNames() {
        return getTiers().stream().map(TimelineTier::getName).collect(Collectors.toList());
    }

    /**
     * Get timeline tier with given name
     *
     * @param tierName
     * @return timeline tier if exists, null otherwise
     */
    public TimelineTier getTier(String tierName) {
        return getTiers().stream()
                .filter(t -> t.getName().equals(tierName)).findAny().orElse(null);
    }

    /**
     * Remove session level timeline tier
     *
     * @param tierName
     * @return the removed timeline tier or null if not found or not removed
     */
    public TimelineTier removeTier(String tierName) {
        TimelineTier tier = getTier(tierName);
        if(tier != null) {
            if(removeTier(tier))
                return tier;
            else
                return null;
        }
        return null;
    }

    /**
     * Remove session level timeline tier
     *
     * @param tier
     * @return true if removed, false if not found in tier list
     */
    public boolean removeTier(TimelineTier tier) {
        return spi.removeTier(tier);
    }

    /**
     * Add session level timeline tier
     *
     * @param tierName
     * @return new TimelineTier if given name does not exist, existing TimelineTier if it does
     */
    public TimelineTier addTier(String tierName) {
        return null;
    }

    /**
     * Add session level timeline tier
     *
     * @param tier
     * @return true if added, false if tier with given name already exists
     */
    public boolean addTier(TimelineTier tier) {
        return spi.addTier(tier);
    }

}
