package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.view.transcriptEditor.actions.*;
import ca.phon.plugin.PluginManager;
import ca.phon.session.MediaSegment;
import ca.phon.session.MediaUnit;
import ca.phon.session.SessionFactory;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import org.jdesktop.swingx.HorizontalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class TranscriptView extends EditorView {

    public final static String VIEW_NAME = "Transcript Editor";
    public final static String VIEW_ICON = "blank";
    private final TranscriptEditor transcriptEditor;
    private TranscriptScrollPane transcriptScrollPane;

    public TranscriptView(SessionEditor editor) {
        super(editor);
        this.transcriptEditor = new TranscriptEditor(
            editor.getSession(),
            editor.getEventManager(),
            editor.getUndoSupport(),
            editor.getUndoManager()
        );
        this.transcriptEditor.setMediaModel(editor.getMediaModel());
        this.transcriptEditor.addPropertyChangeListener(
            "currentRecordIndex", e -> editor.setCurrentRecordIndex((Integer) e.getNewValue())
        );
        initUI();
        editor.getEventManager().registerActionForEvent(
            EditorEventType.EditorFinishedLoading,
            this::onEditorFinishedLoading,
            EditorEventManager.RunOn.EditorEventDispatchThread
        );
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

        JPanel toolbar = new JPanel(new HorizontalLayout());
        add(toolbar, BorderLayout.NORTH);
        JButton menuButton = new JButton("Menu");
        menuButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JMenu menu = getMenu();
                JPopupMenu popupMenu = new JPopupMenu();
                Arrays.stream(menu.getMenuComponents()).forEach(menuItem -> popupMenu.add(menuItem));
                popupMenu.show(menuButton, e.getX(),e.getY());
            }
        });
        menuButton.setToolTipText("Show transcript editor menu");
        toolbar.add(menuButton);

        PhonUIAction<Void> showMediaAct = PhonUIAction.eventConsumer(this::showMediaPopup, null);
        showMediaAct.putValue(PhonUIAction.NAME, "Test media popup");
        toolbar.add(new JButton(showMediaAct));
    }

    private void showMediaPopup(PhonActionEvent<Void> pae) {
        final SessionFactory factory = SessionFactory.newFactory();
        final MediaSegment testSegment = factory.createMediaSegment();
        testSegment.setStartValue(1000.0f);
        testSegment.setEndValue(3000.0f);
        testSegment.setUnitType(MediaUnit.Millisecond);

        JComponent source = (JComponent) pae.getActionEvent().getSource();
        final SegmentEditorPopup popup = new SegmentEditorPopup(transcriptEditor.getMediaModel(), testSegment);
        popup.showPopup(source, 0, source.getHeight());
    }

    // region Getters and Setters

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

    //endregion Getters and Setters
}
