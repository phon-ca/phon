/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.phon.app.session.editor.view.mediaPlayer;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.EditorViewAdapter;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.actions.AssignMediaAction;
import ca.phon.app.session.editor.undo.MediaLocationEdit;
import ca.phon.app.session.editor.view.mediaPlayer.actions.GoToAction;
import ca.phon.app.session.editor.view.mediaPlayer.actions.GoToEndOfSegmentedAction;
import ca.phon.app.session.editor.view.mediaPlayer.actions.TakeSnapshotAction;
import ca.phon.app.session.editor.view.mediaPlayer.actions.ToggleAdjustVideoAction;
import ca.phon.app.session.editor.view.mediaPlayer.undo.VideoPositionAndSizeEdit;
import ca.phon.app.session.editor.view.mediaPlayer.undo.VideoVisibleEdit;
import ca.phon.app.session.editor.view.transcript.TranscriptView;
import ca.phon.formatter.MsFormatter;
import ca.phon.media.MediaLocator;
import ca.phon.media.VLCHelper;
import ca.phon.media.player.IMediaMenuFilter;
import ca.phon.media.player.PhonMediaPlayer;
import ca.phon.media.player.PhonPlayerComponent;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.dnd.FileTransferHandler;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import org.jdesktop.swingx.VerticalLayout;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

/**
 * Panel for media player in the session editor.  The media player may be embedded
 * or external.  If embedded, the media player canvas is placed in the glass pane
 * of the session editor window.  If external, the media player canvas is placed
 * in an accessory window of the session editor window.
 */
public class MediaPlayerEditorView extends EditorView {

    public static final String VIEW_NAME = "Media Player";

    public static final String VIEW_ICON = IconManager.GoogleMaterialDesignIconsFontName + ":ONDEMAND_VIDEO";

    /**
     * Custom editor event signaled when media loaded
     * Event data: media file (as string)
     */
    public static final EditorEventType<MediaPlayerEditorView> MediaLoaded = new EditorEventType<>("_media_loaded_", MediaPlayerEditorView.class);

    public static final EditorEventType<MediaPlayerEditorView> MediaUnloaded = new EditorEventType<>("_media_unloaded_", MediaPlayerEditorView.class);
    private final static int DEFAULT_MEDIA_CANVAS_WIDTH = 320;
    private final static int DEFAULT_MEDIA_CANVAS_HEIGHT = 240;
    private final static String ADJUST_VIDEO = MediaPlayerEditorView.class.getName() + ".adjustVideo";
    /**
     * Media player for this view
     */
    private PhonMediaPlayer mediaPlayer;
    private final EditorViewAdapter editorViewListener = new EditorViewAdapter() {

        @Override
        public void onClosed(EditorView view) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }

    };

    /**
     * Panel for error message when media is not available
     */
    private JPanel errorPanel;
    private ErrorBanner messageButton = new ErrorBanner();

    /**
     * Manual location setting of the media player canavs when embedded.
     */
    private int mediaCanvasX = -1;
    private int mediaCanvasY = -1;

    /**
     * Manual width of the media player canvas when embedded.
     */
    private int mediaCanvasWidth = -1;

    /**
     * Manual height of the media player canvas when embedded.
     */
    private int mediaCanvasHeight = -1;

    /**
     * Is the player embedded or external?
     */
    private boolean embedded = true;

    private boolean loadVideoOnShow = false;

    // popup frame for time selection
    private JFrame timeSelectionPopup = null;

    public MediaPlayerEditorView(SessionEditor editor) {
        super(editor);

        init();
        addEditorViewListener(editorViewListener);
        editor.getMediaModel().addPropertyChangeListener((evt) -> {
            switch (evt.getPropertyName()) {
                case "playbackRate":
                    if (mediaPlayer != null) {
                        mediaPlayer.setRate(editor.getMediaModel().getPlaybackRate());
                    }
                    break;

                default:
                    break;
            }
        });
        editor.getViewModel().addEditorViewModelListener(new EditorViewModelListener() {
            @Override
            public void viewShown(String viewName) {
                if(MediaPlayerEditorView.VIEW_NAME.equals(viewName) && loadVideoOnShow) {
                    if(embedded) {
                        SwingUtilities.invokeLater(MediaPlayerEditorView.this::showVideoInWindowGlassPane);
                    } else {
                        mediaPlayer.setVideoVisible(true);
                        mediaPlayer.revalidate();
                    }
                    loadVideoOnShow = false;
                }
            }

            @Override
            public void viewHidden(String viewName) {
                if (viewName.equals(MediaPlayerEditorView.VIEW_NAME)) {
                    if (mediaPlayer != null) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                        }
                        mediaPlayer.setVideoVisible(false);
                        final JComponent glassPane = (JComponent) getEditor().getRootPane().getGlassPane();
                        if (SwingUtilities.isDescendingFrom(mediaPlayer.getMediaPlayerCanvas(), glassPane)) {
                            glassPane.remove(mediaPlayer.getMediaPlayerCanvas());
                        }
                    }
                }
            }

            @Override
            public void viewMinimized(String viewName) {

            }

            @Override
            public void viewMaximized(String viewName) {

            }

            @Override
            public void viewNormalized(String viewName) {

            }

            @Override
            public void viewExternalized(String viewName) {

            }

            @Override
            public void viewFocused(String viewName) {

            }
        });
    }

    private void init() {
        setLayout(new BorderLayout());
        mediaPlayer = new PhonMediaPlayer(getEditor().getMediaModel().getVolumeModel());
        mediaPlayer.addMediaMenuFilter(new MediaMenuFilter());
        mediaPlayer.addPropertyChangeListener("mediaLoaded", (e) -> {
            if ((boolean) e.getNewValue()) {
                EditorEvent<MediaPlayerEditorView> ee = new EditorEvent<>(MediaLoaded, this, this);
                getEditor().getEventManager().queueEvent(ee);
            } else {
                EditorEvent<MediaPlayerEditorView> ee = new EditorEvent(MediaUnloaded, this, this);
                getEditor().getEventManager().queueEvent(ee);
            }
        });
        final PhonPlayerComponent mediaPlayerCanvas = mediaPlayer.getMediaPlayerCanvas();
        mediaPlayerCanvas.setTransferHandler(new FileSelectionTransferHandler());
        mediaPlayerCanvas.setOverlayPainter(this::paintMediaCanvasOverlay);
        mediaPlayerCanvas.addMouseListener(new MediaPlayerCanvasOverlayListener());
        mediaPlayerCanvas.addMouseListener(mediaPlayerCanvasMouseAdapter);
        mediaPlayerCanvas.addMouseMotionListener(mediaPlayerCanvasMouseAdapter);
        mediaPlayer.setVideoVisible(false);

        add(mediaPlayer, BorderLayout.CENTER);

        final AssignMediaAction browseForMediaAct = new AssignMediaAction(getEditor());
        browseForMediaAct.putValue(AssignMediaAction.LARGE_ICON_KEY, browseForMediaAct.getValue(AssignMediaAction.SMALL_ICON));

        messageButton.setDefaultAction(browseForMediaAct);
        messageButton.addAction(browseForMediaAct);

        messageButton.setTopLabelText("<html><b>Session media not available</b></html>");
        messageButton.setBottomLabelText("<html>Click here to assign media file to session.</html>");
        messageButton.setVisible(!getEditor().getMediaModel().isSessionMediaAvailable());

        errorPanel = new JPanel(new VerticalLayout());
        errorPanel.add(messageButton);

        add(errorPanel, BorderLayout.SOUTH);

        setupEditorActions();

        // load media if available
        final String mediaFilePath = getMediaFilePath();
        if (mediaFilePath != null)
            SwingUtilities.invokeLater(this::loadMedia);
    }

    private void setupEditorActions() {
        getEditor().getEventManager().registerActionForEvent(EditorEventType.SessionMediaChanged, this::onMediaChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
        getEditor().getEventManager().registerActionForEvent(EditorEventType.SessionChanged, this::onSessionChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
        getEditor().getEventManager().registerActionForEvent(EditorEventType.RecordChanged, this::onRecordChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
        getEditor().getEventManager().registerActionForEvent(EditorEventType.SegmentPlayback, this::doSegmentPlayback, EditorEventManager.RunOn.AWTEventDispatchThread);
        getEditor().getEventManager().registerActionForEvent(EditorEventType.EditorClosing, this::doCleanup, EditorEventManager.RunOn.AWTEventDispatchThread);
    }

    /**
     * Return the media file path or null if not found
     */
    private String getMediaFilePath() {
        final Session t = getEditor().getSession();
        File mediaFile =
                MediaLocator.findMediaFile(getEditor().getProject(), t);
        if (mediaFile != null) {
            return mediaFile.getAbsolutePath();
        }
        return null;
    }

    private void loadMedia() {
        final SessionMediaModel mediaModel = getEditor().getMediaModel();
        if (mediaModel.isSessionMediaAvailable()) {
            // first check to make sure VLC was found, if not issue a message
            // and return
            if (!VLCHelper.checkNativeLibrary(true)) return;

            final File mediaFile = mediaModel.getSessionMediaFile();
            mediaPlayer.loadMedia(mediaFile.getAbsolutePath());
            mediaPlayer.setRate(getEditor().getMediaModel().getPlaybackRate());

            messageButton.setVisible(false);
        } else {
            mediaPlayer.setMediaFile(null);
            messageButton.setVisible(true);
        }
    }

    private boolean paintOverlay = false;

    private void paintMediaCanvasOverlay(Graphics2D g2d) {
        if(!paintOverlay) return;

        final Color iconColor = new Color(255, 255, 255, 170);
        final ImageIcon playIcn = IconManager.getInstance().getFontIcon(
                IconManager.GoogleMaterialDesignIconsFontName, "play_arrow", IconSize.XXLARGE,
                iconColor);
        final ImageIcon pauseIcn = IconManager.getInstance().getFontIcon(
                IconManager.GoogleMaterialDesignIconsFontName, "pause", IconSize.XXLARGE,
                iconColor);

        final ImageIcon closeIcn = IconManager.getInstance().getFontIcon(
                IconManager.GoogleMaterialDesignIconsFontName, "close", IconSize.MEDIUM,
                iconColor);

        final MediaPlayer player = mediaPlayer.getMediaPlayer();
        final JComponent mediaPlayerCanvas = mediaPlayer.getMediaPlayerCanvas();
        // draw icon in center of media player canvas
        int x = (mediaPlayerCanvas.getWidth() - playIcn.getIconWidth()) / 2;
        int y = (mediaPlayerCanvas.getHeight() - playIcn.getIconHeight()) / 2;
        if(player != null && player.status().isPlaying()) {
            g2d.drawImage(pauseIcn.getImage(), x, y, null);
        } else {
            g2d.drawImage(playIcn.getImage(), x, y, null);
        }

        if(embedded) {
            // draw close icon in top right corner
            x = mediaPlayerCanvas.getWidth() - closeIcn.getIconWidth() - 5;
            y = 5;
            g2d.drawImage(closeIcn.getImage(), x, y, null);
        }
    }

    /**
     * Editor actions
     */
    private void onMediaChanged(EditorEvent<EditorEventType.SessionMediaChangedData> ee) {
        loadMedia();
    }

    public void onSessionChanged(EditorEvent<Session> ee) {
        loadMedia();
    }

    private void onRecordChanged(EditorEvent<EditorEventType.RecordChangedData> ee) {
        if (!isAdjustVideo()) return;
        final Record utt = getEditor().currentRecord();
        if (utt == null) return;

        final MediaSegment media = utt.getMediaSegment();

        // check for necessary vars
        if (media == null) return;
        if (!mediaPlayer.willPlay()) return;

        // don't set position if player is playing
        if (mediaPlayer.isPlaying()) return;

        mediaPlayer.setTime((long) media.getStartValue());
    }

    private void doSegmentPlayback(EditorEvent<MediaSegment> ee) {
        MediaSegment segment = ee.data();

        long startTime = (long) segment.getStartValue();
        long length = (long) (segment.getStartValue() - segment.getEndValue());

        mediaPlayer.playSegment(startTime, length);
    }

    private void doCleanup(EditorEvent ee) {
        mediaPlayer.cleanup();
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded(boolean embedded) {
        this.embedded = embedded;
    }

    public int getMediaCanvasX() {
        return mediaCanvasX;
    }

    public void setMediaCanvasX(int mediaCanvasX) {
        this.mediaCanvasX = mediaCanvasX;
    }

    public int getMediaCanvasY() {
        return mediaCanvasY;
    }

    public void setMediaCanvasY(int mediaCanvasY) {
        this.mediaCanvasY = mediaCanvasY;
    }

    public Point getMediaCanvasPosition() {
        return new Point(mediaCanvasX, mediaCanvasY);
    }

    public int getMediaCanvasWidth() {
        return mediaCanvasWidth;
    }

    public void setMediaCanvasWidth(int mediaCanvasWidth) {
        this.mediaCanvasWidth = mediaCanvasWidth;
    }

    public int getMediaCanvasHeight() {
        return mediaCanvasHeight;
    }

    public void setMediaCanvasHeight(int mediaCanvasHeight) {
        this.mediaCanvasHeight = mediaCanvasHeight;
    }

    public void setMediaCanvasPosition(Point p) {
        this.mediaCanvasX = p.x;
        this.mediaCanvasY = p.y;
    }

    public Dimension getMediaCanvasSize() {
        return new Dimension(mediaCanvasWidth, mediaCanvasHeight);
    }

    public void setMediaCanvasSize(Dimension size) {
        this.mediaCanvasWidth = size.width;
        this.mediaCanvasHeight = size.height;
    }

    /**
     * Should the media position move with the
     * current record.
     */
    public boolean isAdjustVideo() {
        final Boolean isAdjustVideo = PrefHelper.getBoolean(ADJUST_VIDEO, Boolean.TRUE);
        return isAdjustVideo;
    }

    @Override
    public String getName() {
        return VIEW_NAME;
    }

    @Override
    public ImageIcon getIcon() {
        final String[] iconData = VIEW_ICON.split(":");
        return IconManager.getInstance().getFontIcon(iconData[0], iconData[1], IconSize.MEDIUM, UIManager.getColor(SessionEditorUIProps.VIEW_ACTIVE_TEXT));
    }

    @Override
    public JMenu getMenu() {
        final JMenu retVal = new JMenu(VIEW_NAME);
        final MenuBuilder menuBuilder = new MenuBuilder(retVal);
        setupMenu(menuBuilder);
        return retVal;
    }

    private void toggleEmbeddedVideo() {
        final VideoVisibleEdit edit = new VideoVisibleEdit(this, !mediaPlayer.isVideoVisible());
        getEditor().getUndoSupport().postEdit(edit);
    }

    public void hideMediaPlayerCanvas() {
        if(embedded) {
            mediaPlayer.setVideoVisible(false);
            final JComponent glassPane = (JComponent) getEditor().getRootPane().getGlassPane();
            if (SwingUtilities.isDescendingFrom(mediaPlayer.getMediaPlayerCanvas(), glassPane)) {
                glassPane.remove(mediaPlayer.getMediaPlayerCanvas());
            }
        }
    }

    public void showVideoInWindowGlassPane() {
        final CommonModuleFrame cmf = CommonModuleFrame.getCurrentFrame();
        if (cmf instanceof SessionEditorWindow sessionEditorWindow) {
            final TranscriptView transcriptView = (TranscriptView) sessionEditorWindow.getSessionEditor().getViewModel().getView(TranscriptView.VIEW_NAME);
            transcriptView.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    setupMediaCanvasBounds();
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                    setupMediaCanvasBounds();
                }
            });
            final JComponent glassPane = (JComponent) cmf.getGlassPane();
            setupMediaCanvasBounds();
            mediaPlayer.setVideoVisible(true);
            glassPane.setLayout(null);
            glassPane.add(mediaPlayer.getMediaPlayerCanvas());
            glassPane.revalidate();
            glassPane.setOpaque(false);
			glassPane.setVisible(true);
            mediaPlayer.getMediaPlayerCanvas().setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }
    }

    public void setupMediaCanvasBounds() {
        setupMediaCanvasBounds(true);
    }

    public void setupMediaCanvasBounds(boolean allowSnap) {
        final CommonModuleFrame cmf = CommonModuleFrame.getCurrentFrame();
        if (cmf instanceof SessionEditorWindow sessionEditorWindow) {
            // get bounds of media player view in window, place above on top-right
            final Rectangle bounds = getBounds();
            // convert to window coordinates
            final TranscriptView transcriptView = (TranscriptView) sessionEditorWindow.getSessionEditor().getViewModel().getView(TranscriptView.VIEW_NAME);
            final JComponent glassPane = (JComponent) cmf.getGlassPane();

            final int width = this.mediaCanvasWidth >= 0 ? this.mediaCanvasWidth : DEFAULT_MEDIA_CANVAS_WIDTH;
            final int height = this.mediaCanvasHeight >= 0 ? this.mediaCanvasHeight : DEFAULT_MEDIA_CANVAS_HEIGHT;
            final Point p = SwingUtilities.convertPoint(transcriptView, bounds.x, bounds.y, glassPane);

            final Insets insets = mediaPlayer.getMediaPlayerCanvas().getInsets();

            final int transcriptScrollBarWidth =
                    transcriptView.getTranscriptScrollPane().getVerticalScrollBar().isVisible() ?
                    transcriptView.getTranscriptScrollPane().getVerticalScrollBar().getWidth(): 0;

            final Point snapPoint = new Point(p.x + transcriptView.getWidth() - transcriptScrollBarWidth, p.y - transcriptView.getStatusBar().getHeight());
            int x = this.mediaCanvasX >= 0 ? this.mediaCanvasX : p.x + (transcriptView.getWidth() - width) - insets.right - transcriptScrollBarWidth;
            int y = this.mediaCanvasY >= 0 ? this.mediaCanvasY : p.y - height - transcriptView.getStatusBar().getHeight() - insets.top - insets.bottom;
            final Point bottomRight = new Point(x + width, y + height);

            // if bottomRight is within 10px of the snapPoint, snap to it
            if (allowSnap && Math.abs(bottomRight.x - snapPoint.x) < 10 && Math.abs(bottomRight.y - snapPoint.y) < 10) {
                // snap to bottom right corner
                x = snapPoint.x - width - insets.right;
                y = snapPoint.y - height - insets.top - insets.bottom;
                mediaCanvasX = -1;
                mediaCanvasY = -1;
            }

            mediaPlayer.getMediaPlayerCanvas().setBounds(x, y, width, height);
        }
    }

    @Override
    public DockPosition getPreferredDockPosition() {
        return DockPosition.WEST;
    }

    public PhonMediaPlayer getPlayer() {
        return this.mediaPlayer;
    }

    /**
     * Called when we need to refresh the media player.
     * Media players need to be refreshed when the
     * PlayerCanvas they are listening to becomes invalid.
     *
     * @param pae
     */
    public void onLoadMedia(PhonActionEvent pae) {
        loadMedia();
    }

    public void onLoadMedia(EditorEvent ee) {
        loadMedia();
    }

    public void reloadMedia() {
        loadMedia();
    }

    public void onMenuSelectGoto(PhonActionEvent pae)
            throws ParseException {
        // display a popup window with
        // a formatted text field to select media time

        String defValue = "000:00.000";
        Record utt = getEditor().currentRecord();
        if (utt.getMediaSegment() != null) {
            long startTime = (long) utt.getMediaSegment().getStartValue();
            defValue = MsFormatter.msToDisplayString(startTime);
        }

        MaskFormatter formatter = new MaskFormatter("###:##.###");
        formatter.setPlaceholderCharacter('0');
        JFormattedTextField formattedTextField =
                new JFormattedTextField(formatter);
        formattedTextField.setText(defValue);
        formattedTextField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                String timeStr =
                        ((JFormattedTextField) ae.getSource()).getText();
                long msVal;
                try {
                    msVal = MsFormatter.displayStringToMs(timeStr);
                    mediaPlayer.setTime(msVal);
                    if (timeSelectionPopup != null) {
                        timeSelectionPopup.dispose();
                        timeSelectionPopup = null;
                    }
                } catch (ParseException e) {
                    LogUtil.warning(e);
                }
            }

        });

        JButton menuBtn = mediaPlayer.getMenuButton();

        if (timeSelectionPopup == null) {
            timeSelectionPopup = new JFrame();
            timeSelectionPopup.setUndecorated(true);
            timeSelectionPopup.addWindowFocusListener(new WindowFocusListener() {

                @Override
                public void windowGainedFocus(WindowEvent we) {
                }

                @Override
                public void windowLostFocus(WindowEvent we) {
                    if (timeSelectionPopup != null) {
                        timeSelectionPopup.setVisible(false);
                        timeSelectionPopup = null;
                    }
                }
            });

            timeSelectionPopup.add(formattedTextField);

            Point p = menuBtn.getLocation();
            SwingUtilities.convertPointToScreen(p, menuBtn.getParent());
            // setup bounds
            Rectangle windowBounds = new Rectangle(
                    p.x,
                    p.y - menuBtn.getHeight(),
                    timeSelectionPopup.getPreferredSize().width,
                    timeSelectionPopup.getPreferredSize().height);
            timeSelectionPopup.setBounds(windowBounds);
            timeSelectionPopup.setVisible(true);
        }
    }

    public void onMenuGoto(PhonActionEvent pae) {
        final SessionFactory factory = SessionFactory.newFactory();
        MediaSegment lastSegment = factory.createMediaSegment();
        lastSegment.setStartValue(0.0f);
        lastSegment.setEndValue(0.0f);

        final Session t = getEditor().getSession();

        if (pae.getData() == null) {
            for (int uttIdx = t.getRecordCount() - 1; uttIdx >= 0; uttIdx--) {
                final Record utt = t.getRecord(uttIdx);
                if (utt.getMediaSegment().getEndValue() > lastSegment.getEndValue())
                    lastSegment = utt.getMediaSegment();
            }
        } else {
            final Participant p = (Participant) pae.getData();
            for (int uttIdx = t.getRecordCount() - 1; uttIdx >= 0; uttIdx--) {
                final Record utt = t.getRecord(uttIdx);
                if (utt.getSpeaker() != null
                        && utt.getSpeaker().getId() == p.getId()) {
                    lastSegment = utt.getMediaSegment();
                    break;
                }
            }
        }

        mediaPlayer.setTime((long) lastSegment.getEndValue());
    }

    /**
     * Toggle the option to trun on/off moving
     * video with the current record (while
     * paused.)
     */
    public void onToggleAdjustVideo() {
        final Boolean isAdjustVideo = isAdjustVideo();
        PrefHelper.getUserPreferences().putBoolean(ADJUST_VIDEO, !isAdjustVideo);
    }

    @Override
    public Properties getStateProperties() {
        Properties retVal = super.getStateProperties();
        retVal.put("mediaCanvasX", String.valueOf(mediaCanvasX));
        retVal.put("mediaCanvasY", String.valueOf(mediaCanvasY));
        retVal.put("mediaCanvasWidth", String.valueOf(mediaCanvasWidth));
        retVal.put("mediaCanvasHeight", String.valueOf(mediaCanvasHeight));
        retVal.put("videoVisible", String.valueOf(mediaPlayer.isVideoVisible()));
        retVal.put("embedded", String.valueOf(embedded));
        return retVal;
    }

    @Override
    public void loadStateProperties(Properties props) {
        super.loadStateProperties(props);

        if (props.containsKey("mediaCanvasX")) {
            mediaCanvasX = Integer.parseInt(props.getProperty("mediaCanvasX"));
        }
        if (props.containsKey("mediaCanvasY")) {
            mediaCanvasY = Integer.parseInt(props.getProperty("mediaCanvasY"));
        }
        if (props.containsKey("mediaCanvasWidth")) {
            mediaCanvasWidth = Integer.parseInt(props.getProperty("mediaCanvasWidth"));
        }
        if (props.containsKey("mediaCanvasHeight")) {
            mediaCanvasHeight = Integer.parseInt(props.getProperty("mediaCanvasHeight"));
        }
        if (props.containsKey("embedded")) {
            embedded = Boolean.parseBoolean(props.getProperty("embedded"));
        } else {
            embedded = true; // default
        }
        if(props.containsKey("videoVisible")) {
            loadVideoOnShow = Boolean.parseBoolean(props.getProperty("videoVisible"));
        }
    }

    private void setupMenu(MenuBuilder menuBuilder) {
        final PhonUIAction toggleEmbeddedVideoAct = PhonUIAction.runnable(MediaPlayerEditorView.this::toggleEmbeddedVideo);
        toggleEmbeddedVideoAct.putValue(PhonUIAction.NAME, mediaPlayer.isVideoVisible() ? "Hide video player" : "Show video player");
        toggleEmbeddedVideoAct.putValue(PhonUIAction.SHORT_DESCRIPTION, mediaPlayer.isVideoVisible() ? "Hide video player" : "Show video player");
        menuBuilder.addItem(".", new JMenuItem(toggleEmbeddedVideoAct));

        if(embedded) {
            final PhonUIAction resetVideoSizeAct = PhonUIAction.runnable(MediaPlayerEditorView.this::resetVideoSizeAndPosition);
            resetVideoSizeAct.putValue(PhonUIAction.NAME, "Reset video size and position");
            resetVideoSizeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Reset video size and position to default");
            menuBuilder.addItem(".", new JMenuItem(resetVideoSizeAct));

            final PhonUIAction moveToExternalWindowAct = PhonUIAction.runnable(MediaPlayerEditorView.this::moveToExternalWindow);
            moveToExternalWindowAct.putValue(PhonUIAction.NAME, "Move video to external window");
            moveToExternalWindowAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move video player to external window");
            menuBuilder.addItem(".", new JMenuItem(moveToExternalWindowAct));
        }

        menuBuilder.addItem(".", new TakeSnapshotAction(getEditor(), this));

        menuBuilder.addSeparator(".", "adjust video options");
        final ToggleAdjustVideoAction adjustVideoAct = new ToggleAdjustVideoAction(getEditor(), MediaPlayerEditorView.this);
        adjustVideoAct.putValue(PhonUIAction.SELECTED_KEY, isAdjustVideo());
        JCheckBoxMenuItem adjustVideoItem = new JCheckBoxMenuItem(adjustVideoAct);
        menuBuilder.addItem(".", adjustVideoItem);

        menuBuilder.addSeparator(".", "Media Player");
        setupGotoItems(menuBuilder);
    }

    private void setupGotoItems(MenuBuilder menuBuilder) {
        final GoToAction gotoSelectAct = new GoToAction(getEditor(), MediaPlayerEditorView.this);
        JMenuItem gotoSelectItem = new JMenuItem(gotoSelectAct);
        menuBuilder.addItem(".", gotoSelectItem);

        final GoToEndOfSegmentedAction gotoLastSegmentAct = new GoToEndOfSegmentedAction(getEditor(), MediaPlayerEditorView.this);
        menuBuilder.addItem(".", gotoLastSegmentAct);

        final SessionEditor editor = getEditor();
        final Session session = editor.getSession();

        // for each participant
        for (int i = 0; i < session.getParticipantCount(); i++) {
            final Participant p = session.getParticipant(i);
            final GoToEndOfSegmentedAction gotoPartSegmentAct =
                    new GoToEndOfSegmentedAction(getEditor(), MediaPlayerEditorView.this, p);
            menuBuilder.addItem(".", gotoPartSegmentAct);
        }
    }

    private void resetVideoSizeAndPosition() {
        if(embedded) {
            final VideoPositionAndSizeEdit edit = new VideoPositionAndSizeEdit(this,
                -1, -1, DEFAULT_MEDIA_CANVAS_WIDTH, DEFAULT_MEDIA_CANVAS_HEIGHT,
                mediaCanvasX, mediaCanvasY, mediaCanvasWidth, mediaCanvasHeight);
            getEditor().getUndoSupport().postEdit(edit);
        }
    }

    private void moveToExternalWindow() {
        if(embedded) {
            // remove media player canvas from glass pane and into accessory window
            getEditor().getViewModel().showViewInAccessoryWindow(VIEW_NAME);
        }
    }

    /**
     * Media player menu filter
     */
    private class MediaMenuFilter implements IMediaMenuFilter {

        @Override
        public JPopupMenu makeMenuChanges(JPopupMenu menu) {
            menu.removeAll();
            final MenuBuilder menuBuilder = new MenuBuilder(menu);
            setupMenu(menuBuilder);
            return menu;
        }

    }

    private class FileSelectionTransferHandler extends FileTransferHandler {

        private static final long serialVersionUID = 6799990443658389742L;

        @Override
        public boolean importData(JComponent comp, Transferable transferable) {
            File file = null;
            try {
                file = getFile(transferable);
            } catch (IOException e) {
                return false;
            }

            if (file != null && FileFilter.mediaFilter.accept(file)) {
                MediaLocationEdit mediaEdit = new MediaLocationEdit(getEditor(), file.getAbsolutePath());
                getEditor().getUndoSupport().postEdit(mediaEdit);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public File getFile(Transferable transferable) throws IOException {
            File retVal = super.getFile(transferable);
            final FileFilter filter = FileFilter.mediaFilter;
            if (filter != null && !filter.accept(retVal)) {
                retVal = null;
            }
            return retVal;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return super.createTransferable(c);
        }

    }

    private class MediaPlayerCanvasOverlayListener extends MouseAdapter {
        @Override
        public void mouseExited(MouseEvent e) {
            super.mouseExited(e);
            paintOverlay = false;
            if(!mediaPlayer.isPlaying())
                mediaPlayer.getMediaPlayerCanvas().repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            super.mouseEntered(e);
            paintOverlay = true;
            if(!mediaPlayer.isPlaying())
                mediaPlayer.getMediaPlayerCanvas().repaint();
        }
    }

    private boolean isDraggingPosition = false;
    private boolean isDraggingSize = false;
    private boolean isDraggingTopLeft = false;
    private boolean isDraggingBottomRight = false;
    private boolean isDraggingBottomLeft = false;
    private Point dragStartPoint = null;
    private Point mediaPositionStart = null;
    private Dimension mediaSizeStart = null;
    private MouseInputAdapter mediaPlayerCanvasMouseAdapter = new MouseInputAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                if(embedded) {
                    final Rectangle closeBounds = new Rectangle(mediaPlayer.getMediaPlayerCanvas().getWidth() - IconSize.MEDIUM.getWidth() - 5, 0,
                        IconSize.MEDIUM.getWidth() + 5, IconSize.MEDIUM.getHeight() + 5);
                    if(closeBounds.contains(e.getPoint())) {
                        toggleEmbeddedVideo();
                    } else {
                        if (mediaPlayer.getMediaPlayer() != null && mediaPlayer.getMediaPlayer().media().isValid()) {
                            if (mediaPlayer.getMediaPlayer().status().isPlaying()) {
                                mediaPlayer.getMediaPlayer().controls().pause();
                            } else {
                                mediaPlayer.getMediaPlayer().controls().play();
                            }
                        }
                    }
                } else {
                    if (mediaPlayer.getMediaPlayer() != null && mediaPlayer.getMediaPlayer().media().isValid()) {
                        if (mediaPlayer.getMediaPlayer().status().isPlaying()) {
                            mediaPlayer.getMediaPlayer().controls().pause();
                        } else {
                            mediaPlayer.getMediaPlayer().controls().play();
                        }
                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if(!embedded) return;

            final Rectangle topLeftResizeBounds = new Rectangle(0, 0, 10, 10);
            final Rectangle bottomRightResizeBounds = new Rectangle(mediaPlayer.getMediaPlayerCanvas().getWidth() - 10, mediaPlayer.getMediaPlayerCanvas().getHeight() - 10, 10, 10);
            final Rectangle bottomLeftResizeBounds = new Rectangle(0, mediaPlayer.getMediaPlayerCanvas().getHeight() - 10, 10, 10);
            if (topLeftResizeBounds.contains(e.getPoint())) {
                isDraggingSize = true;
                isDraggingPosition = false;
                isDraggingTopLeft = true;
                isDraggingBottomRight = false;
            } else if (bottomRightResizeBounds.contains(e.getPoint())) {
                isDraggingSize = true;
                isDraggingPosition = false;
                isDraggingTopLeft = false;
                isDraggingBottomRight = true;
            } else if (bottomLeftResizeBounds.contains(e.getPoint())) {
                isDraggingSize = true;
                isDraggingPosition = false;
                isDraggingTopLeft = false;
                isDraggingBottomRight = false;
                isDraggingBottomLeft = true;
            } else {
                // start dragging position
                isDraggingSize = false;
                isDraggingPosition = true;
            }
            dragStartPoint = e.getPoint();
            mediaPositionStart = new Point(mediaCanvasX, mediaCanvasY);
            mediaSizeStart = new Dimension(mediaCanvasWidth, mediaCanvasHeight);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if(!embedded) return;

            final Rectangle topLeftResizeBounds = new Rectangle(0, 0, 10, 10);
            final Rectangle bottomRightResizeBounds = new Rectangle(mediaPlayer.getMediaPlayerCanvas().getWidth() - 10, mediaPlayer.getMediaPlayerCanvas().getHeight() - 10, 10, 10);
            final Rectangle bottomLeftResizeBounds = new Rectangle(0, mediaPlayer.getMediaPlayerCanvas().getHeight() - 10, 10, 10);

            if (topLeftResizeBounds.contains(e.getPoint())) {
                mediaPlayer.getMediaPlayerCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
            } else if (bottomRightResizeBounds.contains(e.getPoint())) {
                mediaPlayer.getMediaPlayerCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
            } else if (bottomLeftResizeBounds.contains(e.getPoint())) {
                mediaPlayer.getMediaPlayerCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
            } else {
                mediaPlayer.getMediaPlayerCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if(embedded && isDraggingPosition) {
                final Point dragEndPoint = e.getPoint();
                // adjust media player canvas position
                final Point currentPoint = mediaPlayer.getMediaPlayerCanvas().getLocation();
                mediaCanvasX = Math.max(0, currentPoint.x + (dragEndPoint.x - dragStartPoint.x));
                mediaCanvasY = Math.max(0, currentPoint.y + (dragEndPoint.y - dragStartPoint.y));
                setupMediaCanvasBounds();
            } else if(embedded && isDraggingSize) {
                Point dragEndPoint = e.getPoint();

                if(isDraggingTopLeft) {
                    final Rectangle currentBounds = mediaPlayer.getMediaPlayerCanvas().getBounds();
                    Point bottomRight = new Point(currentBounds.x + currentBounds.width, currentBounds.y + currentBounds.height);
                    // convert point to session editor coordinates
                    dragEndPoint = SwingUtilities.convertPoint(mediaPlayer.getMediaPlayerCanvas(), dragEndPoint, getEditor().getRootPane());
                    dragEndPoint.x = Math.max(0, dragEndPoint.x);
                    dragEndPoint.y = Math.max(0, dragEndPoint.y);

                    final Rectangle newBounds = new Rectangle(
                            dragEndPoint.x, dragEndPoint.y,
                            Math.max(0, bottomRight.x - dragEndPoint.x),
                            Math.max(0, bottomRight.y - dragEndPoint.y)
                    );
                    if (mediaCanvasX >= 0 && mediaCanvasY >= 0) {
                        mediaCanvasX = newBounds.x;
                        mediaCanvasY = newBounds.y;
                    }
                    mediaCanvasWidth = newBounds.width;
                    mediaCanvasHeight = newBounds.height;
                    setupMediaCanvasBounds();
                } else if(isDraggingBottomRight) {
                    final Rectangle currentBounds = mediaPlayer.getMediaPlayerCanvas().getBounds();
                    Point topLeft = new Point(currentBounds.x, currentBounds.y);
                    // convert point to session editor coordinates
                    dragEndPoint = SwingUtilities.convertPoint(mediaPlayer.getMediaPlayerCanvas(), dragEndPoint, getEditor().getRootPane());
                    dragEndPoint.x = Math.max(0, dragEndPoint.x);
                    dragEndPoint.y = Math.max(0, dragEndPoint.y);

                    final Rectangle newBounds = new Rectangle(
                            topLeft.x, topLeft.y,
                            Math.max(0, dragEndPoint.x - topLeft.x),
                            Math.max(0, dragEndPoint.y - topLeft.y)
                    );
                    mediaCanvasX = newBounds.x;
                    mediaCanvasY = newBounds.y;
                    mediaCanvasWidth = newBounds.width;
                    mediaCanvasHeight = newBounds.height;
                    setupMediaCanvasBounds(false);
                } else if(isDraggingBottomLeft) {
                    final Rectangle currentBounds = mediaPlayer.getMediaPlayerCanvas().getBounds();
                    Point topRight = new Point(currentBounds.x + currentBounds.width, currentBounds.y);
                    // convert point to session editor coordinates
                    dragEndPoint = SwingUtilities.convertPoint(mediaPlayer.getMediaPlayerCanvas(), dragEndPoint, getEditor().getRootPane());
                    dragEndPoint.x = Math.max(0, dragEndPoint.x);
                    dragEndPoint.y = Math.max(0, dragEndPoint.y);

                    final Rectangle newBounds = new Rectangle(
                            dragEndPoint.x, topRight.y,
                            Math.max(0, topRight.x - dragEndPoint.x),
                            Math.max(0, dragEndPoint.y - topRight.y)
                    );
                    mediaCanvasX = newBounds.x;
                    mediaCanvasY = newBounds.y;
                    mediaCanvasWidth = newBounds.width;
                    mediaCanvasHeight = newBounds.height;
                    setupMediaCanvasBounds(false);
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if(!embedded) return;
            if(isDraggingPosition) {
                isDraggingPosition = false;
                dragStartPoint = null;
            } else if(isDraggingSize) {
                isDraggingSize = false;
                dragStartPoint = null;
            }
            final Point finalPosition = new Point(mediaCanvasX, mediaCanvasY);
            final Dimension finalSize = new Dimension(mediaCanvasWidth, mediaCanvasHeight);

            if(mediaPositionStart != null && mediaSizeStart != null) {
                if (mediaPositionStart.x != finalPosition.x ||
                        mediaPositionStart.y != finalPosition.y ||
                        mediaSizeStart.width != finalSize.width ||
                        mediaSizeStart.height != finalSize.height) {
                    // post edit
                    final VideoPositionAndSizeEdit edit = new VideoPositionAndSizeEdit(
                            MediaPlayerEditorView.this,
                            mediaCanvasX, mediaCanvasY, mediaCanvasWidth, mediaCanvasHeight,
                            mediaPositionStart.x, mediaPositionStart.y,
                            mediaSizeStart.width, mediaSizeStart.height);
                    getEditor().getUndoSupport().postEdit(edit);
                }
            }
            mediaPositionStart = null;
            mediaSizeStart = null;
        }
    };
}
