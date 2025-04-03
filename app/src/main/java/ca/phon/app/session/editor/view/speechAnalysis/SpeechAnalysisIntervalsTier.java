package ca.phon.app.session.editor.view.speechAnalysis;

import ca.phon.app.session.timeline.TimelineTierComponent;
import ca.phon.session.Session;
import ca.phon.session.Timeline;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;

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
