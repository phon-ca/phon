package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.view.transcriptEditor.actions.*;
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
    private TranscriptScrollPane transcriptScrollPane;

    public TranscriptView(SessionEditor editor) {
        super(editor);
        this.transcriptEditor = new TranscriptEditor(editor.getSession(), editor.getEventManager(), editor.getUndoSupport(), editor.getUndoManager());
        this.transcriptEditor.setSegmentPlayback(editor.getMediaModel().getSegmentPlayback());
        this.transcriptEditor.addPropertyChangeListener("currentRecordIndex", e -> editor.setCurrentRecordIndex((Integer) e.getNewValue()));
        initUI();
        editor.getEventManager().registerActionForEvent(EditorEventType.EditorFinishedLoading, this::onEditorFinishedLoading, EditorEventManager.RunOn.EditorEventDispatchThread);
        if (editor.isFinishedLoading()) {
            transcriptEditor.loadSession();
        }
    }

    private void initUI() {
        transcriptScrollPane = new TranscriptScrollPane(transcriptEditor);
        transcriptScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        setLayout(new BorderLayout());
        add(transcriptScrollPane, BorderLayout.CENTER);
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
        retVal.add(new ToggleRecordNumbersAction(getEditor(), this));
        retVal.add(new ToggleSyllabificationVisibleAction(getEditor(), this));
        retVal.add(new ToggleSyllabificationIsComponent(getEditor(), this));
        retVal.add(new ToggleAlignmentVisibleAction(getEditor(), this));
        retVal.add(new ToggleAlignmentIsComponentAction(getEditor(), this));

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
        transcriptEditor.loadSession();
    }

    public boolean isSingleRecordActive() {
        return transcriptEditor.getTranscriptDocument().getSingleRecordView();
    }

    public void toggleSingleRecordActive() {
        transcriptEditor.getTranscriptDocument().setSingleRecordView(!isSingleRecordActive());
    }

    public boolean getShowRecordNumbers() {
        return transcriptScrollPane.getGutter().getShowRecordNumbers();
    }

    public void toggleShowRecordNumbers() {
        transcriptScrollPane.getGutter().setShowRecordNumbers(!getShowRecordNumbers());
    }

    public boolean isSyllabificationVisible() {
        return transcriptEditor.isSyllabificationVisible();
    }

    public void toggleSyllabificationVisible() {
        transcriptEditor.setSyllabificationVisible(!isSyllabificationVisible());
    }

    public boolean isSyllabificationComponent() {
        return transcriptEditor.isSyllabificationComponent();
    }

    public void toggleSyllabificationIsComponent() {
        transcriptEditor.setSyllabificationIsComponent(!isSyllabificationComponent());
    }

    public boolean isAlignmentVisible() {
        return transcriptEditor.isAlignmentVisible();
    }

    public void toggleAlignmentVisible() {
        transcriptEditor.setAlignmentIsVisible(!isAlignmentVisible());
    }

    public boolean isAlignmentComponent() {
        return transcriptEditor.isAlignmentComponent();
    }

    public void toggleAlignmentIsComponent() {
        transcriptEditor.setAlignmentIsComponent(!isAlignmentComponent());
    }
}
