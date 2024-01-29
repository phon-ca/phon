package ca.phon.app.session.editor.view.speechAnalysis;

import javax.swing.*;
import java.awt.*;

public class TextGridTier extends SpeechAnalysisTier {

    public TextGridTier(SpeechAnalysisEditorView parentView) {
        super(parentView);

        init();
    }

    private void init() {
        setLayout(new BorderLayout());
    }

    @Override
    public void addMenuItems(JMenu menuEle, boolean includeAccelerators) {

    }

    @Override
    public void onRefresh() {

    }

}
