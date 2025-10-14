package ca.phon.app.session.editor.view.speechAnalysis;

import ca.phon.ui.menu.MenuBuilder;

/**
 * Interface for adding menu items to interval tier menu
 */
public interface IntervalTierMenuHandler {

    void setupMenu(SpeechAnalysisIntervalsTier speechAnalysisIntervalsTier, MenuBuilder menuBuilder);

}
