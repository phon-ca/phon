package ca.phon.app.session.editor.view.speechAnalysis;

import ca.phon.app.session.timeline.RecordTimelineTier;
import ca.phon.app.session.timeline.TimelineTierComponent;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.session.Session;
import ca.phon.session.Timeline;
import ca.phon.session.TimelineTier;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.sql.Time;

/**
 * Tier for displaying intervals from the session {@link ca.phon.session.Timeline}
 */
public class SpeechAnalysisIntervalsTier extends SpeechAnalysisTier {

    public SpeechAnalysisIntervalsTier(SpeechAnalysisEditorView parentView) {
        super(parentView);

        init();
    }

    private void init() {
        setLayout(new VerticalLayout());

        // add a tier for each session timeline tier
        final Session session = getParentView().getEditor().getSession();
        final Timeline timeline = session.getTimeline();

        for(String timelineTierName:timeline.getRecordTimelineTiers()) {
            final RecordTimelineTier recordTimelineTier = new RecordTimelineTier(session, timelineTierName);
            final TimelineTier timelineTier = new TimelineTier(recordTimelineTier);
            final TimelineTierComponent timelineTierComponent = new TimelineTierComponent(getParentView().getTimeModel(), timelineTier);
            add(timelineTierComponent);
        }

        for(var timelineTier : timeline.getTiers()) {
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
