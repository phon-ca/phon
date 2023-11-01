package ca.phon.app.session.editor.view.transcriptEditor.extensions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.view.transcriptEditor.*;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.session.MediaSegment;
import ca.phon.ui.CalloutWindow;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Media segment extensions for the {@link TranscriptEditor}
 */
public class MediaSegmentExtensions implements TranscriptEditorExtension {

    private TranscriptEditor editor;
    private MediaSegment selectedSegment = null;

    public MediaSegmentExtensions() {
        super();
    }

    @Override
    public void install(TranscriptEditor editor) {
        this.editor = editor;

        editor.addKeyListener(onSpace);

        editor.getTranscriptDocument().addInsertionHook((buffer, attrs) -> {
            MediaSegment segment = (MediaSegment) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT);
            if (segment != null) {
                PhonUIAction<MediaSegment> showSegmentEditCalloutAct = PhonUIAction.consumer(MediaSegmentExtensions.this::showSegmentEditCallout, segment);
                attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_ENTER_ACTION, showSegmentEditCalloutAct);
            }
            return new ArrayList<>();
        });
        editor.addCaretMovementHook((dot, attrs) -> {
            TranscriptDocument doc = editor.getTranscriptDocument();

            MediaSegment segment = (MediaSegment) attrs.getAttribute("mediaSegment");
            boolean isSegment = segment != null;

            int segmentIncludedPos = dot;
            if (!isSegment) {
                segment = (MediaSegment) doc.getCharacterElement(dot - 1).getAttributes().getAttribute("mediaSegment");
                segmentIncludedPos--;
            }

            if (segment != null) {
                System.out.println("Segment is present");
                if (!segment.equals(selectedSegment)) {
                    System.out.println(segment);
                    System.out.println("Select segment");
                    selectedSegment = segment;
                    var segmentBounds = doc.getSegmentBounds(segment, segmentIncludedPos);
                    editor.boxSelectBounds(segmentBounds);
                }
            }
            else {
                System.out.println("Segment is not present");
                System.out.println("Selected segment is null?: " + selectedSegment == null);
                if (selectedSegment != null) {
                    System.out.println("Deselect segment");
                    editor.removeCurrentBoxSelect();
                    selectedSegment = null;
                }
            }
        });
    }

    // Add playback when pressing space while caret is inside a media segment
    private final KeyAdapter onSpace = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
        if (selectedSegment != null && e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (editor.getSegmentPlayback() != null) {
                if(editor.getSegmentPlayback().isPlaying()) {
                    editor.getSegmentPlayback().stopPlaying();
                } else {
                    editor.getSegmentPlayback().playSegment(selectedSegment);
                }
            }
        }
        }
    };

    private void showSegmentEditCallout(MediaSegment segment) {
        var segmentEditor = new SegmentEditorPopup(editor.getMediaModel(), segment);
        segmentEditor.setPreferredSize(new Dimension(segmentEditor.getPreferredPopupWidth(), (int) segmentEditor.getPreferredSize().getHeight()));

        try {
            var start = editor.modelToView2D(editor.getSelectionStart());
            var end = editor.modelToView2D(editor.getSelectionEnd());

            Point point = new Point(
                (int) (((end.getBounds().getMaxX() - start.getBounds().getMinX()) / 2) + start.getBounds().getMinX()),
                (int) start.getCenterY()
            );

            point.x += editor.getLocationOnScreen().x;
            point.y += editor.getLocationOnScreen().y;

            System.out.println(point);
            System.out.println(editor.getLocationOnScreen());

            CalloutWindow.showCallout(
                CommonModuleFrame.getCurrentFrame(),
                segmentEditor,
                SwingConstants.NORTH,
                SwingConstants.CENTER,
                point
            );
        }
        catch (BadLocationException e) {
            LogUtil.warning(e);
        }
    }
}
