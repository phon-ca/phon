package ca.phon.app.session.intervalTiers;

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
public class IntervalTierComponent extends TimeComponent {

    /**
     * The timeline tier this component is associated with
     */
    private final IntervalTier intervalTier;

    public IntervalTierComponent(TimeUIModel model, IntervalTier intervalTier) {
        super(model);
        this.intervalTier = intervalTier;

        setUI(new IntervalTierComponentUI());
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
        if(ui instanceof IntervalTierComponentUI) {
            super.setUI(ui);
        } else {
            throw new IllegalArgumentException("ui must be a IntervalTierComponentUI");
        }
    }

}
