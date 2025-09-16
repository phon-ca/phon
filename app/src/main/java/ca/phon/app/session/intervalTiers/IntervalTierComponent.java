package ca.phon.app.session.intervalTiers;

import ca.phon.media.TimeComponent;
import ca.phon.media.TimeComponentUI;
import ca.phon.media.TimeUIModel;
import ca.phon.session.IntervalTier;

import javax.swing.*;
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

    private final ListSelectionModel selectionModel;

    public IntervalTierComponent(TimeUIModel model, IntervalTier intervalTier) {
        super(model);
        this.intervalTier = intervalTier;
        this.selectionModel = new DefaultListSelectionModel();

        setUI(new IntervalTierComponentUI());
    }

    public ListSelectionModel getSelectionModel() {
        return this.selectionModel;
    }

    public int getSelectedIndex() {
        return this.selectionModel.getMinSelectionIndex();
    }

    public void setSelectedIndex(int index) {
        this.selectionModel.setSelectionInterval(index, index);
        repaint();
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
