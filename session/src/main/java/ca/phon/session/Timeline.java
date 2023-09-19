package ca.phon.session;

import ca.phon.extensions.ExtendableObject;
import ca.phon.session.spi.TimelineSPI;

import java.awt.*;
import java.util.Collections;
import java.util.List;

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
     * Get timeline tiers as an unmodifiable list
     *
     * @return list of timeline tiers
     */
    public List<TimelineTier> getTimelineTiers() {
        return Collections.unmodifiableList(spi.getTimelineTiers());
    }

}
