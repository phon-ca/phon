package ca.phon.app.session.editor.view.transcript.extensions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.transcript.*;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.position.TranscriptElementLocation;
import ca.phon.ui.CalloutWindow;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * An extension that provides media segment playback and editing support to the {@link TranscriptEditor}
 * for record media segments as well as internal-media segments.
 */
public class MediaSegmentExtension implements TranscriptEditorExtension {
    private TranscriptEditor editor;

    /* State */

    private MediaSegment selectedSegment = null;
    /**
     * A {@link KeyAdapter} that lets the user toggle playback of a segment by pressing space
     * when a segment is selected
     */
    private final KeyAdapter onSpace = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (selectedSegment != null && e.getKeyCode() == KeyEvent.VK_SPACE) {
                if (editor.getSegmentPlayback() != null) {
                    if (editor.getSegmentPlayback().isPlaying()) {
                        editor.getSegmentPlayback().stopPlaying();
                    } else {
                        editor.getSegmentPlayback().playSegment(selectedSegment);
                    }
                }
            }
        }
    };

    /**
     * Constructor
     */
    public MediaSegmentExtension() {
        super();
    }

    @Override
    public void install(TranscriptEditor editor) {
        this.editor = editor;

        editor.addKeyListener(onSpace);
        editor.getTranscriptDocument().addInsertionHook(new DefaultInsertionHook() {
            @Override
            public List<DefaultStyledDocument.ElementSpec> batchInsertString(StringBuilder buffer, MutableAttributeSet attrs) {
                MediaSegment segment = (MediaSegment) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT);
                if (segment != null) {
                    Record record = (Record) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
                    Tier<MediaSegment> segmentTier = (Tier<MediaSegment>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                    if (record != null && segmentTier != null) {
                        PhonUIAction<SegmentCalloutInfo> showSegmentEditCalloutAct = PhonUIAction.consumer(MediaSegmentExtension.this::showSegmentEditCallout,
                                new SegmentCalloutInfo(record, segmentTier));
                        attrs.addAttribute(TranscriptStyleConstants.ATTR_KEY_ENTER_ACTION, showSegmentEditCalloutAct);
                    }
                }
                return new ArrayList<>();
            }
        });

        editor.getEventManager().registerActionForEvent(TranscriptEditor.transcriptLocationChanged, this::onTranscriptLocationChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
    }

    /**
     * Shows the segment edit callout
     *
     * @param segmentCalloutInfo infor for callout
     */
    private void showSegmentEditCallout(SegmentCalloutInfo segmentCalloutInfo) {
        final var segmentEditor = getSegmentEditorPopup(segmentCalloutInfo);

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
                    true,
                    segmentEditor,
                    SwingConstants.NORTH,
                    point
            );
        } catch (BadLocationException e) {
            LogUtil.warning(e);
        }
    }

    private void onTranscriptLocationChanged(EditorEvent<TranscriptEditor.TranscriptLocationChangeData> evt) {
        final TranscriptElementLocation loc = evt.getData().get().newLoc();
        if (SystemTierType.Segment.getName().equals(loc.tier())) {
            final int recordIndex = editor.getSession().getTranscript().getRecordIndex(loc.transcriptElementIndex());
            if (recordIndex >= 0) {
                final Record record = editor.getSession().getRecord(recordIndex);
                final var segmentTier = record.getSegmentTier();
                final var segmentBounds = editor.getTranscriptDocument().getTierContentStartEnd(recordIndex, loc.tier());
                if (segmentBounds.valid()) {
                    selectedSegment = segmentTier.getValue();
//                    editor.boxSelectBounds(segmentBounds);
                    return;
                }
            }
        }

        // remove box select if we are not in a segment anymore
        if (selectedSegment != null) {
//            editor.removeCurrentBoxSelect();
            selectedSegment = null;
        }
    }

    @NotNull
    private SegmentEditorPopup getSegmentEditorPopup(SegmentCalloutInfo segmentCalloutInfo) {
        final Tier<MediaSegment> segmentTier = segmentCalloutInfo.segmentTier();
        final Record record = segmentCalloutInfo.record();
        var segmentEditor = new SegmentEditorPopup(editor.getMediaModel(), segmentTier.getValue());
        segmentEditor.setPreferredSize(new Dimension(segmentEditor.getPreferredPopupWidth(), (int) segmentEditor.getPreferredSize().getHeight()));

        segmentEditor.addPropertyChangeListener("segment", e -> {
            if (e.getNewValue() != null) {
                final TierEdit<MediaSegment> tierEdit = new TierEdit<MediaSegment>(editor.getSession(),
                        editor.getEventManager(), record, segmentTier, (MediaSegment) e.getNewValue());
                editor.getUndoSupport().postEdit(tierEdit);
            }
        });
        return segmentEditor;
    }

    private record SegmentCalloutInfo(Record record, Tier<MediaSegment> segmentTier) {
    }

    private class SegmentPlaybackComponentFactory implements ComponentFactory {

        @Override
        public JComponent createComponent(AttributeSet attrs) {
            return null;
        }

        @Override
        public JComponent getComponent() {
            return null;
        }

        @Override
        public void requestFocusStart() {

        }

        @Override
        public void requestFocusEnd() {

        }

        @Override
        public void requestFocusAtOffset(int offset) {

        }
    }
}
