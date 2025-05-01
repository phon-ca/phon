package ca.phon.app.session.editor.view.speechAnalysis;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.intervalTiers.IntervalTierComponent;
import ca.phon.app.session.intervalTiers.RecordIntervalTier;
import ca.phon.session.*;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Tier for displaying intervals from the session {@link IntervalTiers}
 */
public class SpeechAnalysisIntervalsTier extends SpeechAnalysisTier {

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

        // add a tier for each session intervalTiers tier
        final Session session = getParentView().getEditor().getSession();
        final IntervalTiers intervalTiers = session.getTimeline();

        // check for word intervals tier
        final TierDescription worTierDesc = session.getUserTiers()
                .stream()
                .filter(td -> UserTierType.Wor.getPhonTierName().equals(td.getName()))
                .findAny().orElse(null);
        if(worTierDesc != null) {
            final RecordIntervalTier recordTimelineTier = new RecordIntervalTier(session, worTierDesc.getName());
            final IntervalTier intervalTier = new IntervalTier(recordTimelineTier);
            final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(getParentView().getTimeModel(), intervalTier);
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
            final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(getParentView().getTimeModel(), intervalTier);
            add(intervalTierComponent);
            intervalTiersMap.put(phoTierDesc.getName(), intervalTierComponent);
        }

        for(String timelineTierName: intervalTiers.getRecordIntervalTiers()) {
            final RecordIntervalTier recordTimelineTier = new RecordIntervalTier(session, timelineTierName);
            final IntervalTier intervalTier = new IntervalTier(recordTimelineTier);
            final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(getParentView().getTimeModel(), intervalTier);
            add(intervalTierComponent);
            intervalTiersMap.put(timelineTierName, intervalTierComponent);
        }

        for(var timelineTier : intervalTiers.getTiers()) {
            final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(getParentView().getTimeModel(), timelineTier);
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

}
