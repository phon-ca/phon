package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.EditorViewCategory;
import ca.phon.app.session.editor.EditorViewInfo;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(name= TranscriptView.VIEW_NAME)
@EditorViewInfo(name= TranscriptView.VIEW_NAME, category= EditorViewCategory.RECORD, icon= TranscriptView.VIEW_ICON)
public class TranscriptEditorViewExtPt implements IPluginExtensionPoint<EditorView>, IPluginExtensionFactory<EditorView> {

    @Override
    public EditorView createObject(Object... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException();
        }
        if (args[0] instanceof SessionEditor editor) {
            return new TranscriptView(editor);
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Class<?> getExtensionType() {
        return EditorView.class;
    }

    @Override
    public IPluginExtensionFactory<EditorView> getFactory() {
        return this;
    }
}
