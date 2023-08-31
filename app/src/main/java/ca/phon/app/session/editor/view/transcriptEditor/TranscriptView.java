package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.view.transcriptEditor.actions.ToggleSingleRecordAction;
import ca.phon.plugin.PluginManager;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class TranscriptView extends EditorView {

    public final static String VIEW_NAME = "Transcript Editor";
    public final static String VIEW_ICON = "blank";
    private final TranscriptEditor transcriptEditor;

    public TranscriptView(SessionEditor editor) {
        super(editor);
        this.transcriptEditor = new TranscriptEditor(editor.getSession(), editor.getEventManager(), editor.getUndoSupport(), editor.getUndoManager());
        this.transcriptEditor.setSegmentPlayback(editor.getMediaModel().getSegmentPlayback());
        this.transcriptEditor.addPropertyChangeListener("currentRecordIndex", e -> {
            editor.setCurrentRecordIndex((Integer) e.getNewValue());
        });
        initUI();
        editor.getEventManager().registerActionForEvent(EditorEventType.EditorFinishedLoading, this::onEditorFinishedLoading, EditorEventManager.RunOn.EditorEventDispatchThread);
    }

    private void initUI() {
        TranscriptScrollPane scrollPane = new TranscriptScrollPane(transcriptEditor);
        scrollPane.setRowHeaderView(new TranscriptRowHeader(transcriptEditor));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            scrollPane.getRowHeader().setViewPosition(new Point(0, e.getValue()));
        });
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(new TranscriptStatusBar(transcriptEditor), BorderLayout.SOUTH);
    }

    @Override
    public String getName() {
        return VIEW_NAME;
    }

    @Override
    public ImageIcon getIcon() {
        return IconManager.getInstance().getIcon(VIEW_ICON, IconSize.SMALL);
    }

    @Override
    public JMenu getMenu() {
        final JMenu retVal = new JMenu();

        retVal.add(new ToggleSingleRecordAction(getEditor(), this));

        return retVal;
    }

    private void createTierLabelPopup(JLabel tierLabel, MouseEvent mouseEvent) {

        JPopupMenu menu = new JPopupMenu();
        MenuBuilder builder = new MenuBuilder(menu);

        var extPts = PluginManager.getInstance().getExtensionPoints(TierLabelMenuHandler.class);

        for (var extPt : extPts) {
            var menuHandler = extPt.getFactory().createObject(getEditor());
            menuHandler.addMenuItems(builder);
        }

        menu.show(tierLabel, mouseEvent.getX(), mouseEvent.getY());
    }

    private void onEditorFinishedLoading(EditorEvent<Void> event) {
        transcriptEditor.setSession();
    }

    public boolean isSingleRecordActive() {
        return transcriptEditor.getTranscriptDocument().getSingleRecordView();
    }

    public void toggleSingleRecordActive() {
        transcriptEditor.getTranscriptDocument().setSingleRecordView(!isSingleRecordActive());
    }
}
