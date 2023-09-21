package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.SessionMediaModel;
import ca.phon.app.session.editor.view.common.SegmentField;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.media.TimeComponent;
import ca.phon.media.TimeUIModel;
import ca.phon.media.Timebar;
import ca.phon.media.WaveformDisplay;
import ca.phon.session.MediaSegment;
import ca.phon.ui.action.PhonUIAction;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * UI for editing media segments
 */
public class SegmentEditorPopup extends TimeComponent {

    private final SessionMediaModel mediaModel;

    private final SegmentField segmentField;

    private final WaveformDisplay waveformDisplay;

    private final MediaSegment segment;

    private final static int DEFAULT_POPUP_WIDTH = 400;
    private int preferredPopupWidth = DEFAULT_POPUP_WIDTH;

    private final static String DEFAULT_SEGMENT_TEXT = "000:00.000-000:00.000";

    public SegmentEditorPopup(SessionMediaModel mediaModel, MediaSegment segment) {
        super();
        this.mediaModel = mediaModel;
        this.segment = segment;
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

        final TimeUIModel timeUIModel = getTimeModel();
        if(mediaModel.isSessionAudioAvailable()) {
            try {
                timeUIModel.setPixelsPerSecond(100.0f);
                timeUIModel.setStartTime(0.0f);
                timeUIModel.setEndTime(mediaModel.getSharedSessionAudio().length());
                waveformDisplay.setLongSound(mediaModel.getSharedSessionAudio());
                waveformDisplay.setPreferredChannelHeight(50);
                waveformDisplay.setTrackViewportHeight(true);
                waveformDisplay.setFocusable(true);
                TimeUIModel.Interval segmentInterval = timeUIModel.addInterval(segment.getStartValue()/1000.0f, segment.getEndValue()/1000.0f);
                segmentInterval.addPropertyChangeListener(e -> {
                    this.segment.setStartValue(segmentInterval.getStartMarker().getTime() * 1000.0f);
                    this.segment.setEndValue(segmentInterval.getEndMarker().getTime() * 1000.0f);
                    updateText();
                });
                final JScrollPane scrollPane = new JScrollPane(waveformDisplay);
                scrollPane.setColumnHeaderView(new Timebar(timeUIModel));
                add(scrollPane, BorderLayout.CENTER);
                add(this.segmentField, BorderLayout.SOUTH);
            } catch (IOException e) {
                Toolkit.getDefaultToolkit().beep();
                LogUtil.severe(e);
                add(new JLabel(e.getLocalizedMessage()), BorderLayout.CENTER);
            }
        } else {
            add(new JLabel("No session audio available"), BorderLayout.CENTER);
        }
    }


    public JFrame showPopup(JComponent component, int x, int y) {
        final JFrame retVal = new JFrame();
        retVal.setUndecorated(true);
        final Point p = new Point(x, y);
        SwingUtilities.convertPointToScreen(p, component);
        retVal.getContentPane().setLayout(new BorderLayout());
        retVal.getContentPane().add(this, BorderLayout.CENTER);
        retVal.pack();
        retVal.setSize(getPreferredPopupWidth(), retVal.getPreferredSize().height);
        retVal.setLocation(p);
        retVal.setVisible(true);
        retVal.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                retVal.setVisible(false);
                retVal.dispose();
            }
        });
        final PhonUIAction<Void> closeAct = PhonUIAction.runnable(() -> retVal.setVisible(false));
        final KeyStroke escKs = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

        ((JComponent)retVal.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKs, "close_popup");
        ((JComponent)retVal.getContentPane()).getActionMap().put("close_popup", closeAct);

        return retVal;
    }

}
