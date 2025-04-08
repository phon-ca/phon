package ca.phon.app.session.editor.view.speechAnalysis;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;

public final class SpeechAnalysisIntervalsTierExtPt implements IPluginExtensionFactory<SpeechAnalysisTier>, IPluginExtensionPoint<SpeechAnalysisTier> {

    @Override
    public Class<?> getExtensionType() {
        return SpeechAnalysisTier.class;
    }

    @Override
    public IPluginExtensionFactory<SpeechAnalysisTier> getFactory() {
        return this;
    }

    @Override
    public SpeechAnalysisTier createObject(Object... args) {
        if(args[0] instanceof SpeechAnalysisEditorView view) {
            return new SpeechAnalysisIntervalsTier(view);
        } else {
            throw new IllegalArgumentException("Invalid arguments");
        }
    }

}
