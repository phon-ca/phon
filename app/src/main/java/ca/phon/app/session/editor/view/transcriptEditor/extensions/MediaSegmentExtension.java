package ca.phon.app.session.editor.view.transcriptEditor.extensions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.view.transcriptEditor.*;
import ca.phon.app.session.editor.view.transcriptEditor.InsertionHook;
import ca.phon.session.MediaSegment;
import ca.phon.ui.CalloutWindow;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * An extension that provides media segment support to the {@link TranscriptEditor}
 */
public class MediaSegmentExtension implements TranscriptEditorExtension {
    private TranscriptEditor editor;

    /* State */

    private MediaSegment selectedSegment = null;

    /**
     * Constructor
     * */
    public MediaSegmentExtension() {
        super();
    }

    @Override
    public void install(TranscriptEditor editor) {
        this.editor = editor;

        editor.addKeyListener(onSpace);
        editor.addCaretListener(onCaretMove);

        editor.getTranscriptDocument().addInsertionHook(new DefaultInsertionHook() {
            @Override
            public List<DefaultStyledDocument.ElementSpec> batchInsertString(StringBuilder buffer, MutableAttributeSet attrs) {
                MediaSegment segment = (MediaSegment) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT);
                if (segment != null) {
                    PhonUIAction<MediaSegment> showSegmentEditCalloutAct = PhonUIAction.consumer(MediaSegmentExtension.this::showSegmentEditCallout, segment);
                    attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_ENTER_ACTION, showSegmentEditCalloutAct);
                }
                return new ArrayList<>();
            }
        });
    }

    /**
     * A {@link KeyAdapter} that lets the user toggle playback of a segment by pressing space
     * when a segment is selected
     * */
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

    /**
     * A {@link CaretListener} that handles segment selection when the caret moves into and out of segments
     * */
    private final CaretListener onCaretMove = new CaretListener() {
        @Override
        public void caretUpdate(CaretEvent e) {
            TranscriptDocument doc = editor.getTranscriptDocument();
            AttributeSet attrs = doc.getCharacterElement(e.getDot()).getAttributes();

            MediaSegment segment = (MediaSegment) attrs.getAttribute("mediaSegment");
            boolean isSegment = segment != null;

            int segmentIncludedPos = e.getDot();
            if (!isSegment) {
                segment = (MediaSegment) doc.getCharacterElement(e.getDot() - 1).getAttributes().getAttribute("mediaSegment");
                segmentIncludedPos--;
            }

            if (segment != null) {
                if (!segment.equals(selectedSegment)) {
                    selectedSegment = segment;
                    var segmentBounds = doc.getSegmentBounds(segment, segmentIncludedPos);
                    editor.boxSelectBounds(segmentBounds);
                }
            }
            else {
                if (selectedSegment != null) {
                    editor.removeCurrentBoxSelect();
                    selectedSegment = null;
                }
            }
        }
    };

    /**
     * Shows the segment edit callout
     *
     * @param segment the segment that will be editable in the callout
     * */
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
