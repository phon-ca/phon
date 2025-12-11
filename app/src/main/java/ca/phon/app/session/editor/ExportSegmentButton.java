package ca.phon.app.session.editor;

import ca.phon.app.session.editor.actions.ExportAdjacencySequenceAction;
import ca.phon.app.session.editor.actions.ExportCustomSegmentAction;
import ca.phon.app.session.editor.actions.ExportSegmentAction;
import ca.phon.app.session.editor.actions.ExportSpeechTurnAction;
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
import java.util.function.Supplier;

public class ExportSegmentButton extends DropDownButton {

    private final SessionEditor editor;

    private Supplier<MediaSegment> selectedSegmentSupplier;

    public ExportSegmentButton(SessionEditor editor) {
        this(editor, null);
    }

    public ExportSegmentButton(SessionEditor editor, Supplier<MediaSegment> selectedSegmentSupplier) {
        super();
        this.editor = editor;
        this.selectedSegmentSupplier = selectedSegmentSupplier;

        init();
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
        final JPopupMenu exportSegmentMenu = new JPopupMenu();
        exportSegmentMenu.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                exportSegmentMenu.removeAll();
                setupExportMenu(getEditor(), selectedSegmentSupplier, new MenuBuilder(exportSegmentMenu));
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }

        });

        final ImageIcon exportIcon =
                IconManager.getInstance().getFontIcon(IconManager.GoogleMaterialDesignIconsFontName, "file_upload", IconSize.MEDIUM, UIManager.getColor("textText"));
        final PhonUIAction exportSegmentAct = PhonUIAction.eventConsumer(this::exportSegment);
        exportSegmentAct.putValue(PhonUIAction.NAME, "Export segment");
        exportSegmentAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Export segment");
        exportSegmentAct.putValue(PhonUIAction.SMALL_ICON, exportIcon);
        setAction(exportSegmentAct);
        setButtonPopup(new ButtonPopup(exportSegmentMenu));
        setOnlyPopup(false);

        setFocusable(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setRolloverEnabled(true);
    }

    private void exportSegment(PhonActionEvent<Void> pae) {
        (new ExportSegmentAction(getEditor())).actionPerformed(pae.getActionEvent());
    }

    /**
     * Setup export segment menu
     * @param editor session editor
     * @param segmentSupplier supplier for selected segment (may be null)
     * @param builder menu builder
     */
    public static void setupExportMenu(SessionEditor editor, Supplier<MediaSegment> segmentSupplier, MenuBuilder builder) {
        final SessionMediaModel mediaModel = editor.getMediaModel();

        boolean enabled = (mediaModel.isSessionAudioAvailable() ||
                (mediaModel.isSessionMediaAvailable() && editor.getViewModel().isShowing(MediaPlayerEditorView.VIEW_NAME)));

        final MediaSegment selectedSegment = segmentSupplier != null ? segmentSupplier.get() : null;
        if(selectedSegment != null) {
            final ExportCustomSegmentAction exportSelectedSegAct = new ExportCustomSegmentAction(editor, selectedSegment);
            exportSelectedSegAct.putValue(PhonUIAction.NAME, "Export selected segment");
            exportSelectedSegAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Export selected segment");
            builder.addItem(".", exportSelectedSegAct).setEnabled(enabled);
        }

        builder.addItem(".", new ExportSegmentAction(editor)).setEnabled(enabled);
        builder.addItem(".", new ExportCustomSegmentAction(editor)).setEnabled(enabled);
        builder.addItem(".", new ExportSpeechTurnAction(editor)).setEnabled(enabled);
        builder.addItem(".", new ExportAdjacencySequenceAction(editor)).setEnabled(enabled);
    }

}
