package ca.phon.app.session.editor.view.transcript.extensions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.transcript.*;
import ca.phon.formatter.MediaTimeFormatStyle;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.format.MediaSegmentFormatter;
import ca.phon.session.position.TranscriptElementLocation;
import ca.phon.ui.CalloutWindow;
import ca.phon.ui.action.PhonUIAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An extension that provides media segment playback and editing support to the {@link TranscriptEditor}
 * for record media segments as well as internal-media segments.
 */
public class MediaSegmentExtension implements TranscriptEditorExtension {
    private TranscriptEditor editor;

    private AtomicReference<CalloutWindow> calloutRef = new AtomicReference<>();

    /* State */

//    private MediaSegment selectedSegment = null;
    /**
     * A {@link KeyAdapter} that lets the user toggle playback of a segment by pressing space
     * when a segment is selected
     */
    private final KeyAdapter onSpace = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
//            if (selectedSegment != null && e.getKeyCode() == KeyEvent.VK_SPACE) {
//                if (editor.getSegmentPlayback() != null) {
//                    if (editor.getSegmentPlayback().isPlaying()) {
//                        editor.getSegmentPlayback().stopPlaying();
//                    } else {
//                        editor.getSegmentPlayback().playSegment(selectedSegment);
//                    }
//                }
//            }
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
        editor.getTranscriptDocument().addInsertionHook(new MediaSegmentInsertionHook());

        editor.getEventManager().registerActionForEvent(TranscriptEditor.transcriptLocationChanged,
                this::onTranscriptLocationChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
    }

    private void onTranscriptLocationChanged(EditorEvent<TranscriptEditor.TranscriptLocationChangeData> evt) {
        final TranscriptElementLocation loc = evt.getData().get().newLoc();
        final TranscriptElementLocation oldLoc = evt.getData().get().oldLoc();

        LogUtil.info("[Media ext] - Transcript location changed: " + oldLoc + " -> " + loc);
        if ((loc.transcriptElementIndex() != oldLoc.transcriptElementIndex()) || !(loc.tier().equals(oldLoc.tier()))) {
            if(calloutRef.get() != null && calloutRef.get() == editor.getCurrentCallout()) {
                editor.getCurrentCallout().setVisible(false);
                editor.getCurrentCallout().dispose();
            }

            if (SystemTierType.Segment.getName().equals(loc.tier())) {
                final int recordIndex = editor.getSession().getTranscript().getRecordIndex(loc.transcriptElementIndex());
                if (recordIndex >= 0) {
                    final Record record = editor.getSession().getRecord(recordIndex);
                    final var segmentTier = record.getSegmentTier();
                    final var segmentBounds = editor.getTranscriptDocument().getTierContentStartEnd(recordIndex, loc.tier());
                    if (segmentBounds.valid()) {
                    }
                }
            }
        }
    }

    /**
     * Shows the segment edit callout
     *
     * @param segmentCalloutInfo info for callout
     */
    private void showSegmentEditCallout(SegmentCalloutInfo segmentCalloutInfo) {
        final var segmentEditor = getSegmentEditorPopup(segmentCalloutInfo);

        try {
            final int recordIndex = editor.getSession().getRecordPosition(segmentCalloutInfo.record());
            if (recordIndex < 0) {
                return;
            }
            final TranscriptDocument.StartEnd startEnd =
                    editor.getTranscriptDocument().getTierContentStartEnd(recordIndex, segmentCalloutInfo.segmentTier.getName());
            if (!startEnd.valid()) {
                return;
            }

            // box select bounds
            editor.boxSelectBounds(startEnd);

            var start = editor.modelToView2D(startEnd.start());
            var end = editor.modelToView2D(startEnd.end());

            final Point topLeft = new Point((int) start.getX(), (int) start.getY());
            final Point bottomRight = new Point((int) end.getMaxX(), (int) end.getMaxY());
            SwingUtilities.convertPointToScreen(topLeft, editor);
            SwingUtilities.convertPointToScreen(bottomRight, editor);
            var pointAt = new Rectangle(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);

            CalloutWindow currentSegmentCallout = editor.showNonFocusableCallout(false, segmentEditor, SwingConstants.NORTH, pointAt);
            currentSegmentCallout.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    calloutRef.set(null);
                }
            });
            calloutRef.set(currentSegmentCallout);
        } catch (BadLocationException e) {
            LogUtil.warning(e);
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

    /**
     * Information used to show media segment callout window for editing
     *
     * @param record
     * @param segmentTier
     */
    private record SegmentCalloutInfo(Record record, Tier<MediaSegment> segmentTier) {
    }

    private class MediaSegmentInsertionHook extends DefaultInsertionHook {
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
                    TranscriptDocumentFilter.setCustomFilter(attrs, new MediaSegmentDocumentFilter(editor.getTranscriptDocument()));
                }
            }
            return new ArrayList<>();
        }
    }

    private class MediaSegmentDocumentFilter extends DocumentFilter {

        private final TranscriptDocument doc;

        public MediaSegmentDocumentFilter(TranscriptDocument doc) {
            this.doc = doc;
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            final TranscriptElementLocation location = doc.charPosToSessionLocation(offset);
            // Locked tiers - if locked, do not allow editing
            Record record = TranscriptStyleConstants.getRecord(attrs);
            Tier<?> tier = TranscriptStyleConstants.getTier(attrs);

            // •MMM:SS.sss-MMM:SS.sss•
            final Pattern mediaSegmentPattern = Pattern.compile("•(([0-9]{3}):([0-9]{2})\\.([0-9]{3})-([0-9]{3}):([0-9]{2})\\.([0-9]{3}))•");

            // media segment text will be editable, but only with formatted text
            final MediaSegment mediaSegment = TranscriptStyleConstants.getMediaSegment(attrs);
            if (mediaSegment != null) {
                // allow editing of media segment text by overwriting it with the new text
                // first get current value
                final TranscriptDocument.StartEnd mediaSegmentStartEnd = doc.getSegmentBounds(mediaSegment, offset);
                if (mediaSegmentStartEnd.valid()) {
                    final String currentText = doc.getText(mediaSegmentStartEnd.start(), mediaSegmentStartEnd.length());

                    final String replacedText = currentText.substring(0, location.charPosition())
                            + text + currentText.substring(location.charPosition() + text.length());

                    TranscriptElementLocation newLocation = location;
                    final Matcher matcher = mediaSegmentPattern.matcher(replacedText);
                    if (matcher.matches()) {
                        editor.getTranscriptEditorCaret().freeze();
                        doc.setBypassDocumentFilter(true);
                        try {
                            doc.remove(mediaSegmentStartEnd.start(), mediaSegmentStartEnd.length());
                            final TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder(doc);
                            final SimpleAttributeSet tierAttrs = new SimpleAttributeSet();
                            tierAttrs.addAttributes(doc.getTranscriptStyleContext().getRecordAttributes(record));
                            tierAttrs.addAttributes(doc.getTranscriptStyleContext().getTierAttributes(tier));
                            final MediaSegmentFormatter segmentFormatter = new MediaSegmentFormatter(MediaTimeFormatStyle.PADDED_MINUTES_AND_SECONDS);
                            final MediaSegment newSegment = segmentFormatter.parse(matcher.group(1));
                            batchBuilder.appendFormattedSegment(newSegment, tierAttrs);
                            doc.processBatchUpdates(mediaSegmentStartEnd.start(), batchBuilder.getBatch());
                            newLocation =
                                    new TranscriptElementLocation(location.transcriptElementIndex(), location.tier(), location.charPosition() + text.length());
                        } catch (BadLocationException | ParseException e) {
                            LogUtil.severe(e);
                        } finally {
                            doc.setBypassDocumentFilter(false);
                            editor.getTranscriptEditorCaret().unfreeze();
                        }
                        if (newLocation.valid()) {
                            final int caretPos = doc.sessionLocationToCharPos(newLocation);
                            if (caretPos >= 0) {
                                editor.setCaretPosition(caretPos);
                            }
                        }
                    }
                }
            }
        }
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
