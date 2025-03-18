package ca.phon.app.session.editor.view.transcript.extensions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.transcript.*;
import ca.phon.extensions.UnvalidatedValue;
import ca.phon.formatter.MediaTimeFormatStyle;
import ca.phon.orthography.InternalMedia;
import ca.phon.session.*;
import ca.phon.session.Record;
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

    private AtomicReference<SegmentEditorCalloutInfo> calloutRef = new AtomicReference<>();

    private Timer calloutTimer = null;

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
        editor.getEventManager().registerActionForEvent(EditorEventType.TierChange,
                this::onTierChange, EditorEventManager.RunOn.AWTEventDispatchThread);
    }

    private void onTierChange(EditorEvent<EditorEventType.TierChangeData> event) {
        if(calloutRef.get() == null || !calloutRef.get().callout().isVisible()) {
            return;
        }
        final var record = event.getData().get().record();
        if(calloutRef.get().requestInfo().record() != record) {
            return;
        }
        final var tier = event.getData().get().tier();
        if(!SystemTierType.Segment.getName().equals(tier.getName())) {
            return;
        }
        if(event.source() == calloutRef.get().editor()) return;
        if(event.data().valueAdjusting()) return;

        // update segment in callout
        final SegmentEditorPopup segmentEditor = calloutRef.get().editor();
        final var segmentTier = calloutRef.get().requestInfo().segmentTier();
        final var segment = segmentTier.getValue();
        segmentEditor.setMediaSegment(segment.getStartTime(), segment.getEndTime());
    }

    private void onTranscriptLocationChanged(EditorEvent<TranscriptEditor.TranscriptLocationChangeData> evt) {
        final TranscriptElementLocation loc = evt.getData().get().newLoc();
        final TranscriptElementLocation oldLoc = evt.getData().get().oldLoc();

        if(calloutTimer != null && calloutTimer.isRunning()) {
            calloutTimer.stop();
            calloutTimer = null;
        }

        if(SystemTierType.Segment.getName().equals(loc.tier())) {
            final Transcript.Element recordEle = editor.getSession().getTranscript().getElementAt(loc.transcriptElementIndex());
            if (!recordEle.isRecord()) return;
            final Record record = recordEle.asRecord();
            if (loc.tier().equals(oldLoc.tier())) {
                boolean calloutWasVisible = calloutRef.get() != null && calloutRef.get().callout().isVisible();
                if ((loc.transcriptElementIndex() != oldLoc.transcriptElementIndex())) {
                    // hide callout if switching tiers
                    if (calloutRef.get() != null && calloutRef.get().callout() == editor.getCurrentCallout()) {
                        LogUtil.info("Hiding callout due to record switch");
                        editor.getCurrentCallout().setVisible(false);
                        editor.getCurrentCallout().dispose();
                    }
                    // show callout immediately
                    if (calloutWasVisible)
                        showSegmentEditCallout(new SegmentCalloutRequestInfo(record, record.getSegmentTier()));
                } else {
                    if (calloutRef.get() == null || calloutRef.get().callout() != editor.getCurrentCallout()) {
                        // show callout immediately if moving within the media tier
                        showSegmentEditCallout(new SegmentCalloutRequestInfo(record, record.getSegmentTier()));
                    }
                }
            } else {
                // start timer to show callout
                calloutTimer = new Timer(1000, e -> {
                    showSegmentEditCallout(new SegmentCalloutRequestInfo(record, record.getSegmentTier()));
                });
                calloutTimer.setRepeats(false);
                calloutTimer.start();
            }
        } else {
            // hide callout if visible
            if (calloutRef.get() != null && calloutRef.get().callout() == editor.getCurrentCallout()) {
                LogUtil.info("Hiding callout due to tier switch");
                editor.getCurrentCallout().setVisible(false);
                editor.getCurrentCallout().dispose();
            }
        }
    }

    /**
     * Shows the segment edit callout
     *
     * @param segmentCalloutRequestInfo info for callout
     */
    private void showSegmentEditCallout(SegmentCalloutRequestInfo segmentCalloutRequestInfo) {
        if(!editor.getMediaModel().isSessionAudioAvailable()) return;

        final var segmentEditor = getSegmentEditorPopup(segmentCalloutRequestInfo);

        try {
            final int recordIndex = editor.getSession().getRecordPosition(segmentCalloutRequestInfo.record());
            if (recordIndex < 0) {
                return;
            }
            final TranscriptDocument.StartEnd startEnd =
                    editor.getTranscriptDocument().getTierContentStartEnd(recordIndex, segmentCalloutRequestInfo.segmentTier.getName());
            if (!startEnd.valid()) {
                return;
            }

            // box select bounds
//            editor.boxSelectBounds(startEnd);

            var start = editor.modelToView2D(startEnd.start());
            var end = editor.modelToView2D(startEnd.end());

            final Point topLeft = new Point((int) start.getX(), (int) start.getY());
            final Point bottomRight = new Point((int) end.getMaxX(), (int) end.getMaxY());
            SwingUtilities.convertPointToScreen(topLeft, editor);
            SwingUtilities.convertPointToScreen(bottomRight, editor);
            var pointAt = new Rectangle(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);

            final CalloutWindow currentSegmentCallout = editor.showNonFocusableCallout(false, segmentEditor, SwingConstants.NORTH, pointAt);
            currentSegmentCallout.setAlwaysOnTop(true);
            currentSegmentCallout.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if(calloutRef.get().callout() == currentSegmentCallout)
                        calloutRef.set(null);
                    currentSegmentCallout.removeWindowListener(this);
                }
            });
            calloutRef.set(new SegmentEditorCalloutInfo(currentSegmentCallout, segmentCalloutRequestInfo, segmentEditor));
        } catch (BadLocationException e) {
            LogUtil.warning(e);
        }
    }

    @NotNull
    private SegmentEditorPopup getSegmentEditorPopup(SegmentCalloutRequestInfo segmentCalloutRequestInfo) {
        final Tier<MediaSegment> segmentTier = segmentCalloutRequestInfo.segmentTier();
        final Record record = segmentCalloutRequestInfo.record();
        var segmentEditor = new SegmentEditorPopup(editor.getMediaModel(), segmentTier.getValue());
        segmentEditor.setPreferredSize(new Dimension(segmentEditor.getPreferredPopupWidth(), (int) segmentEditor.getPreferredSize().getHeight()));

        segmentEditor.addPropertyChangeListener(e -> {
            if ("segment".equals(e.getPropertyName()) && e.getNewValue() != null) {
                final TierEdit<MediaSegment> tierEdit = new TierEdit<MediaSegment>(editor.getSession(),
                        editor.getEventManager(), record, segmentTier, (MediaSegment) e.getNewValue());
                tierEdit.setSource(segmentEditor);
                tierEdit.setValueAdjusting(segmentEditor.valueIsAdjusting());
                editor.getUndoSupport().postEdit(tierEdit);
            } else if ("valueAdjusting".equals(e.getPropertyName())) {
                if(segmentEditor.valueIsAdjusting())
                    editor.getUndoSupport().beginUpdate("Edit record segment");
                else {
                    // indicate final change on last edit to update other views
                    final TierEdit<MediaSegment> tierEdit = new TierEdit<MediaSegment>(editor.getSession(),
                            editor.getEventManager(), record, segmentTier, segmentTier.getValue());
                    tierEdit.setSource(segmentEditor);
                    tierEdit.setValueAdjusting(segmentEditor.valueIsAdjusting());
                    editor.getUndoSupport().postEdit(tierEdit);

                    editor.getUndoSupport().endUpdate();
                }
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
    private record SegmentCalloutRequestInfo(Record record, Tier<MediaSegment> segmentTier) {
    }

    /**
     * Information about a currently visible segment editor callout
     */
    private record SegmentEditorCalloutInfo(CalloutWindow callout, SegmentCalloutRequestInfo requestInfo, SegmentEditorPopup editor) {
    }

    private class MediaSegmentInsertionHook extends DefaultInsertionHook {
        @Override
        public List<DefaultStyledDocument.ElementSpec> batchInsertString(StringBuilder buffer, MutableAttributeSet attrs) {
            MediaSegment segment = (MediaSegment) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_MEDIA_SEGMENT);
            if (segment != null) {
                Record record = (Record) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_RECORD);
                Tier<MediaSegment> segmentTier = (Tier<MediaSegment>) attrs.getAttribute(TranscriptStyleConstants.ATTR_KEY_TIER);
                if (record != null && segmentTier != null) {
                    PhonUIAction<SegmentCalloutRequestInfo> showSegmentEditCalloutAct = PhonUIAction.consumer(MediaSegmentExtension.this::showSegmentEditCallout,
                            new SegmentCalloutRequestInfo(record, segmentTier));
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
            final TranscriptElementLocation location = doc.charPosToSessionLocation(offset);
            final TranscriptElementLocation caretLocation = editor.getTranscriptEditorCaret().getTranscriptLocation();
            int direction = location.charPosition() - caretLocation.charPosition();
            final AttributeSet attrs = doc.getCharacterElement(offset).getAttributes();
            final Record record = TranscriptStyleConstants.getRecord(attrs);
            final Tier<?> tier = TranscriptStyleConstants.getTier(attrs);
            final MediaSegment mediaSegment = TranscriptStyleConstants.getMediaSegment(attrs);
            final TranscriptDocument.StartEnd startEnd = doc.getSegmentBounds(mediaSegment, offset);
            if(startEnd.valid()) {
                final String currentText = doc.getText(startEnd.start(), startEnd.length());
                // attempt to replace all digits with zeros
                final StringBuilder sb = new StringBuilder();
                for(int i = 0; i < currentText.length(); i++) {
                    if(i >= location.charPosition() && i < (location.charPosition() + length)) {
                        if(Character.isDigit(currentText.charAt(i))) {
                            sb.append('0');
                        } else {
                            sb.append(currentText.charAt(i));
                        }
                    } else {
                        sb.append(currentText.charAt(i));
                    }
                }
                final String replacedText = sb.toString();

                final MediaSegmentFormatter segmentFormatter = new MediaSegmentFormatter(MediaTimeFormatStyle.PADDED_MINUTES_AND_SECONDS);
                MediaSegment newSegment = SessionFactory.newFactory().createMediaSegment();
                try {
                    newSegment = segmentFormatter.parse(replacedText.substring(1, replacedText.length() - 1));
                } catch (ParseException e) {
                    mediaSegment.putExtension(UnvalidatedValue.class, new UnvalidatedValue(replacedText, e));
                }
                // update segment
                if(SystemTierType.Segment.getName().equals(tier.getName())) {
                    final TierEdit<MediaSegment> tierEdit = new TierEdit<>(editor.getSession(),
                            editor.getEventManager(), record, (Tier<MediaSegment>) tier, newSegment);
                    tierEdit.setValueAdjusting(false);
                    editor.getUndoSupport().postEdit(tierEdit);

                    final var newLocation =
                            new TranscriptElementLocation(location.transcriptElementIndex(), location.tier(), caretLocation.charPosition() + direction);
                    if (newLocation.valid()) {
                        SwingUtilities.invokeLater(() -> {
                            final int caretPos = doc.sessionLocationToCharPos(newLocation);
                            if (caretPos >= 0) {
                                editor.setCaretPosition(caretPos);
                            }
                        });
                    }
                }
            }
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

                    final Matcher matcher = mediaSegmentPattern.matcher(replacedText);
                    MediaSegment newSegment = SessionFactory.newFactory().createMediaSegment();
                    if (matcher.matches()) {
                        try {
                            final MediaSegmentFormatter segmentFormatter = new MediaSegmentFormatter(MediaTimeFormatStyle.PADDED_MINUTES_AND_SECONDS);
                            newSegment = segmentFormatter.parse(matcher.group(1));

                            if(newSegment.getStartTime() > newSegment.getEndTime()) {
                                final UnvalidatedValue uv = new UnvalidatedValue(InternalMedia.MEDIA_BULLET +
                                        segmentFormatter.format(newSegment) + InternalMedia.MEDIA_BULLET,
                                        new ParseException("Segment start time > end time", 0));
                                newSegment = SessionFactory.newFactory().createMediaSegment();
                                newSegment.putExtension(UnvalidatedValue.class, uv);
                            }
                        } catch (ParseException e) {
                            mediaSegment.putExtension(UnvalidatedValue.class, new UnvalidatedValue(replacedText, e));
                        }
                        // update segment
                        if(SystemTierType.Segment.getName().equals(tier.getName())) {
                            final TierEdit<MediaSegment> tierEdit = new TierEdit<>(editor.getSession(),
                                    editor.getEventManager(), record, (Tier<MediaSegment>) tier, newSegment);
                            tierEdit.setValueAdjusting(false);
                            editor.getUndoSupport().postEdit(tierEdit);
                        }
                        int nextChar = location.charPosition() + text.length();
                        while(nextChar < replacedText.length() -2 && !Character.isDigit(replacedText.charAt(nextChar))) {
                            nextChar++;
                        }
                        final var newLocation =
                                new TranscriptElementLocation(location.transcriptElementIndex(), location.tier(), nextChar);
                        if (newLocation.valid()) {
                            SwingUtilities.invokeLater(() -> {
                                final int caretPos = doc.sessionLocationToCharPos(newLocation);
                                if (caretPos >= 0) {
                                    editor.setCaretPosition(caretPos);
                                }
                            });
                        }
                    }
                }
            }
        }
    }

}
