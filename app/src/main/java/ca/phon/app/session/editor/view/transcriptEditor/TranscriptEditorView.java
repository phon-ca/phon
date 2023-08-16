package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.session.editor.*;
import ca.phon.plugin.PluginManager;
import ca.phon.session.Session;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TranscriptEditorView extends EditorView {

    public final static String VIEW_NAME = "Transcript Editor";
    public final static String VIEW_ICON = "blank";
    private final TranscriptEditor transcriptEditor;

    public TranscriptEditorView(SessionEditor editor) {
        super(editor);
        this.transcriptEditor = new TranscriptEditor(editor.getSession());
        initUI();
        registerEditorActions();
    }

    private void initUI() {
        JScrollPane scrollPane = new JScrollPane(transcriptEditor);
        scrollPane.setRowHeaderView(new TranscriptEditorRowHeader(transcriptEditor));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            scrollPane.getRowHeader().setViewPosition(new Point(0, e.getValue()));
        });
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        transcriptEditor.getTranscriptDocument().setTierLabelFactory(this::createTierLabel);
    }

    private JComponent createTierLabel(String tierName) {
        JLabel tierLabel = new JLabel(tierName + ":");
        var labelFont = new Font(tierLabel.getFont().getFontName(), tierLabel.getFont().getStyle(), 12);
        tierLabel.setFont(labelFont);
        tierLabel.setAlignmentY(.8f);
        tierLabel.setMaximumSize(new Dimension(150, tierLabel.getPreferredSize().height));
        tierLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        EmptyBorder tierLabelPadding = new EmptyBorder(0,8,0,8);
        tierLabel.setBorder(tierLabelPadding);
        tierLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                transcriptEditor.getTranscriptDocument().setTierItemViewLocked(tierName, true);
                System.out.println("Testing");
                createTierLabelPopup(tierLabel, e);
            }
        });

        return tierLabel;
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
        return new JMenu();
    }

    private void registerEditorActions() {
        getEditor().getEventManager().registerActionForEvent(EditorEventType.SessionChanged, this::onSessionChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
        getEditor().getEventManager().registerActionForEvent(EditorEventType.TierViewChanged, this::onTierViewChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
    }

    private void onSessionChanged(EditorEvent<Session> editorEvent) {

    }

    private void onTierViewChanged(EditorEvent<EditorEventType.TierViewChangedData> editorEvent) {
        transcriptEditor.getTranscriptDocument().reload();
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
}
