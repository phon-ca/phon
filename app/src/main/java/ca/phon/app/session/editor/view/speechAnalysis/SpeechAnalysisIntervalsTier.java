package ca.phon.app.session.editor.view.speechAnalysis;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.intervalTiers.IntervalTierComponent;
import ca.phon.app.session.intervalTiers.IntervalTierComponentUI;
import ca.phon.app.session.intervalTiers.RecordIntervalTier;
import ca.phon.media.TimeUIModel;
import ca.phon.media.TimeUIModelAdapter;
import ca.phon.orthography.*;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.util.Range;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Tier for displaying intervals from the session {@link IntervalTiers}
 */
public class SpeechAnalysisIntervalsTier extends SpeechAnalysisTier {

    private TimeUIModel intervalTierTimeModel;

    /**
     * Currently focused selected interval, changes to this interval will update the selected interval
     * as well as any intervals which share a relationship with this interval (e.g. phones within a word).
     * Intervals across other tiers will also be affected if they share an start/end time with this interval.
     */
    private TimeUIModel.Interval currentInterval;

    private IntervalTierComponent currentIntervalTierComponent;

    private int currentIntervalIndex = -1;

    /**
     * Map of tier name to interval tier components for record data tiers
     */
    private final Map<String, IntervalTierComponent> recordDataIntervalTiers = new HashMap<>();

    /**
     * Map of tier name to interval tier components for session intervalTiers tiers
     */
    private final Map<String, IntervalTierComponent> sessionLevelIntervalTiers = new HashMap<>();

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

        if(recordDataIntervalTiers.containsKey(tier.getName())) {
            final IntervalTierComponent intervalTierComponent = recordDataIntervalTiers.get(tier.getName());
            final RecordIntervalTier recordIntervalTier = intervalTierComponent.getTimelineTier().getExtension(RecordIntervalTier.class);
            if(recordIntervalTier != null) {
                recordIntervalTier.updateCachedIntervals(ee.data().record());
            }
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
            intervalTier.putExtension(RecordIntervalTier.class, recordTimelineTier);
            final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(intervalTierTimeModel, intervalTier);
            intervalTierComponent.getSelectionModel().addListSelectionListener(wordAndPhoneSelectionListener);
            intervalTierComponent.setIntervalClickedCallback( (index,interval) ->
                    setupSelectionInterval(intervalTierComponent, index, interval)
            );
            add(intervalTierComponent);
            recordDataIntervalTiers.put(worTierDesc.getName(), intervalTierComponent);
        }

        // check for phone intervals tier
        final TierDescription phoTierDesc = session.getUserTiers()
                .stream()
                .filter(td -> UserTierType.PhoneIntervals.getPhonTierName().equals(td.getName()))
                .findAny().orElse(null);
        if(phoTierDesc != null) {
            final RecordIntervalTier recordTimelineTier = new RecordIntervalTier(session, phoTierDesc.getName());
            final IntervalTier intervalTier = new IntervalTier(recordTimelineTier);
            intervalTier.putExtension(RecordIntervalTier.class, recordTimelineTier);
            final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(this.intervalTierTimeModel, intervalTier);
            intervalTierComponent.getSelectionModel().addListSelectionListener(wordAndPhoneSelectionListener);
            intervalTierComponent.setIntervalClickedCallback( (index,interval) ->
                    setupSelectionInterval(intervalTierComponent, index, interval)
            );
            add(intervalTierComponent);
            recordDataIntervalTiers.put(phoTierDesc.getName(), intervalTierComponent);
        }

        for(String timelineTierName: intervalTiers.getRecordIntervalTiers()) {
            final RecordIntervalTier recordTimelineTier = new RecordIntervalTier(session, timelineTierName);
            final IntervalTier intervalTier = new IntervalTier(recordTimelineTier);
            intervalTier.putExtension(RecordIntervalTier.class, recordTimelineTier);
            final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(this.intervalTierTimeModel, intervalTier);
            intervalTierComponent.setIntervalClickedCallback( (index,interval) ->
                    setupSelectionInterval(intervalTierComponent, index, interval)
            );
            add(intervalTierComponent);
            recordDataIntervalTiers.put(timelineTierName, intervalTierComponent);
        }

        for(var timelineTier : intervalTiers.getTiers()) {
            final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(this.intervalTierTimeModel, timelineTier);
            intervalTierComponent.setIntervalClickedCallback( (index,interval) ->
                    setupSelectionInterval(intervalTierComponent, index, interval)
            );
            add(intervalTierComponent);
            sessionLevelIntervalTiers.put(timelineTier.getName(), intervalTierComponent);
        }

    }

    /**
     * Setup selection for given interval tier name and index
     * @param tierComponent the interval tier component
     * @param intervalIndex the interval index
     * @param interval the interval
     */
    private void setupSelectionInterval(IntervalTierComponent tierComponent, int intervalIndex, IntervalTier.Interval interval) {
        getParentView().setSelection(interval.getStart(), interval.getEnd());

        // remove old markers
        if(currentInterval != null) {
            intervalTierTimeModel.removeInterval(currentInterval);
        }

        currentIntervalTierComponent = tierComponent;
        currentIntervalIndex = intervalIndex;
        final IntervalTier.Interval previousInterval = currentIntervalIndex > 0 ? tierComponent.getTimelineTier().getIntervals().get(currentIntervalIndex-1) : null;
        final IntervalTier.Interval nextInterval = (currentIntervalIndex < tierComponent.getTimelineTier().getIntervals().size() - 1) ?
                tierComponent.getTimelineTier().getIntervals().get(currentIntervalIndex+1) : null;
        currentInterval = new TimeUIModel.Interval(interval.getStart(), interval.getEnd());
        currentInterval.getStartMarker().setMaxTime(interval.getEnd());
        if(previousInterval != null) {
            currentInterval.getStartMarker().setMinTime(previousInterval.getStart());
        }
        currentInterval.getEndMarker().setMinTime(interval.getStart());
        if(nextInterval != null) {
            currentInterval.getEndMarker().setMaxTime(nextInterval.getEnd());
        }
        currentInterval.setAutoSwapMarkers(false);
        currentInterval.addPropertyChangeListener(currentIntervalListener);
        intervalTierTimeModel.addInterval(currentInterval);
    }

    public void clearSelectionInterval() {
        if(currentInterval != null) {
            intervalTierTimeModel.removeInterval(currentInterval);
            currentInterval.removePropertyChangeListener(currentIntervalListener);
            currentInterval = null;
            currentIntervalIndex = -1;
            currentIntervalTierComponent = null;
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
            return recordDataIntervalTiers.get(UserTierType.Wor.getPhonTierName());
        }

        private IntervalTierComponent phoneIntervalTierComponent() {
            return recordDataIntervalTiers.get(UserTierType.PhoneIntervals.getPhonTierName());
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            final IntervalTierComponent wordIntervalTier = wordIntervalTierComponent();
            final RecordIntervalTier worIntervalTier = (wordIntervalTier == null ? null : wordIntervalTier.getTimelineTier().getExtension(RecordIntervalTier.class));
            final IntervalTierComponent phoneIntervalTier = phoneIntervalTierComponent();
            final RecordIntervalTier phoIntervalTier = (phoneIntervalTier == null ? null : phoneIntervalTier.getTimelineTier().getExtension(RecordIntervalTier.class));
            if(worIntervalTier == null && phoIntervalTier == null) return;

            if(e.getValueIsAdjusting()) return;
            if(e.getSource() == wordIntervalTier.getSelectionModel()) {
                if(phoneIntervalTier == null) return;
                // get indices of selected phone intervals for the word
                final int selectedWord = wordIntervalTier.getSelectedIndex();
                if(selectedWord == -1) return;
                final IntervalTier.Interval wordInterval = wordIntervalTier.getTimelineTier().getIntervals().get(selectedWord);
                final var phoneIndiciesForWord =
                    ((IntervalTierComponentUI)phoneIntervalTier.getUI()).getIntervalIndicesForTimeRange(wordInterval.getStart(), wordInterval.getEnd());
                if(phoneIndiciesForWord.isEmpty()) {
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

                // select the record which contains this word interval
                for(int i = 0; i < getParentView().getEditor().getSession().getRecordCount(); i++) {
                    final Record r = getParentView().getEditor().getSession().getRecord(i);
                    final Range intervalRange = worIntervalTier.getRecordIntervalData(true).recordRanges().get(r);
                    if(intervalRange != null && intervalRange.contains(selectedWord)) {
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
                // find the record which contains this phone interval
                int recordIdx = -1;
                for(int i = 0; i < getParentView().getEditor().getSession().getRecordCount(); i++) {
                    final Record r = getParentView().getEditor().getSession().getRecord(i);
                    final Range intervalRange = phoIntervalTier.getRecordIntervalData(true).recordRanges().get(r);
                    if(intervalRange != null && intervalRange.contains(selectedPhone)) {
                        recordIdx = i;
                        break;
                    }
                }
                if(recordIdx == -1) return;
                final Record r = getParentView().getEditor().getSession().getRecord(recordIdx);

                // find the word interval that contains the phone interval
                final var wordIndicesForRecord = worIntervalTier.getRecordIntervalData(true).recordRanges().get(r);
                if(wordIndicesForRecord == null) return;

                for(int wordIndex:wordIndicesForRecord) {
                    final IntervalTier.Interval wordInterval = wordIntervalTier.getTimelineTier().getIntervals().get(wordIndex);
                    if(wordInterval.getStart() <= phoneInterval.getStart() &&
                            wordInterval.getEnd() >= phoneInterval.getEnd()) {
                        wordIntervalTier.getSelectionModel().setSelectionInterval(wordIndex, wordIndex);
                        wordIntervalTier.repaint();
                        break;
                    }
                }
            }
        }

    };

    private final PropertyChangeListener currentIntervalListener = (e) -> {
        if(currentIntervalTierComponent == null || currentIntervalIndex == -1) return;
        String tierName = null;
        for(var entry: recordDataIntervalTiers.entrySet()) {
            if(entry.getValue() == currentIntervalTierComponent) {
                tierName = entry.getKey();
                break;
            }
        }
        if(tierName == null) return;
        if("startMarker.time".equals(e.getPropertyName()) || "endMarker.time".equals(e.getPropertyName())) {
            final Record currentRecord = getParentView().getEditor().currentRecord();
            final MediaSegment seg = currentRecord.getMediaSegment();
            if(seg.isPoint()) return;
//            final int[] recordIntervalIndices = currentIntervalTierComponent.getIntersectingIntervals(seg.getStartTime(), seg.getEndTime());
//            final int offset = (recordIntervalIndices.length > 0 ? recordIntervalIndices[0] : 0);
//            final int idx = Math.max(0, currentIntervalIndex - offset);
            if(UserTierType.Wor.getPhonTierName().equals(tierName)) {
                final RecordIntervalTier worIntervalTier = currentIntervalTierComponent.getTimelineTier().getExtension(RecordIntervalTier.class);
                if(worIntervalTier == null) return;
                final Range recordIntervalIndices = worIntervalTier.getRecordIntervalData(true).recordRanges().get(currentRecord);
                if(recordIntervalIndices == null) return;
                final int offset = recordIntervalIndices.getStart();
                final int idx = Math.max(0, currentIntervalIndex - offset);
                if(idx < 0) return;

                final InternalMedia newInterval = new InternalMedia(currentInterval.getStartMarker().getTime(), currentInterval.getEndMarker().getTime());
                final WorTierUpdater updater = new WorTierUpdater(idx, newInterval);
                final Tier<Orthography> worTier = (Tier<Orthography>)currentRecord.getTier(tierName);
                final Orthography wor = worTier.getValue();
                wor.accept(updater);
                final Orthography updatedWor = updater.getUpdatedOrthography();

//                // if modifying the first or last interval, and the start/end time overlaps the previous/next
//                // record segment, update the %wor tier for the previous/next record as well  Do this first to avoid
//                // issues with overlapping record intervals
//                if(idx == 0) {
//                    final int prevRecordIdx = getParentView().getEditor().getCurrentRecordIndex() - 1;
//                    if(prevRecordIdx >= 0) {
//                        final Record prevRecord = getParentView().getEditor().getSession().getRecord(prevRecordIdx);
//                        final MediaSegment prevSeg = prevRecord.getMediaSegment();
//                        final int[] prevRecordIntervalIndices = currentIntervalTierComponent.getIntersectingIntervals(prevSeg.getStartTime(), prevSeg.getEndTime());
//                        if(prevRecordIntervalIndices.length == 0) return;
//
//                        // modify the previous segment end time if it overlaps
//                        if(prevSeg.getEndTime() > currentInterval.getStartMarker().getTime()) {
//                            final Tier<Orthography> prevWorTier = (Tier<Orthography>)prevRecord.getTier(tierName);
//                            final Orthography prevWor = prevWorTier.getValue();
//                            final var lastRecordInterval = prevRecordIntervalIndices[prevRecordIntervalIndices.length - 1];
//                            final int prevIdx = Math.max(0, lastRecordInterval - prevRecordIntervalIndices[0]);
//                            final InternalMedia prevNewInterval = new InternalMedia(prevSeg.getStartTime(), currentInterval.getStartMarker().getTime());
//                            final WorTierUpdater prevUpdater = new WorTierUpdater(prevIdx, prevNewInterval);
//                            prevWor.accept(prevUpdater);
//                            final Orthography updatedPrevWor = prevUpdater.getUpdatedOrthography();
//                            System.out.println(updatedPrevWor);
//                            final TierEdit<Orthography> prevWorEdit =
//                                    new TierEdit<Orthography>(getParentView().getEditor().getSession(), getParentView().getEditor().getEventManager(),
//                                            getParentView().getEditor().getDataModel().getTranscriber(), prevRecord,
//                                            (Tier<Orthography>)prevRecord.getTier(UserTierType.Wor.getPhonTierName()), updatedPrevWor, currentInterval.isValueAdjusting());
//                            getParentView().getEditor().getUndoSupport().postEdit(prevWorEdit);
//                        }
//                    }
//                }

                final TierEdit<Orthography> worEdit =
                        new TierEdit<Orthography>(getParentView().getEditor().getSession(), getParentView().getEditor().getEventManager(),
                                getParentView().getEditor().getDataModel().getTranscriber(), getParentView().getEditor().currentRecord(),
                                (Tier<Orthography>)getParentView().getEditor().currentRecord().getTier(UserTierType.Wor.getPhonTierName()), updatedWor, currentInterval.isValueAdjusting());
                getParentView().getEditor().getUndoSupport().postEdit(worEdit);
            } else if(UserTierType.PhoneIntervals.getPhonTierName().equals(tierName)) {
                // first get the word interval that contains this phone interval,
                // then all of the other phone intervals within that word interval
                final IntervalTierComponent wordIntervalTier = recordDataIntervalTiers.get(UserTierType.Wor.getPhonTierName());
                if(wordIntervalTier == null) return;
                final IntervalTier.Interval phoneInterval = currentIntervalTierComponent.getTimelineTier().getIntervals().get(currentIntervalIndex);

            } else {

            }
        } else if("valueAdjusting".equals(e.getPropertyName())) {
            if((boolean)e.getNewValue()) {
                getParentView().getEditor().getUndoSupport().beginUpdate("Adjust interval");
            } else {
                if(UserTierType.Wor.getPhonTierName().equals(tierName)) {
                    final Record currentRecord = getParentView().getEditor().currentRecord();
                    final MediaSegment seg = currentRecord.getMediaSegment();
                    if(seg.isPoint()) return;
                    final int[] recordIntervalIndices = currentIntervalTierComponent.getIntersectingIntervals(seg.getStartTime(), seg.getEndTime());
                    final int offset = (recordIntervalIndices.length > 0 ? recordIntervalIndices[0] : 0);
                    final int idx = Math.max(0, currentIntervalIndex - offset);
                    final InternalMedia newInterval = new InternalMedia(this.currentInterval.getStartMarker().getTime(), this.currentInterval.getEndMarker().getTime());
                    final WorTierUpdater updater = new WorTierUpdater(idx, newInterval);
                    final Tier<Orthography> worTier = (Tier<Orthography>)currentRecord.getTier(tierName);
                    final Orthography wor = worTier.getValue();
                    wor.accept(updater);
                    final Orthography updatedWor = updater.getUpdatedOrthography();
                    final TierEdit<Orthography> worEdit =
                            new TierEdit<Orthography>(getParentView().getEditor().getSession(), getParentView().getEditor().getEventManager(),
                                    getParentView().getEditor().getDataModel().getTranscriber(), getParentView().getEditor().currentRecord(),
                                    (Tier<Orthography>)getParentView().getEditor().currentRecord().getTier(UserTierType.Wor.getPhonTierName()), updatedWor, currentInterval.isValueAdjusting());
                    getParentView().getEditor().getUndoSupport().postEdit(worEdit);
                }
                getParentView().getEditor().getUndoSupport().endUpdate();
            }
        }
    };

    public static class WorTierUpdater extends VisitorAdapter<OrthographyElement> {

        private final int intervalIndex;

        private InternalMedia newInterval;

        private OrthographyBuilder builder = new OrthographyBuilder();

        private int currentIndex = 0;

        public WorTierUpdater(int intervalIndex, InternalMedia newInterval) {
            super();
            this.intervalIndex = intervalIndex;
            this.newInterval = newInterval;
        }

        @Visits
        public void visitInternalMedia(InternalMedia internalMedia) {
            if(currentIndex == intervalIndex) {
                builder.append(newInterval);
            } else {
                builder.append(internalMedia);
            }
            currentIndex++;
        }

        @Override
        public void fallbackVisit(OrthographyElement element) {
            builder.append(element);
        }

        public Orthography getUpdatedOrthography() {
            return builder.toOrthography();
        }
    }

}
