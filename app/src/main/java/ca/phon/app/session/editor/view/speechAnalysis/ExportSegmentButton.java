package ca.phon.app.session.editor.view.speechAnalysis;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.ui.ButtonPopup;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class ExportSegmentButton extends DropDownButton {

    final private SpeechAnalysisEditorView view;

    public ExportSegmentButton(SpeechAnalysisEditorView view) {
        super();
        this.view = view;

        init();
    }

    private void init() {
        final JPopupMenu saveMenu = new JPopupMenu();
        saveMenu.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                saveMenu.removeAll();
                setupExportMenu(new MenuBuilder(saveMenu));
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
        final PhonUIAction<Void> exportAct = PhonUIAction.runnable(view::onExportSelectionOrSegment);
        exportAct.putValue(PhonUIAction.SMALL_ICON, exportIcon);
        exportAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Export selection/segment (audio only)");
        exportAct.putValue(PhonUIAction.NAME, "Export segment...");
        exportAct.putValue(DropDownButton.BUTTON_POPUP, saveMenu);
        setAction(exportAct);
        setButtonPopup(new ButtonPopup(saveMenu));

        setFocusable(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setRolloverEnabled(true);
    }

    private void setupExportMenu(MenuBuilder builder) {
        final PhonUIAction<Void> exportSelectionAct = PhonUIAction.runnable(view::exportSelection);
        exportSelectionAct.putValue(PhonUIAction.NAME, "Export selection...");
        exportSelectionAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Export selection (audio only)");
        final ImageIcon exportIcon =
                IconManager.getInstance().getFontIcon(IconManager.GoogleMaterialDesignIconsFontName, "file_upload", IconSize.MEDIUM, UIManager.getColor("textText"));
        exportSelectionAct.putValue(PhonUIAction.SMALL_ICON, exportIcon);

        final PhonUIAction<Void> exportSegmentAct = PhonUIAction.runnable(view::exportSegment);
        exportSegmentAct.putValue(PhonUIAction.NAME, "Export record segment...");
        exportSegmentAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Export record segment (audio only)");
        exportSegmentAct.putValue(PhonUIAction.SMALL_ICON, exportIcon);

        builder.addItem(".", exportSelectionAct).setEnabled(view.getSelectionInterval() != null);
        builder.addItem(".", exportSegmentAct).setEnabled(view.getCurrentRecordInterval() != null);
    }

}
