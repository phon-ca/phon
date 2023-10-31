package ca.phon.app.session.editor.view.transcriptEditor.extensions;

import ca.phon.app.session.editor.view.transcriptEditor.TranscriptEditor;
import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;

@Extension(TranscriptEditor.class)
public class DefaultEditorExtensionProvider implements ExtensionProvider {

    @Override
    public void installExtension(IExtendable obj) {
        if(obj instanceof TranscriptEditor editor) {
            final MediaSegmentExtensions segmentExtensions = new MediaSegmentExtensions();
            segmentExtensions.install(editor);
        }
    }

}
