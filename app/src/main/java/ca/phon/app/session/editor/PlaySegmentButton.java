package ca.phon.app.session.editor;

import ca.phon.app.session.editor.actions.PlayAdjacencySequenceAction;
import ca.phon.app.session.editor.actions.PlayCustomSegmentAction;
import ca.phon.app.session.editor.actions.PlaySegmentAction;
import ca.phon.app.session.editor.actions.PlaySpeechTurnAction;
import ca.phon.app.session.editor.view.mediaPlayer.MediaPlayerEditorView;
import ca.phon.session.MediaSegment;
import ca.phon.ui.ButtonPopup;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.function.Supplier;

public class PlaySegmentButton extends DropDownButton {

    private final SessionEditor editor;

    private Supplier<MediaSegment> selectedSegmentSupplier;

    public PlaySegmentButton(SessionEditor editor) {
        this(editor, null);
    }

    public PlaySegmentButton(SessionEditor editor, Supplier<MediaSegment> selectedSegmentSupplier) {
        super();
        this.editor = editor;
        this.selectedSegmentSupplier = selectedSegmentSupplier;

        init();
        editor.getMediaModel().getSegmentPlayback().addPropertyChangeListener(SegmentPlayback.PLAYBACK_PROP, this::onSegmentPlaybackChange);
    }

    public SessionEditor getEditor() {
        return this.editor;
    }

    private void setSelectedSegmentSupplier(Supplier<MediaSegment> selectedSegmentSupplier) {
        this.selectedSegmentSupplier = selectedSegmentSupplier;
    }

    public MediaSegment getSelectedSegment() {
        if(this.selectedSegmentSupplier != null) {
            return this.selectedSegmentSupplier.get();
        } else {
            return null;
        }
    }

    private void init() {
        final JPopupMenu playSegmentMenu = new JPopupMenu();
        playSegmentMenu.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                playSegmentMenu.removeAll();
                setupPlaySegmentMenu(getEditor(), selectedSegmentSupplier, new MenuBuilder(playSegmentMenu));
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }

        });

        final ImageIcon playIcn = IconManager.getInstance()
                .getFontIcon(IconManager.GoogleMaterialDesignIconsFontName, "play_arrow", IconSize.MEDIUM, Color.black);
        final PhonUIAction playSegmentAct = PhonUIAction.eventConsumer(this::playPause);
        playSegmentAct.putValue(PhonUIAction.NAME, "Play segment");
        playSegmentAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Play segment");
        playSegmentAct.putValue(PhonUIAction.SMALL_ICON, playIcn);
        setAction(playSegmentAct);
        setButtonPopup(new ButtonPopup(playSegmentMenu));
        setOnlyPopup(false);

        setFocusable(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setRolloverEnabled(true);
    }

    private void onSegmentPlaybackChange(PropertyChangeEvent evt) {
        SegmentPlayback segmentPlayback = (SegmentPlayback)evt.getSource();
        if(SegmentPlayback.PLAYBACK_PROP.contentEquals(evt.getPropertyName())) {
            if(segmentPlayback.isPlaying()) {
                final ImageIcon stopIcon = IconManager.getInstance().getFontIcon(IconManager.GoogleMaterialDesignIconsFontName, "stop", IconSize.MEDIUM, UIManager.getColor("Button.foreground"));
                setIcon(stopIcon);
                setText("Stop playback");
            } else {
                final ImageIcon playIcon = IconManager.getInstance().getFontIcon(IconManager.GoogleMaterialDesignIconsFontName, "play_arrow", IconSize.MEDIUM, UIManager.getColor("Button.foreground"));
                setIcon(playIcon);
                setText("Play segment");
            }
        }
    }
//
//    public SessionEditor getEditor() {
//        return this.editor;
//    }

    private void playPause(PhonActionEvent<Void> pae) {
        final SessionMediaModel mediaModel = getEditor().getMediaModel();
        final SegmentPlayback segPlayback = mediaModel.getSegmentPlayback();
        if(segPlayback.isPlaying()) {
            segPlayback.stopPlaying();
        } else {
            (new PlaySegmentAction(getEditor())).actionPerformed(pae.getActionEvent());
        }
    }

    /**
     * Setup play segment menu actions for editor segment playback
     * @param editor session editor
     * @param segmentSupplier segment supplier (may be null)
     * @param builder menu builder
     */
    public static void setupPlaySegmentMenu(SessionEditor editor, Supplier<MediaSegment> segmentSupplier, MenuBuilder builder) {
        final SessionMediaModel mediaModel = editor.getMediaModel();
        final SegmentPlayback segPlayback = mediaModel.getSegmentPlayback();

        if(segPlayback.isPlaying()) {
            final PhonUIAction stopAct = PhonUIAction.runnable(segPlayback::stopPlaying);
            stopAct.putValue(PhonUIAction.NAME, "Stop playback");
            stopAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-stop", IconSize.SMALL));
            stopAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Stop segment playback");
            builder.addItem(".", stopAct);

            builder.addSeparator(".", "s1");
        }

        boolean enabled = (mediaModel.isSessionAudioAvailable() ||
                (mediaModel.isSessionMediaAvailable() && editor.getViewModel().isShowing(MediaPlayerEditorView.VIEW_NAME)));

        final MediaSegment selectedSegment = segmentSupplier != null ? segmentSupplier.get() : null;
        if(selectedSegment != null) {
            final PlayCustomSegmentAction playSelectedSegAct = new PlayCustomSegmentAction(editor, selectedSegment);
            playSelectedSegAct.putValue(PhonUIAction.NAME, "Play selected segment");
            playSelectedSegAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Play selected segment");
            builder.addItem(".", playSelectedSegAct).setEnabled(enabled);
        }

        builder.addItem(".", new PlaySegmentAction(editor)).setEnabled(enabled);
        builder.addItem(".", new PlayCustomSegmentAction(editor)).setEnabled(enabled);
        builder.addItem(".", new PlaySpeechTurnAction(editor)).setEnabled(enabled);
        builder.addItem(".", new PlayAdjacencySequenceAction(editor)).setEnabled(enabled);
    }

}
