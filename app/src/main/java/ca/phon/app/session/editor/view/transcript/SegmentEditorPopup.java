package ca.phon.app.session.editor.view.transcript;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.SessionMediaModel;
import ca.phon.app.session.editor.view.common.SegmentField;
import ca.phon.app.session.editor.view.speechAnalysis.SpeechAnalysisViewColors;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.media.*;
import ca.phon.session.MediaSegment;
import ca.phon.session.MediaUnit;
import ca.phon.session.SessionFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * UI for editing media segments
 */
public class SegmentEditorPopup extends TimeComponent {

    private final SessionMediaModel mediaModel;

    private final SegmentField segmentField;

    private JScrollPane waveformScroller;

    private final WaveformDisplay waveformDisplay;

    private MediaSegment segment;

    private TimeUIModel.Interval currentRecordInterval;

    private final static int DEFAULT_POPUP_WIDTH = 400;

    private int preferredPopupWidth = DEFAULT_POPUP_WIDTH;

    private Rectangle scrollToRect;

    private final static String DEFAULT_SEGMENT_TEXT = "000:00.000-000:00.000";

    public SegmentEditorPopup(SessionMediaModel mediaModel, MediaSegment segment) {
        super();
        this.mediaModel = mediaModel;
        this.segment = SessionFactory.newFactory().createMediaSegment();
        this.segment.setStartValue(segment.getStartValue());
        this.segment.setEndValue(segment.getEndValue());
        this.segment.setUnitType(segment.getUnitType());
        this.waveformDisplay = new WaveformDisplay(getTimeModel());
        this.segmentField = new SegmentField();
        init();
    }

    public int getPreferredPopupWidth() {
        return preferredPopupWidth;
    }

    public void setPreferredPopupWidth(int preferredPopupWidth) {
        var oldVal = this.preferredPopupWidth;
        this.preferredPopupWidth = preferredPopupWidth;
        firePropertyChange("preferredPopupWidth", oldVal, preferredPopupWidth);
    }

    public void updateText() {
        final MediaSegment segment = this.segment;
        final Formatter<MediaSegment> segmentFormatter = FormatterFactory.createFormatter(MediaSegment.class);

        String tierTxt =
                (segmentFormatter != null ? segmentFormatter.format(segment) : DEFAULT_SEGMENT_TEXT);
        segmentField.setText(tierTxt);
    }

    public void init() {
        setLayout(new BorderLayout());

        updateText();
        addPropertyChangeListener("segment", e -> updateText());

        final TimeUIModel timeUIModel = getTimeModel();
        if(mediaModel.isSessionAudioAvailable()) {
            try {
                waveformDisplay.setLongSound(mediaModel.getSharedSessionAudio());
                waveformDisplay.setPreferredChannelHeight(50);
                waveformDisplay.setTrackViewportHeight(true);
                waveformDisplay.setFocusable(true);
                waveformScroller = new JScrollPane(waveformDisplay);
                waveformScroller.setColumnHeaderView(new Timebar(timeUIModel));
                add(waveformScroller, BorderLayout.CENTER);
                add(segmentField, BorderLayout.SOUTH);
                setupTimeModel();
            } catch (IOException e) {
                Toolkit.getDefaultToolkit().beep();
                LogUtil.severe(e);
                add(new JLabel(e.getLocalizedMessage()), BorderLayout.CENTER);
            }
        } else {
            add(new JLabel("No session audio available"), BorderLayout.CENTER);
        }
    }

    private final static float CLIP_EXTENSION_MIN = 0.5f;

    private final static float CLIP_EXTENSION_MAX = 1.0f;
    private void setupTimeModel() {
        float startTime = 0.0f;
        float pxPerS = 100.0f;
        float endTime = 0.0f;
        float scrollTo = 0.0f;

        if(currentRecordInterval != null)
            getTimeModel().removeInterval(currentRecordInterval);
//        if(selectionInterval != null)
//            clearSelection();

        float segStart = segment.getUnitType() == MediaUnit.Millisecond ? segment.getStartValue() / 1000.0f : segment.getStartValue();
        float segEnd = segment.getUnitType() == MediaUnit.Millisecond ? segment.getEndValue() / 1000.0f : segment.getEndValue();
        float segLength = segEnd - segStart;

        float preferredClipExtension = segLength * 0.4f;
        if(preferredClipExtension < CLIP_EXTENSION_MIN)
            preferredClipExtension = CLIP_EXTENSION_MIN;
        if(preferredClipExtension > CLIP_EXTENSION_MAX)
            preferredClipExtension = CLIP_EXTENSION_MAX;

        float clipStart = segStart - preferredClipExtension;
        float displayStart = Math.max(0.0f, clipStart);
        float displayLength = segLength + (2*preferredClipExtension);

        if(waveformDisplay.getLongSound() != null) {
            if((displayStart + displayLength) > waveformDisplay.getLongSound().length()) {
                displayStart = waveformDisplay.getLongSound().length() - displayLength;

                if(displayStart < 0.0f) {
                    displayStart = 0.0f;
                    displayLength = waveformDisplay.getLongSound().length();
                }
            }
        }

        int displayWidth = getPreferredPopupWidth();
        if(displayWidth > 0)
            pxPerS = displayWidth / displayLength;

        currentRecordInterval = getTimeModel().addInterval(segStart, segEnd);
        currentRecordInterval.setColor(UIManager.getColor(SpeechAnalysisViewColors.INTERVAL_BACKGROUND));
        currentRecordInterval.getStartMarker().setColor(UIManager.getColor(SpeechAnalysisViewColors.INTERVAL_MARKER_COLOR));
        currentRecordInterval.getEndMarker().setColor(UIManager.getColor(SpeechAnalysisViewColors.INTERVAL_MARKER_COLOR));
        currentRecordInterval.setRepaintEntireInterval(true);
        currentRecordInterval.addPropertyChangeListener(e -> {
            float startVal = this.segment.getUnitType() == MediaUnit.Millisecond
                    ? currentRecordInterval.getStartMarker().getTime() * 1000.0f
                    : currentRecordInterval.getStartMarker().getTime();
            float endVal = this.segment.getUnitType() == MediaUnit.Millisecond
                    ? currentRecordInterval.getEndMarker().getTime() * 1000.0f
                    : currentRecordInterval.getEndMarker().getTime();
            setMediaSegment(startVal, endVal);
        });
        scrollTo = displayStart;

        if(waveformDisplay.getLongSound() != null) {
            endTime = waveformDisplay.getLongSound().length();
        }

        getTimeModel().setStartTime(startTime);
        getTimeModel().setEndTime(endTime);
        getTimeModel().setPixelsPerSecond(pxPerS);

        final float scrollToTime = scrollTo;
        final double xPos = getTimeModel().xForTime(scrollToTime);
        final Rectangle scrollRect = new Rectangle((int)xPos, 0, displayWidth, 10);
        scrollToRect = scrollRect;
        SwingUtilities.invokeLater(() -> {waveformDisplay.scrollRectToVisible(scrollRect);});
    }

    private void setMediaSegment(float startTime, float endTime) {
        var oldVal = this.segment;
        this.segment = SessionFactory.newFactory().createMediaSegment();
        this.segment.setUnitType(oldVal.getUnitType());
        this.segment.setStartValue(startTime);
        this.segment.setEndValue(endTime);
        firePropertyChange("segment", oldVal, this.segment);
    }

}
