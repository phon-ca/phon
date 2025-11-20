package ca.phon.app.session.editor;

import ca.phon.app.session.editor.actions.PlayAdjacencySequenceAction;
import ca.phon.app.session.editor.actions.PlayCustomSegmentAction;
import ca.phon.app.session.editor.actions.PlaySegmentAction;
import ca.phon.app.session.editor.actions.PlaySpeechTurnAction;
import ca.phon.app.session.editor.view.mediaPlayer.MediaPlayerEditorView;
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

public class PlaySegmentButton extends DropDownButton {

    private SessionEditor editor;

    public PlaySegmentButton(SessionEditor editor) {
        super();
        this.editor = editor;

        init();
    }

    private void init() {
        final JPopupMenu playSegmentMenu = new JPopupMenu();
        playSegmentMenu.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                playSegmentMenu.removeAll();
                setupPlaySegmentMenu(new MenuBuilder(playSegmentMenu));
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }

        });

        final ImageIcon playIcn = IconManager.getInstance()
                .getFontIcon(IconManager.GoogleMaterialDesignIconsFontName, "play_circle", IconSize.MEDIUM, Color.black);
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

    public SessionEditor getEditor() {
        return this.editor;
    }

    private void playPause(PhonActionEvent<Void> pae) {
        final SessionMediaModel mediaModel = getEditor().getMediaModel();
        final SegmentPlayback segPlayback = mediaModel.getSegmentPlayback();
        if(segPlayback.isPlaying()) {
            segPlayback.stopPlaying();
        } else {
            (new PlaySegmentAction(getEditor())).actionPerformed(pae.getActionEvent());
        }
    }

    private void setupPlaySegmentMenu(MenuBuilder builder) {
        final SessionMediaModel mediaModel = getEditor().getMediaModel();
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
                (mediaModel.isSessionMediaAvailable() && getEditor().getViewModel().isShowing(MediaPlayerEditorView.VIEW_NAME)));
        builder.addItem(".", new PlaySegmentAction(getEditor())).setEnabled(enabled);
        builder.addItem(".", new PlayCustomSegmentAction(getEditor())).setEnabled(enabled);
        builder.addItem(".", new PlaySpeechTurnAction(getEditor())).setEnabled(enabled);
        builder.addItem(".", new PlayAdjacencySequenceAction(getEditor())).setEnabled(enabled);
    }

}
