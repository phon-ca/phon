package ca.phon.app.session.intervalTiers;

import ca.phon.media.TimeComponent;
import ca.phon.media.TimeComponentUI;
import ca.phon.media.TimeUIModel;
import ca.phon.session.IntervalTier;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

    /**
     * User specified callback for interval clicks, unlike in selection model
     * this callback will only be called when an interval is clicked, not when
     * the selection is changed programmatically
     */
    private BiConsumer<Integer, IntervalTier.Interval> intervalClickedCallback = null;

    public IntervalTierComponent(TimeUIModel model, IntervalTier intervalTier) {
        super(model);
        this.intervalTier = intervalTier;
        this.selectionModel = new DefaultListSelectionModel();

        setUI(new IntervalTierComponentUI());
    }

    public void setIntervalClickedCallback(BiConsumer<Integer, IntervalTier.Interval> callback) {
        this.intervalClickedCallback = callback;
    }

    public BiConsumer<Integer, IntervalTier.Interval> getIntervalClickedCallback() {
        return this.intervalClickedCallback;
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
