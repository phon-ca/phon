package ca.phon.app.session.editor.view.speechAnalysis;

import ca.phon.app.session.intervalTiers.IntervalTierComponent;
import ca.phon.app.session.intervalTiers.RecordIntervalTier;
import ca.phon.session.*;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;

/**
 * Tier for displaying intervals from the session {@link IntervalTiers}
 */
public class SpeechAnalysisIntervalsTier extends SpeechAnalysisTier {

    public SpeechAnalysisIntervalsTier(SpeechAnalysisEditorView parentView) {
        super(parentView);

        init();
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
        }

        for(String timelineTierName: intervalTiers.getRecordIntervalTiers()) {
            final RecordIntervalTier recordTimelineTier = new RecordIntervalTier(session, timelineTierName);
            final IntervalTier intervalTier = new IntervalTier(recordTimelineTier);
            final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(getParentView().getTimeModel(), intervalTier);
            add(intervalTierComponent);
        }

        for(var timelineTier : intervalTiers.getTiers()) {
            final IntervalTierComponent intervalTierComponent = new IntervalTierComponent(getParentView().getTimeModel(), timelineTier);
            add(intervalTierComponent);
        }
    }

    @Override
    public void addMenuItems(JMenu menuEle, boolean includeAccelerators) {

    }

    @Override
    public void onRefresh() {

    }

}
