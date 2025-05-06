package ca.phon.app.session.editor.view.transcript;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.SegmentPlayback;
import ca.phon.app.session.editor.SessionMediaModel;
import ca.phon.app.session.editor.view.speechAnalysis.SpeechAnalysisViewColors;
import ca.phon.media.TimeComponent;
import ca.phon.media.TimeUIModel;
import ca.phon.media.Timebar;
import ca.phon.media.WaveformDisplay;
import ca.phon.session.MediaSegment;
import ca.phon.session.SessionFactory;
import ca.phon.ui.FlatButton;
import ca.phon.ui.HasIconStrip;
import ca.phon.ui.IconStrip;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.OSInfo;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.io.IOException;

/**
 * UI for editing media segments within a callout window in the transcript editor.
 */
public class SegmentEditorPopup extends TimeComponent implements HasIconStrip {

    private final static int DEFAULT_POPUP_WIDTH = 400;
    private final static float CLIP_EXTENSION_MIN = 0.5f;
    private final static float CLIP_EXTENSION_MAX = 1.0f;
    private final SessionMediaModel mediaModel;
    private final WaveformDisplay waveformDisplay;
    private FlatButton playSegmentButton;
    private JScrollPane waveformScroller;
    private MediaSegment segment;
    private TimeUIModel.Interval currentRecordInterval;
    private TimeUIModel.Marker segmentPlaybackMarker;
    private int preferredPopupWidth = DEFAULT_POPUP_WIDTH;
    private Rectangle scrollToRect;
    private boolean insideSetMediaSegment = false;
    private IconStrip iconStrip;

    public SegmentEditorPopup(SessionMediaModel mediaModel, MediaSegment segment) {
        super();
        this.mediaModel = mediaModel;
        this.segment = SessionFactory.newFactory().createMediaSegment();
        this.segment.setStartValue(segment.getStartValue());
        this.segment.setEndValue(segment.getEndValue());
        this.segment.setUnitType(segment.getUnitType());
        this.waveformDisplay = new WaveformDisplay(getTimeModel());
        init();
    }

    public void init() {
        setLayout(new BorderLayout());

        final TimeUIModel timeUIModel = getTimeModel();
        if (mediaModel.isSessionAudioAvailable()) {
            try {
                waveformDisplay.setLongSound(mediaModel.getSharedSessionAudio());
                waveformDisplay.setPreferredChannelHeight(50);
                waveformDisplay.setTrackViewportHeight(true);
                waveformDisplay.setFocusable(true);
                waveformScroller = new JScrollPane(waveformDisplay);
                waveformScroller.setColumnHeaderView(new Timebar(timeUIModel));
                add(waveformScroller, BorderLayout.CENTER);

                final PhonUIAction<Void> playSegmentAct = PhonUIAction.runnable(() -> {
                    if (mediaModel.getSegmentPlayback().isPlaying())
                        mediaModel.getSegmentPlayback().stopPlaying();
                    else
                        mediaModel.getSegmentPlayback().playSegment(segment);
                });
                playSegmentAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Play segment");
                playSegmentAct.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
                playSegmentAct.putValue(FlatButton.ICON_NAME_PROP, "play_arrow");
                playSegmentAct.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
                playSegmentButton = new FlatButton(playSegmentAct);
                playSegmentButton.setBgColor(UIManager.getColor("text"));

                // add play segment keystrokes (F2 or menu shortcut+space)
                final JLabel playSegmentLabel = new JLabel("F2 / " + "CTRL+SPACE");
                playSegmentLabel.setFont(UIManager.getFont("Label.font").deriveFont(10.0f));
                playSegmentLabel.setForeground(UIManager.getColor("textText"));

                mediaModel.getSegmentPlayback().addPropertyChangeListener(SegmentPlayback.PLAYBACK_PROP, e -> {
                    if (mediaModel.getSegmentPlayback().isPlaying()) {
                        playSegmentButton.setIconName("stop");
                    } else {
                        playSegmentButton.setIconName("play_arrow");
                    }
                });
                mediaModel.getSegmentPlayback().addPropertyChangeListener(this::onSegmentPlaybackChange);

                iconStrip = new IconStrip(SwingUtilities.HORIZONTAL);
                iconStrip.setBackground(UIManager.getColor("text"));
                iconStrip.add(playSegmentButton, IconStrip.IconStripPosition.LEFT);
                iconStrip.add(playSegmentLabel, IconStrip.IconStripPosition.LEFT);
                add(iconStrip, BorderLayout.NORTH);
            } catch (IOException e) {
                Toolkit.getDefaultToolkit().beep();
                LogUtil.severe(e);
                add(new JLabel(e.getLocalizedMessage()), BorderLayout.CENTER);
            }
        } else {
            add(new JLabel("No session audio available"), BorderLayout.CENTER);
        }
        setupTimeModel();
    }

    public IconStrip getIconStrip() {
        return iconStrip;
    }

    private void setupTimeModel() {
        float startTime = 0.0f;
        float pxPerS = 100.0f;
        float endTime = 0.0f;
        float scrollTo = 0.0f;

        if (currentRecordInterval != null)
            getTimeModel().removeInterval(currentRecordInterval);

        float segStart = segment.getStartTime();
        float segEnd = segment.getEndTime();
        float segLength = segment.getLength();

        float preferredClipExtension = segLength * 0.4f;
        if (preferredClipExtension < CLIP_EXTENSION_MIN)
            preferredClipExtension = CLIP_EXTENSION_MIN;
        if (preferredClipExtension > CLIP_EXTENSION_MAX)
            preferredClipExtension = CLIP_EXTENSION_MAX;

        float clipStart = segStart - preferredClipExtension;
        float displayStart = Math.max(0.0f, clipStart);
        float displayLength = segLength + (2 * preferredClipExtension);

        if (waveformDisplay.getLongSound() != null) {
            if ((displayStart + displayLength) > waveformDisplay.getLongSound().length()) {
                displayStart = waveformDisplay.getLongSound().length() - displayLength;

                if (displayStart < 0.0f) {
                    displayStart = 0.0f;
                    displayLength = waveformDisplay.getLongSound().length();
                }
            }
        }

        int displayWidth = getPreferredPopupWidth();
        if (displayWidth > 0)
            pxPerS = displayWidth / displayLength;

        currentRecordInterval = getTimeModel().addInterval(segStart, segEnd);
        currentRecordInterval.setColor(UIManager.getColor(SpeechAnalysisViewColors.INTERVAL_BACKGROUND));
        currentRecordInterval.getStartMarker().setColor(UIManager.getColor(SpeechAnalysisViewColors.INTERVAL_MARKER_COLOR));
        currentRecordInterval.getEndMarker().setColor(UIManager.getColor(SpeechAnalysisViewColors.INTERVAL_MARKER_COLOR));
        currentRecordInterval.setRepaintEntireInterval(true);
        currentRecordInterval.addPropertyChangeListener(e -> {
            if (insideSetMediaSegment) return;
            float startVal = currentRecordInterval.getStartMarker().getTime();
            float endVal = currentRecordInterval.getEndMarker().getTime();
            adjustMediaSegment(startVal, endVal);
            if ("valueAdjusting".equals(e.getPropertyName())) {
                firePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
            }
        });
        scrollTo = displayStart;

        if (waveformDisplay.getLongSound() != null) {
            endTime = waveformDisplay.getLongSound().length();
        }

        getTimeModel().setStartTime(startTime);
        getTimeModel().setEndTime(endTime);
        getTimeModel().setPixelsPerSecond(pxPerS);

        final float scrollToTime = scrollTo;
        final double xPos = getTimeModel().xForTime(scrollToTime);
        final Rectangle scrollRect = new Rectangle((int) xPos, 0, displayWidth, 10);
        scrollToRect = scrollRect;
        SwingUtilities.invokeLater(() -> {
            waveformDisplay.scrollRectToVisible(scrollRect);
        });
    }

    public int getPreferredPopupWidth() {
        return preferredPopupWidth;
    }

    public void setPreferredPopupWidth(int preferredPopupWidth) {
        var oldVal = this.preferredPopupWidth;
        this.preferredPopupWidth = preferredPopupWidth;
        firePropertyChange("preferredPopupWidth", oldVal, preferredPopupWidth);
    }

    /**
     * Listener for segment playback, added to the session media model's segment playback object
     *
     * @param evt the property change event
     */
    private void onSegmentPlaybackChange(PropertyChangeEvent evt) {
        final SegmentPlayback segmentPlayback = (SegmentPlayback)evt.getSource();
        final TimeUIModel timeModel = getTimeModel();
        if(SegmentPlayback.PLAYBACK_PROP.contentEquals(evt.getPropertyName())) {
            if(segmentPlayback.isPlaying()) {
                segmentPlaybackMarker = timeModel.addMarker(segmentPlayback.getTime(), UIManager.getColor(SpeechAnalysisViewColors.PLAYBACK_MARKER_COLOR));
                segmentPlaybackMarker.setOwner(this.waveformDisplay);
                segmentPlaybackMarker.setDraggable(false);
            } else {
                if(segmentPlaybackMarker != null)
                    timeModel.removeMarker(segmentPlaybackMarker);
                segmentPlaybackMarker = null;
            }
        } else if(SegmentPlayback.TIME_PROP.contentEquals(evt.getPropertyName())) {
            if(segmentPlaybackMarker != null) {
                segmentPlaybackMarker.setTime((float)evt.getNewValue());
            }
        }
    }

    /**
     * Internal adjustment of media segment values
     *
     * @param startTime
     * @param endTime
     */
    private void adjustMediaSegment(float startTime, float endTime) {
        var oldVal = this.segment;
        this.segment = SessionFactory.newFactory().createMediaSegment();
        this.segment.setUnitType(oldVal.getUnitType());
        this.segment.setStartTime(startTime);
        this.segment.setEndTime(endTime);
        firePropertyChange("segment", oldVal, this.segment);
    }

    /**
     * Set media segment, this method will not fire segment changes to listeners
     *
     * @param startTime
     * @param endTime
     */
    public void setMediaSegment(float startTime, float endTime) {
        var oldVal = this.segment;
        this.segment = SessionFactory.newFactory().createMediaSegment();
        this.segment.setUnitType(oldVal.getUnitType());
        this.segment.setStartTime(startTime);
        this.segment.setEndTime(endTime);
        insideSetMediaSegment = true;
        currentRecordInterval.getStartMarker().setTime(startTime);
        currentRecordInterval.getEndMarker().setTime(endTime);
        insideSetMediaSegment = false;
    }

    /**
     * Are we in the middle of adjusting the media segment
     *
     * @return
     */
    public boolean valueIsAdjusting() {
        return currentRecordInterval != null ? currentRecordInterval.isValueAdjusting() : false;
    }

}
