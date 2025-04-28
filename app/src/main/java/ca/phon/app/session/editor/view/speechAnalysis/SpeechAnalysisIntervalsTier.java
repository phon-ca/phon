package ca.phon.app.session.editor.view.speechAnalysis;

import ca.phon.app.session.timeline.RecordIntervalTier;
import ca.phon.app.session.timeline.TimelineTierComponent;
import ca.phon.session.IntervalTier;
import ca.phon.session.IntervalTiers;
import ca.phon.session.Session;
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

        for(String timelineTierName: intervalTiers.getRecordTimelineTiers()) {
            final RecordIntervalTier recordTimelineTier = new RecordIntervalTier(session, timelineTierName);
            final IntervalTier intervalTier = new IntervalTier(recordTimelineTier);
            final TimelineTierComponent timelineTierComponent = new TimelineTierComponent(getParentView().getTimeModel(), intervalTier);
            add(timelineTierComponent);
        }

        for(var timelineTier : intervalTiers.getTiers()) {
            final TimelineTierComponent timelineTierComponent = new TimelineTierComponent(getParentView().getTimeModel(), timelineTier);
            add(timelineTierComponent);
        }
    }

    @Override
    public void addMenuItems(JMenu menuEle, boolean includeAccelerators) {

    }

    @Override
    public void onRefresh() {

    }

}
