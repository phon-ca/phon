package ca.phon.app.session.timeline;

import ca.phon.media.TimeComponent;
import ca.phon.media.TimeComponentUI;
import ca.phon.media.TimeUIModel;
import ca.phon.session.TimelineTier;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;

/**
 * Time component for session {@link ca.phon.session.TimelineTier}s
 *
 *
 */
public class TimelineTierComponent extends TimeComponent {

    /**
     * The timeline tier this component is associated with
     */
    private final TimelineTier timelineTier;

    public TimelineTierComponent(TimeUIModel model, TimelineTier timelineTier) {
        super(model);
        this.timelineTier = timelineTier;

        setUI(new TimelineTierComponentUI());
    }

    public TimelineTier getTimelineTier() {
        return timelineTier;
    }

    @Override
    public TimeComponentUI getUI() {
        return super.getUI();
    }

    @Override
    public void setUI(ComponentUI ui) {
        if(ui instanceof TimelineTierComponentUI) {
            super.setUI(ui);
        } else {
            throw new IllegalArgumentException("ui must be a TimelineTierComponentUI");
        }
    }

}
