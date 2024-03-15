package ca.phon.app.session.editor.view.transcript.extensions;

import ca.phon.app.session.editor.view.transcript.TranscriptEditor;
import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;

@Extension(TranscriptEditor.class)
public class DefaultEditorExtensionProvider implements ExtensionProvider {

    @Override
    public void installExtension(IExtendable obj) {
        if (obj instanceof TranscriptEditor editor) {
            final TierLabelMenuExtension tierLabelMenuExtension = new TierLabelMenuExtension();
            tierLabelMenuExtension.install(editor);

            final MediaSegmentExtension segmentExtension = new MediaSegmentExtension();
            segmentExtension.install(editor);

            final SyllabificationExtension syllabificationExtension = new SyllabificationExtension();
            syllabificationExtension.install(editor);

            final AlignmentExtension alignmentExtension = new AlignmentExtension();
            alignmentExtension.install(editor);

            final BlindTranscriptionExtension blindTranscriptionExtension = new BlindTranscriptionExtension();
            blindTranscriptionExtension.install(editor);

            final HeaderTierExtension headerTierExtension = new HeaderTierExtension();
            headerTierExtension.install(editor);

            final AutoTranscriptionExtension autoTranscriptionExtension = new AutoTranscriptionExtension();
            autoTranscriptionExtension.install(editor);
        }
    }

}
