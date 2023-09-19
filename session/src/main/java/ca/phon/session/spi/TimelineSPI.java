package ca.phon.session.spi;

import ca.phon.session.MediaUnit;
import ca.phon.session.TimelineTier;

import java.util.List;

public interface TimelineSPI {

    public float getLength();

    public MediaUnit getMediaUnit();

    public List<String> getRecordTimelineTiers();

    public List<TimelineTier> getTimelineTiers();

}
