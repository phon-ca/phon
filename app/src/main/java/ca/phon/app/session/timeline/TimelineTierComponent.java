package ca.phon.app.session.timeline;

import ca.phon.media.TimeComponent;
import ca.phon.media.TimeComponentUI;
import ca.phon.media.TimeUIModel;
import ca.phon.session.IntervalTier;

import javax.swing.plaf.ComponentUI;

/**
 * Time component for session {@link IntervalTier}s
 *
 *
 */
public class TimelineTierComponent extends TimeComponent {

    /**
     * The timeline tier this component is associated with
     */
    private final IntervalTier intervalTier;

    public TimelineTierComponent(TimeUIModel model, IntervalTier intervalTier) {
        super(model);
        this.intervalTier = intervalTier;

        setUI(new TimelineTierComponentUI());
    }

    public IntervalTier getTimelineTier() {
        return intervalTier;
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
