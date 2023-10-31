package ca.phon.app.session.editor.view.transcriptEditor.extensions;

import ca.phon.app.session.editor.view.transcriptEditor.TranscriptEditor;
import ca.phon.plugin.IPluginExtensionFactory;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Media segment extensions for the {@link TranscriptEditor}
 */
public class MediaSegmentExtensions implements TranscriptEditorExtension {

    private TranscriptEditor editor;

    public MediaSegmentExtensions() {
        super();
    }

    @Override
    public void install(TranscriptEditor editor) {
        this.editor = editor;

        editor.addKeyListener(onSpace);
    }

    // add playback when pressing space while caret is inside a media segment
    private final KeyAdapter onSpace = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (editor.getCurrentMediaSegment() != null && e.getKeyCode() == KeyEvent.VK_SPACE) {
                if (editor.getSegmentPlayback() != null) {
                    if(editor.getSegmentPlayback().isPlaying()) {
                        editor.getSegmentPlayback().stopPlaying();
                    } else {
                        editor.getSegmentPlayback().playSegment(editor.getCurrentMediaSegment());
                    }
                }
            }
        }
    };

}
