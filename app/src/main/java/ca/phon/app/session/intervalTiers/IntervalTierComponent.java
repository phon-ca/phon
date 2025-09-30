package ca.phon.app.session.intervalTiers;

import ca.phon.media.TimeComponent;
import ca.phon.media.TimeComponentUI;
import ca.phon.media.TimeUIModel;
import ca.phon.session.IntervalTier;
import ca.phon.ui.text.FileSelectionField;

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

    /**
     * List selection model for interval selection
     */
    private final ListSelectionModel selectionModel;

    /**
     * List selection model for interval highlighting - (e.g., intervals which intersect the current Speech Analysis selection)
     */
    private final ListSelectionModel highlightModel;

    /**
     * User specified callback for interval clicks, unlike in selection model
     * this callback will only be called when an interval is clicked, not when
     * the selection is changed programmatically
     */
    private BiConsumer<Integer, IntervalTier.Interval> intervalClickedCallback = null;

    public IntervalTierComponent(TimeUIModel model, IntervalTier intervalTier) {
        super(model);
        this.intervalTier = intervalTier;
        this.selectionModel = new DefaultListSelectionModel() {
            @Override
            public void setSelectionMode(int selectionMode) {
                if(selectionMode == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {
                    throw new IllegalArgumentException("selectionMode must be SINGLE_INTERVAL_SELECTION or SINGLE_SELECTION");
                }
                super.setSelectionMode(selectionMode);
            }
        };
        this.highlightModel = new DefaultListSelectionModel();
        this.highlightModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        setUI(new IntervalTierComponentUI());
    }

    public void setIntervalClickedCallback(BiConsumer<Integer, IntervalTier.Interval> callback) {
        this.intervalClickedCallback = callback;
    }

    public BiConsumer<Integer, IntervalTier.Interval> getIntervalClickedCallback() {
        return this.intervalClickedCallback;
    }

    /**
     * Get the selection model used to track selected intervals
     *
     * @return the selection model
     */
    public ListSelectionModel getSelectionModel() {
        return this.selectionModel;
    }

    /**
     * Get the highlight model used to track highlighted intervals
     *
     * @return the highlight model
     */
    public int getSelectedIndex() {
        return this.selectionModel.getMinSelectionIndex();
    }

    /**
     * Set the selected interval index
     *
     * @param index the index to select
     */
    public void setSelectedIndex(int index) {
        this.selectionModel.setSelectionInterval(index, index);
        repaint();
    }

    /**
     * Add an interval index to the list of highlighted intervals
     *
     * @param index the index to highlight
     */
    public void addHighlightedIndex(int index) {
        this.highlightModel.addSelectionInterval(index, index);
        repaint();
    }

    /**
     * Clear all highlighted intervals
     */
    public void clearHighlightedIndices() {
        this.highlightModel.clearSelection();
        repaint();
    }

    /**
     * Add a range of interval indices to the list of highlighted intervals
     * @param fromIndex the start index (inclusive)
     * @param toIndex the end index (inclusive)v
     */
    public void addHighlightedIndices(int fromIndex, int toIndex) {
        this.highlightModel.addSelectionInterval(fromIndex, toIndex);
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
