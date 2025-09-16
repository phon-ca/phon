package ca.phon.app.session.editor.view.speechAnalysis;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.intervalTiers.IntervalTierComponent;
import ca.phon.app.session.intervalTiers.IntervalTierComponentUI;
import ca.phon.app.session.intervalTiers.RecordIntervalTier;
import ca.phon.media.TimeUIModel;
import ca.phon.media.TimeUIModelAdapter;
import ca.phon.session.*;
import ca.phon.session.Record;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Tier for displaying intervals from the session {@link IntervalTiers}
 */
public class SpeechAnalysisIntervalsTier extends SpeechAnalysisTier {

    private TimeUIModel intervalTierTimeModel;

    /**
     * Map of tier name to interval tier component
     */
    private final Map<String, IntervalTierComponent> intervalTiersMap = new HashMap<>();

    public SpeechAnalysisIntervalsTier(SpeechAnalysisEditorView parentView) {
        super(parentView);

        init();
        setupEditorEventHandlers();
    }

    private void setupEditorEventHandlers() {
        getParentView().getEditor().getEventManager().registerActionForEvent(EditorEventType.TierChange, this::onTierChange);
    }

    private void unregisterEditorEventHandlers() {
        getParentView().getEditor().getEventManager().removeActionForEvent(EditorEventType.TierChange, this::onTierChange);
    }

    public void onTierChange(EditorEvent<EditorEventType.TierChangeData> ee) {
        final EditorEventType.TierChangeData data = ee.data();
        final Tier<?> tier = data.tier();

        if(intervalTiersMap.containsKey(tier.getName())) {
            final IntervalTierComponent intervalTierComponent = intervalTiersMap.get(tier.getName());
            intervalTierComponent.repaint();
        }
    }

    private void init() {
        setLayout(new VerticalLayout());

        // setup time model
        final TimeUIModel parentModel = getParentView().getTimeModel();
        this.intervalTierTimeModel = new TimeUIModel();
        this.intervalTierTimeModel.setCurrentTime(parentModel.getCurrentTime());
        this.intervalTierTimeModel.setEndTime(parentModel.getEndTime());
        this.intervalTierTimeModel.setTimeInsets(parentModel.getTimeInsets());
        this.intervalTierTimeModel.setMediaEndTime(parentModel.getMediaEndTime());
        this.intervalTierTimeModel.setPixelsPerSecond(parentModel.getPixelsPerSecond());
        parentModel.addTimeUIModelListener(new TimeUIModelAdapter() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
            switch(e.getPropertyName()) {
               case "currentTime" -> intervalTierTimeModel.setCurrentTime(parentModel.getCurrentTime());
               case "endTime" -> intervalTierTimeModel.setEndTime(parentModel.getEndTime());
               case "timeInsets" -> intervalTierTimeModel.setTimeInsets(parentModel.getTimeInsets());
               case "mediaEndTime" -> intervalTierTimeModel.setMediaEndTime(parentModel.getMediaEndTime());
               case "pixelsPerSecond" -> intervalTierTimeModel.setPixelsPerSecond(parentModel.getPixelsPerSecond());
               default -> super.propertyChange(e);
            }
            }
        });

        // add a tier for each session intervalTiers tier
        final Session session = getParentView().getEditor().getSession();
        final IntervalTiers intervalTiers = session.getTimeline();

        final WordAndPhoneSelectionListener wordAndPhoneSelectionListener = new WordAndPhoneSelectionListener();

        // check for word intervals tier
        final TierDescription worTierDesc = session.getUserTiers()
                .stream()
                .filter(td -> UserTierType.Wor.getPhonTierName().equals(td.getName()))
                .findAny().orElse(null);
        if(worTierDesc != null) {
            final RecordIntervalTier recordTimelineTier = new RecordIntervalTier(session, worTierDesc.getName());
            final IntervalTier intervalTier = new IntervalTier(recordTimelineTier);
            final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(intervalTierTimeModel, intervalTier);
            intervalTierComponent.getSelectionModel().addListSelectionListener(wordAndPhoneSelectionListener);
            add(intervalTierComponent);
            intervalTiersMap.put(worTierDesc.getName(), intervalTierComponent);
        }

        // check for phone intervals tier
        final TierDescription phoTierDesc = session.getUserTiers()
                .stream()
                .filter(td -> UserTierType.PhoneIntervals.getPhonTierName().equals(td.getName()))
                .findAny().orElse(null);
        if(phoTierDesc != null) {
            final RecordIntervalTier recordTimelineTier = new RecordIntervalTier(session, phoTierDesc.getName());
            final IntervalTier intervalTier = new IntervalTier(recordTimelineTier);
            final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(this.intervalTierTimeModel, intervalTier);
            intervalTierComponent.getSelectionModel().addListSelectionListener(wordAndPhoneSelectionListener);
            add(intervalTierComponent);
            intervalTiersMap.put(phoTierDesc.getName(), intervalTierComponent);
        }

        for(String timelineTierName: intervalTiers.getRecordIntervalTiers()) {
            final RecordIntervalTier recordTimelineTier = new RecordIntervalTier(session, timelineTierName);
            final IntervalTier intervalTier = new IntervalTier(recordTimelineTier);
            final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(this.intervalTierTimeModel, intervalTier);
            add(intervalTierComponent);
            intervalTiersMap.put(timelineTierName, intervalTierComponent);
        }

        for(var timelineTier : intervalTiers.getTiers()) {
            final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(this.intervalTierTimeModel, timelineTier);
            add(intervalTierComponent);
            intervalTiersMap.put(timelineTier.getName(), intervalTierComponent);
        }
    }

    @Override
    public void addMenuItems(JMenu menuEle, boolean includeAccelerators) {

    }

    @Override
    public void onRefresh() {

    }

    /**
     * Word and phone interval tier selection listener
     *
     * 1) When a word interval is selected, if phone intervals from another word interval are selected,
     *   they should be deselected.
     * 2) When a phone interval is selected, if the parent word interval is not selected, it should be selected.
     */
    private class WordAndPhoneSelectionListener implements ListSelectionListener {

        private IntervalTierComponent wordIntervalTierComponent() {
            return intervalTiersMap.get(UserTierType.Wor.getPhonTierName());
        }

        private IntervalTierComponent phoneIntervalTierComponent() {
            return intervalTiersMap.get(UserTierType.PhoneIntervals.getPhonTierName());
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            final IntervalTierComponent wordIntervalTier = wordIntervalTierComponent();
            final IntervalTierComponent phoneIntervalTier = phoneIntervalTierComponent();

            if(e.getValueIsAdjusting()) return;
            if(e.getSource() == wordIntervalTier.getSelectionModel()) {
                if(phoneIntervalTier == null) return;
                // get indices of selected phone intervals for the word
                final int selectedWord = wordIntervalTier.getSelectedIndex();
                if(selectedWord == -1) return;
                final IntervalTier.Interval wordInterval = wordIntervalTier.getTimelineTier().getIntervals().get(selectedWord);
                final var phoneIndiciesForWord =
                    ((IntervalTierComponentUI)phoneIntervalTier.getUI()).getIntervalIndicesForTimeRange(wordInterval.getStart(), wordInterval.getEnd());
                if(phoneIndiciesForWord.size() == 0) {
                    phoneIntervalTier.getSelectionModel().clearSelection();
                    phoneIntervalTier.repaint();
                } else {
                    final int selectedPhone = phoneIntervalTier.getSelectedIndex();
                    if(selectedPhone >= 0) {
                        if (!phoneIndiciesForWord.contains(selectedPhone)) {
                            phoneIntervalTier.getSelectionModel().clearSelection();
                            phoneIntervalTier.repaint();
                        }
                    }
                }

                for(int i = 0; i < getParentView().getEditor().getSession().getRecordCount(); i++) {
                    final Record r = getParentView().getEditor().getSession().getRecord(i);
                    final MediaSegment seg = r.getMediaSegment();
                    // select record if our word interval intersects with the record media segment
                    final MediaSegment wordSeg =  SessionFactory.newFactory().createMediaSegment();
                    wordSeg.setUnitType(MediaUnit.Second);
                    wordSeg.setStartTime(wordInterval.getStart());
                    wordSeg.setEndTime(wordInterval.getEnd());
                    if(seg.overlaps(wordSeg) != MediaSegment.OverlapType.NO_OVERLAP) {
                        if(getParentView().getEditor().getCurrentRecordIndex() != i) {
                            getParentView().getEditor().setCurrentRecordIndex(i);
                        }
                        break;
                    }
                }
            } else if(e.getSource() == phoneIntervalTier.getSelectionModel()) {
                if(wordIntervalTier == null) return;
                // get selected phone interval
                final int selectedPhone = phoneIntervalTier.getSelectedIndex();
                if(selectedPhone == -1) return;
                final IntervalTier.Interval phoneInterval = phoneIntervalTier.getTimelineTier().getIntervals().get(selectedPhone);
                // find the word interval that contains the phone interval
                final var wordIndiciesForPhone =
                    ((IntervalTierComponentUI)wordIntervalTier.getUI()).getIntervalIndicesForTimeRange(phoneInterval.getStart(), phoneInterval.getEnd());
                if(wordIndiciesForPhone.size() != 0) {
                    final int selectedWord = wordIntervalTier.getSelectedIndex();
                    if(selectedWord == -1 || !wordIndiciesForPhone.contains(selectedWord)) {
                        // select the first word interval that contains the phone interval
                        wordIntervalTier.getSelectionModel().setSelectionInterval(wordIndiciesForPhone.get(0), wordIndiciesForPhone.get(0));
                        wordIntervalTier.repaint();
                    }
                }
            }
        }

    };

}
