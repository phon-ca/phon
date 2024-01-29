package ca.phon.app.session.editor.view.transcript;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.actions.*;
import ca.phon.app.session.editor.autotranscribe.AutoTranscribeAction;
import ca.phon.app.session.editor.search.FindAndReplacePanel;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.mediaPlayer.MediaPlayerEditorView;
import ca.phon.app.session.editor.view.participants.ParticipantsView;
import ca.phon.app.session.editor.view.speechAnalysis.SpeechAnalysisEditorView;
import ca.phon.app.session.editor.view.tierManagement.TierManagementView;
import ca.phon.app.session.editor.actions.NewTierAction;
import ca.phon.app.session.editor.view.timeline.TimelineView;
import ca.phon.app.session.editor.view.transcript.actions.*;
import ca.phon.app.session.editor.view.transcript.extensions.*;
import ca.phon.plugin.PluginManager;
import ca.phon.session.*;
import ca.phon.session.io.SessionOutputFactory;
import ca.phon.ui.CalloutWindow;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.Language;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

/**
 * The {@link EditorView} that uses the {@link TranscriptEditor}
 * */
public class TranscriptView extends EditorView {

    public final static String VIEW_NAME = "Transcript";
    public final static String VIEW_ICON = IconManager.GoogleMaterialDesignIconsFontName + ":description";

    private final TranscriptEditor transcriptEditor;
    private TranscriptScrollPane transcriptScrollPane;
    private FindAndReplacePanel findAndReplacePanel;
    private JPanel centerPanel;

    /* Preferences stuff */

    public final static String FONT_SIZE_DELTA_PROP = TranscriptView.class.getName() + ".fontSizeDelta";
    public final static float DEFAULT_FONT_SIZE_DELTA = 0.0f;

    /* State */

    public float fontSizeDelta = PrefHelper.getFloat(FONT_SIZE_DELTA_PROP, DEFAULT_FONT_SIZE_DELTA);
    private boolean findAndReplaceVisible = false;

    /**
     * Constructor
     * */
    public TranscriptView(SessionEditor editor) {
        super(editor);
        this.transcriptEditor = new TranscriptEditor(
            editor.getDataModel(),
            editor.getSelectionModel(),
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

        addPropertyChangeListener("fontSizeDelta", e -> {
            PrefHelper.getUserPreferences().putFloat(FONT_SIZE_DELTA_PROP, getFontSizeDelta());
            transcriptEditor.repaint();
        });


        InputMap inputMap = transcriptEditor.getInputMap();
        ActionMap actionMap = transcriptEditor.getActionMap();

        KeyStroke save = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        inputMap.put(save, "save");
        PhonUIAction<Void> saveAct = PhonUIAction.runnable(() -> {
            transcriptEditor.saveCurrentLine();
            SwingUtilities.invokeLater(() -> new SaveSessionAction(editor).hookableActionPerformed(null));
        });
        actionMap.put("save", saveAct);

        KeyStroke saveAs = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK);
        inputMap.put(saveAs, "saveAs");
        PhonUIAction<Void> saveAsAct = PhonUIAction.runnable(() -> {
            transcriptEditor.saveCurrentLine();
            SwingUtilities.invokeLater(() -> new SaveAsAction(editor, new SessionOutputFactory().availableSessionIOs().get(0)).hookableActionPerformed(null));
        });
        actionMap.put("saveAs", saveAsAct);
    }

    /**
     * Sets up the UI
     * */
    private void initUI() {
        setLayout(new BorderLayout());
        centerPanel = new JPanel(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);

        transcriptScrollPane = new TranscriptScrollPane(transcriptEditor);
        transcriptScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        centerPanel.add(transcriptScrollPane, BorderLayout.CENTER);
        centerPanel.add(new TranscriptStatusBar(transcriptEditor), BorderLayout.SOUTH);

//        JToolBar toolbar = new JToolBar();
//        add(toolbar, BorderLayout.NORTH);
//
//        var blankIcon = IconManager.getInstance().getIcon("blank", IconSize.SMALL);
//
//        JButton sessionInfoMenuButton = new JButton();
//        PhonUIAction<Void> sessionInfoMenuAct = PhonUIAction.eventConsumer(this::showSessionInfoMenu);
//        sessionInfoMenuAct.putValue(PhonUIAction.NAME, "Session Information");
//        sessionInfoMenuButton.setAction(sessionInfoMenuAct);
//        sessionInfoMenuButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        toolbar.add(sessionInfoMenuButton);
//
//        JButton mediaMenuButton = new JButton();
//        PhonUIAction<Void> mediaMenuAct = PhonUIAction.eventConsumer(this::showMediaMenu);
//        mediaMenuAct.putValue(PhonUIAction.NAME, "Media");
//        mediaMenuButton.setAction(mediaMenuAct);
//        mediaMenuButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        toolbar.add(mediaMenuButton);
//
//        JButton participantsMenuButton = new JButton();
//        PhonUIAction<Void> participantsMenuAct = PhonUIAction.eventConsumer(this::showParticipantsMenu);
//        participantsMenuAct.putValue(PhonUIAction.NAME, "Participants");
//        participantsMenuButton.setAction(participantsMenuAct);
//        participantsMenuButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        toolbar.add(participantsMenuButton);
//
//        JButton transcriptMenuButton = new JButton();
//        PhonUIAction<Void> transcriptMenuAct = PhonUIAction.eventConsumer(this::showTranscriptMenu);
//        transcriptMenuAct.putValue(PhonUIAction.NAME, "Transcript");
//        transcriptMenuButton.setAction(transcriptMenuAct);
//        transcriptMenuButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        toolbar.add(transcriptMenuButton);
//
//        JButton tiersMenuButton = new JButton();
//        PhonUIAction<Void> tiersMenuAct = PhonUIAction.eventConsumer(this::showTiersMenu);
//        tiersMenuAct.putValue(PhonUIAction.NAME, "Tiers");
//        tiersMenuAct.putValue(PhonUIAction.SMALL_ICON, new DropDownIcon(blankIcon, SwingConstants.BOTTOM));
//        tiersMenuAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
//        tiersMenuAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);
//        tiersMenuButton.setAction(tiersMenuAct);
//        tiersMenuButton.setHorizontalTextPosition(SwingConstants.LEFT);
//        tiersMenuButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        toolbar.add(tiersMenuButton);
//
//
//        JButton findReplaceButton = new JButton();
//        findReplaceButton.setAction(new FindAndReplaceAction(getEditor()));
//        findReplaceButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        toolbar.add(findReplaceButton);
//
//
//        JButton fontScaleMenuButton = new JButton();
//        DropDownIcon fontScaleIcon = new DropDownIcon(
//                IconManager.getInstance().getIcon("apps/preferences-desktop-font", IconSize.SMALL),
//                2,
//                SwingConstants.BOTTOM
//        );
//        PhonUIAction<Void> fontScaleMenuAct = PhonUIAction.eventConsumer(this::showFontScaleMenu, null);
//        fontScaleMenuAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show font scale menu");
//        fontScaleMenuAct.putValue(PhonUIAction.SMALL_ICON, fontScaleIcon);
//        fontScaleMenuAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
//        fontScaleMenuAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);
//        fontScaleMenuButton.setAction(fontScaleMenuAct);
//        fontScaleMenuButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        toolbar.add(fontScaleMenuButton);
    }

    /**
     * Shows the font scale menu
     *
     * @param pae the event from the action that called the function
     **/
    private void showFontScaleMenu(PhonActionEvent<Void> pae) {
        JPanel fontScaleMenu = new JPanel(new BorderLayout());
        fontScaleMenu.setOpaque(false);
        fontScaleMenu.setBorder(new EmptyBorder(0,8,0,8));

        // Setup font scale slider
        final JLabel smallLbl = new JLabel("A");
        smallLbl.setFont(getFont().deriveFont(FontPreferences.getDefaultFontSize()));
        smallLbl.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel largeLbl = new JLabel("A");
        largeLbl.setFont(getFont().deriveFont(FontPreferences.getDefaultFontSize()*2));
        largeLbl.setHorizontalAlignment(SwingConstants.CENTER);

        final JSlider scaleSlider = new JSlider(-8, 24);
        scaleSlider.setValue((int)getFontSizeDelta());
        scaleSlider.setMajorTickSpacing(8);
        scaleSlider.setMinorTickSpacing(2);
        scaleSlider.setSnapToTicks(true);
        scaleSlider.setPaintTicks(true);
        scaleSlider.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (fontSizeDelta != scaleSlider.getValue()) {
                    setFontSizeDelta(scaleSlider.getValue());
                    transcriptEditor.getTranscriptDocument().reload();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (fontSizeDelta != scaleSlider.getValue()) {
                    setFontSizeDelta(scaleSlider.getValue());
                    transcriptEditor.getTranscriptDocument().reload();
                }
            }
        });

        JComponent fontComp = new JPanel(new HorizontalLayout());
        fontComp.setOpaque(false);
        fontComp.add(smallLbl);
        fontComp.add(scaleSlider);
        fontComp.add(largeLbl);

        fontScaleMenu.add(fontComp, BorderLayout.CENTER);

        JButton defaultSizeButton = new JButton();
        final PhonUIAction<Void> useDefaultFontSizeAct = PhonUIAction.runnable(() -> {
            scaleSlider.setValue(0);
            if (fontSizeDelta != scaleSlider.getValue()) {
                setFontSizeDelta(0);
                transcriptEditor.getTranscriptDocument().reload();
            }
        });
        useDefaultFontSizeAct.putValue(PhonUIAction.NAME, "Use default font size");
        useDefaultFontSizeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Reset font size");
        defaultSizeButton.setAction(useDefaultFontSizeAct);
        fontScaleMenu.add(defaultSizeButton, BorderLayout.SOUTH);

        JComponent source = (JComponent) pae.getActionEvent().getSource();
        Point point = source.getLocationOnScreen();
        point.translate(source.getWidth() / 2, source.getHeight());

        CalloutWindow.showCallout(
            CommonModuleFrame.getCurrentFrame(),
            fontScaleMenu,
            SwingConstants.TOP,
            SwingConstants.CENTER,
            point
        );
    }

    /**
     * Shows the session info menu
     *
     * @param pae the event from the action that called the function
     * */
    private void showSessionInfoMenu(PhonActionEvent<Void> pae) {
        JPopupMenu menu = new JPopupMenu();
        MenuBuilder menuBuilder = new MenuBuilder(menu);
        setupSessionInformationMenu(menuBuilder);

        JComponent source = (JComponent) pae.getActionEvent().getSource();
        menu.show(
            source,
            0,
            source.getHeight()
        );
    }

    /**
     * Sets up the session info menu with the given {@link MenuBuilder}
     *
     * @param menuBuilder the builder used to build the menu
     * */
    private void setupSessionInformationMenu(MenuBuilder menuBuilder) {
        JMenuItem toggleHeadersItem = new JMenuItem();
        toggleHeadersItem.setAction(new ToggleHeadersVisibleAction(getEditor(), TranscriptView.this));
        menuBuilder.addItem(".", toggleHeadersItem);

        JMenuItem setDateItem = new JMenuItem();
        PhonUIAction<Void> setDateAct = PhonUIAction.runnable(() -> {
            if (getEditor().getSession().getDate() == null) {
                SessionDateEdit edit = new SessionDateEdit(getEditor(), LocalDate.now(), null);
                getEditor().getUndoSupport().postEdit(edit);
            }
            SwingUtilities.invokeLater(() -> {
                int dateHeaderEnd = getTranscriptEditor().getTranscriptDocument().getGenericEnd("Date")-1;
                if (dateHeaderEnd > -1) {
                    getTranscriptEditor().setCaretPosition(dateHeaderEnd);
                    getTranscriptEditor().requestFocus();
                }
            });
        });
        setDateAct.putValue(PhonUIAction.NAME, "Set date");
        setDateItem.setAction(setDateAct);
        menuBuilder.addItem(".", setDateItem);


        JMenu languagesMenu = menuBuilder.addMenu(".", "Languages");

        List<Language> sessionLanguages = getEditor().getSession().getLanguages();

        boolean sessionAlreadyHasLanguages = !sessionLanguages.isEmpty();
        JMenuItem addLanguageItem = new JMenuItem();
        PhonUIAction<Void> addLanguageAct = PhonUIAction.runnable(() -> {
            if (!sessionAlreadyHasLanguages) {
                getTranscriptEditor().getTranscriptDocument().putDocumentProperty("forceShowLanguageHeader", true);
                getTranscriptEditor().getTranscriptDocument().reload();
            }
            SwingUtilities.invokeLater(() -> {
                int end = getTranscriptEditor().getTranscriptDocument().getGenericEnd("Languages")-1;
                if (end > -1) {
                    getTranscriptEditor().setCaretPosition(end);
                    getTranscriptEditor().requestFocus();
                }
            });
        });
        addLanguageAct.putValue(PhonUIAction.NAME, (sessionAlreadyHasLanguages ? "Add" : "Set") + " language");
        addLanguageItem.setAction(addLanguageAct);
        languagesMenu.add(addLanguageItem);

        if (sessionAlreadyHasLanguages) {
            JMenu removeLanguageSubmenu = new JMenu("Remove language");

            for (Language language : sessionLanguages) {
                JMenuItem removeLanguageItem = new JMenuItem();
                PhonUIAction<Void> removeLanguageAct = PhonUIAction.runnable(() -> {
                    var newLangList = sessionLanguages.stream().filter(item -> item != language).toList();
                    SessionLanguageEdit edit = new SessionLanguageEdit(getEditor(), newLangList);
                    getEditor().getUndoSupport().postEdit(edit);
                    SwingUtilities.invokeLater(() -> getTranscriptEditor().getTranscriptDocument().reload());
                });
                removeLanguageAct.putValue(PhonUIAction.NAME, "Remove " + language.getPrimaryLanguage().getName());
                removeLanguageItem.setAction(removeLanguageAct);
                removeLanguageSubmenu.add(removeLanguageItem);
            }

            languagesMenu.add(removeLanguageSubmenu);
        }

        JMenuItem viewMetadataItem = new JMenuItem();
        PhonUIAction<Void> viewMetadataAct = PhonUIAction.runnable(() -> {
            MetadataDialog metadataDialog = new MetadataDialog(CommonModuleFrame.getCurrentFrame());
            metadataDialog.pack();
            metadataDialog.setVisible(true);
        });
        viewMetadataAct.putValue(PhonUIAction.NAME, "View metadata");
        viewMetadataItem.setAction(viewMetadataAct);
        menuBuilder.addItem(".", viewMetadataItem);

        JMenuItem showSessionInfoViewItem = new JMenuItem();
        PhonUIAction<Void> showSessionInfoViewAct = PhonUIAction.runnable(
            () -> getEditor().getViewModel().showView(ParticipantsView.VIEW_NAME)
        );
        showSessionInfoViewAct.putValue(PhonUIAction.NAME, "Show Session Information view");
        showSessionInfoViewItem.setAction(showSessionInfoViewAct);
        menuBuilder.addItem(".", showSessionInfoViewItem);
    }

    /**
     * Shows the media menu
     *
     * @param pae the event from the action that called the function
     * */
    private void showMediaMenu(PhonActionEvent<Void> pae) {
        JPopupMenu menu = new JPopupMenu();
        MenuBuilder menuBuilder = new MenuBuilder(menu);
        setupMediaMenu(menuBuilder);

        JComponent source = (JComponent) pae.getActionEvent().getSource();
        menu.show(
            source,
            0,
            source.getHeight()
        );
    }

    /**
     * Sets up the media menu with the given {@link MenuBuilder}
     *
     * @param menuBuilder the builder used to build the menu
     * */
    private void setupMediaMenu(MenuBuilder menuBuilder) {
        JMenuItem browseMediaItem = new JMenuItem();
        browseMediaItem.setAction(new AssignMediaAction(getEditor()));
        menuBuilder.addItem(".", browseMediaItem);

        // TODO: Wait for Greg to get this part done
        JMenuItem showMediaPlayerItem = new JMenuItem();
        PhonUIAction<Void> showMediaPlayerAct = PhonUIAction.runnable(() -> {System.out.println("Show media player");});
        showMediaPlayerAct.putValue(PhonUIAction.NAME, "Show media player");
        showMediaPlayerItem.setAction(showMediaPlayerAct);
        menuBuilder.addItem(".", showMediaPlayerItem);

        JMenuItem showTimelineViewItem = new JMenuItem();
        PhonUIAction<Void> showTimelineViewAct = PhonUIAction.runnable(() -> getEditor().getViewModel().showView(TimelineView.VIEW_NAME));
        showTimelineViewAct.putValue(PhonUIAction.NAME, "Show Timeline view");
        showTimelineViewItem.setAction(showTimelineViewAct);
        menuBuilder.addItem(".", showTimelineViewItem);

        JMenuItem showMediaPlayerViewItem = new JMenuItem();
        PhonUIAction<Void> showMediaPlayerViewAct = PhonUIAction.runnable(() -> getEditor().getViewModel().showView(MediaPlayerEditorView.VIEW_NAME));
        showMediaPlayerViewAct.putValue(PhonUIAction.NAME, "Show Media Player view");
        showMediaPlayerViewItem.setAction(showMediaPlayerViewAct);
        menuBuilder.addItem(".", showMediaPlayerViewItem);

        JMenuItem showSpeechAnalysisViewItem = new JMenuItem();
        PhonUIAction<Void> showSpeechAnalysisViewAct = PhonUIAction.runnable(() -> getEditor().getViewModel().showView(SpeechAnalysisEditorView.VIEW_NAME));
        showSpeechAnalysisViewAct.putValue(PhonUIAction.NAME, "Show Speech Analysis view");
        showSpeechAnalysisViewItem.setAction(showSpeechAnalysisViewAct);
        menuBuilder.addItem(".", showSpeechAnalysisViewItem);
    }

    /**
     * Shows the participants menu
     *
     * @param pae the event from the action that called the function
     * */
    private void showParticipantsMenu(PhonActionEvent<Void> pae) {
        JPopupMenu menu = new JPopupMenu();
        MenuBuilder menuBuilder = new MenuBuilder(menu);
        setupParticipantsMenu(menuBuilder);

        JComponent source = (JComponent) pae.getActionEvent().getSource();
        menu.show(
            source,
            0,
            source.getHeight()
        );
    }

    /**
     * Sets up the participants menu with the given {@link MenuBuilder}
     *
     * @param menuBuilder the builder used to build the menu
     * */
    private void setupParticipantsMenu(MenuBuilder menuBuilder) {
        JMenuItem addParticipantItem = new JMenuItem();
        addParticipantItem.setAction(new NewParticipantAction(getEditor()));
        menuBuilder.addItem(".", addParticipantItem);

        menuBuilder.addSeparator(".", "");

        for (Participant participant : getEditor().getSession().getParticipants()) {
            JMenu participantSubmenu = menuBuilder.addMenu(".", participant.toString());

            JMenuItem editParticipantItem = new JMenuItem();
            editParticipantItem.setAction(new EditParticipantAction(getEditor(), participant));
            participantSubmenu.add(editParticipantItem);

            JMenuItem removeParticipantItem = new JMenuItem();
            removeParticipantItem.setAction(new DeleteParticipantAction(getEditor(), participant));
            participantSubmenu.add(removeParticipantItem);
        }

        JMenuItem showSessionInfoViewItem = new JMenuItem();
        PhonUIAction<Void> showSessionInfoViewAct = PhonUIAction.runnable(
                () -> getEditor().getViewModel().showView(ParticipantsView.VIEW_NAME)
        );
        showSessionInfoViewAct.putValue(PhonUIAction.NAME, "Show Session Information view");
        showSessionInfoViewItem.setAction(showSessionInfoViewAct);
        menuBuilder.addItem(".", showSessionInfoViewItem);
    }

    /**
     * Shows the transcript menu
     *
     * @param pae the event from the action that called the function
     * */
    private void showTranscriptMenu(PhonActionEvent<Void> pae) {
        JPopupMenu menu = new JPopupMenu();
        MenuBuilder menuBuilder = new MenuBuilder(menu);
        setupTranscriptMenu(menuBuilder);

        JComponent source = (JComponent) pae.getActionEvent().getSource();
        menu.show(
            source,
            0,
            source.getHeight()
        );
    }

    /**
     * Sets up the transcript menu with the given {@link MenuBuilder}
     *
     * @param menuBuilder the builder used to build the menu
     * */
    private void setupTranscriptMenu(MenuBuilder menuBuilder) {

        TranscriptLocation transcriptLocation = getTranscriptEditor().getCurrentSessionLocation();
        int currentTranscriptElementIndex = transcriptLocation == null ? -1 : transcriptLocation.elementIndex();
        boolean inHeaders = currentTranscriptElementIndex < 0;


        if (!inHeaders) {
            JMenuItem insertRecordAboveItem = new JMenuItem();
            PhonUIAction<Void> insertRecordAboveAct = PhonUIAction.runnable(() -> {
                final AddRecordEdit edit = new AddRecordEdit(getEditor(), SessionFactory.newFactory().createRecord(), currentTranscriptElementIndex);
                getEditor().getUndoSupport().postEdit(edit);
            });
            insertRecordAboveAct.putValue(PhonUIAction.NAME, "Insert record above");
            insertRecordAboveItem.setAction(insertRecordAboveAct);
            menuBuilder.addItem(".", insertRecordAboveItem);
        }

        JMenuItem insertRecordBelowItem = new JMenuItem();
        PhonUIAction<Void> insertRecordBelowAct = PhonUIAction.runnable(() -> {
            Transcript transcript = getEditor().getSession().getTranscript();
            final AddRecordEdit edit = new AddRecordEdit(getEditor(), SessionFactory.newFactory().createRecord(), currentTranscriptElementIndex+1);
            getEditor().getUndoSupport().postEdit(edit);
        });
        insertRecordBelowAct.putValue(PhonUIAction.NAME, "Insert record below");
        insertRecordBelowItem.setAction(insertRecordBelowAct);
        menuBuilder.addItem(".", insertRecordBelowItem);

        menuBuilder.addSeparator(".", "");

        if (!inHeaders) {
            JMenuItem insertCommentAboveItem = new JMenuItem();
            PhonUIAction<Void> insertCommentAboveAct = PhonUIAction.runnable(() -> {
                final AddTranscriptElementEdit edit = new AddTranscriptElementEdit(
                    getEditor().getSession(),
                    getEditor().getEventManager(),
                    new Transcript.Element(SessionFactory.newFactory().createComment()),
                    currentTranscriptElementIndex
                );
                getEditor().getUndoSupport().postEdit(edit);
            });
            insertCommentAboveAct.putValue(PhonUIAction.NAME, "Insert comment above");
            insertCommentAboveItem.setAction(insertCommentAboveAct);
            menuBuilder.addItem(".", insertCommentAboveItem);
        }

        JMenuItem insertCommentBelowItem = new JMenuItem();
        PhonUIAction<Void> insertCommentBelowAct = PhonUIAction.runnable(() -> {
            final AddTranscriptElementEdit edit = new AddTranscriptElementEdit(
                getEditor().getSession(),
                getEditor().getEventManager(),
                new Transcript.Element(SessionFactory.newFactory().createComment()),
                currentTranscriptElementIndex+1
            );
            getEditor().getUndoSupport().postEdit(edit);
        });
        insertCommentBelowAct.putValue(PhonUIAction.NAME, "Insert comment below");
        insertCommentBelowItem.setAction(insertCommentBelowAct);
        menuBuilder.addItem(".", insertCommentBelowItem);


        menuBuilder.addSeparator(".", "");


        if (!inHeaders) {
            JMenuItem insertGemAboveItem = new JMenuItem();
            PhonUIAction<Void> insertGemAboveAct = PhonUIAction.runnable(() -> {
                final AddTranscriptElementEdit edit = new AddTranscriptElementEdit(
                    getEditor().getSession(),
                    getEditor().getEventManager(),
                    new Transcript.Element(SessionFactory.newFactory().createGem()),
                    currentTranscriptElementIndex
                );
                getEditor().getUndoSupport().postEdit(edit);
            });
            insertGemAboveAct.putValue(PhonUIAction.NAME, "Insert gem above");
            insertGemAboveItem.setAction(insertGemAboveAct);
            menuBuilder.addItem(".", insertGemAboveItem);
        }

        JMenuItem insertGemBelowItem = new JMenuItem();
        PhonUIAction<Void> insertGemBelowAct = PhonUIAction.runnable(() -> {
            final AddTranscriptElementEdit edit = new AddTranscriptElementEdit(
                getEditor().getSession(),
                getEditor().getEventManager(),
                new Transcript.Element(SessionFactory.newFactory().createGem()),
                currentTranscriptElementIndex+1
            );
            getEditor().getUndoSupport().postEdit(edit);
        });
        insertGemBelowAct.putValue(PhonUIAction.NAME, "Insert gem below");
        insertGemBelowItem.setAction(insertGemBelowAct);
        menuBuilder.addItem(".", insertGemBelowItem);


        menuBuilder.addSeparator(".", "");


        JMenuItem autoTranscribeItem = new JMenuItem();
        autoTranscribeItem.setAction(new AutoTranscribeAction(
            getEditor().getProject(),
            getEditor().getSession(),
            getEditor().getEventManager(),
            getEditor().getUndoSupport(),
            getEditor().getDataModel().getTranscriber()
        ));
        menuBuilder.addItem(".", autoTranscribeItem);
    }

    /**
     * Shows the tiers menu
     *
     * @param pae the event from the action that called the function
     * */
    private void showTiersMenu(PhonActionEvent<Void> pae) {
        JPopupMenu menu = new JPopupMenu();
        MenuBuilder menuBuilder = new MenuBuilder(menu);
        setupTiersMenu(menuBuilder);

        JComponent source = (JComponent) pae.getActionEvent().getSource();
        menu.show(
            source,
            0,
            source.getHeight()
        );
    }

    /**
     * Sets up the tiers menu with the given {@link MenuBuilder}
     *
     * @param menuBuilder the builder used to build the menu
     * */
    private void setupTiersMenu(MenuBuilder menuBuilder) {
        JMenu addTierSubmenu = menuBuilder.addMenu(".", "Add tier");

        List<UserTierType> availableUserTierTypes = new ArrayList<>(List.of(UserTierType.values()));
        availableUserTierTypes.remove(UserTierType.Wor);
        availableUserTierTypes.remove(UserTierType.Mor);
        availableUserTierTypes.remove(UserTierType.Trn);
        availableUserTierTypes.remove(UserTierType.Gra);
        availableUserTierTypes.remove(UserTierType.Grt);
        for (UserTierType userTierType : availableUserTierTypes) {
            JMenuItem addTierItem = new JMenuItem();
            PhonUIAction<Void> addTierAct = PhonUIAction.runnable(() -> {
                TierDescription td = SessionFactory.newFactory().createTierDescription(userTierType);
                TierViewItem tvi = SessionFactory.newFactory().createTierViewItem(td.getName());

                AddTierEdit edit = new AddTierEdit(getEditor(), td, tvi);
                getEditor().getUndoSupport().postEdit(edit);
            });
            addTierAct.putValue(PhonUIAction.NAME, userTierType.name());
            addTierItem.setAction(addTierAct);
            addTierSubmenu.add(addTierItem);
        }

        addTierSubmenu.add(new JSeparator());

        JMenuItem addCustomTierItem = new JMenuItem();
        addCustomTierItem.setAction(new NewTierAction(getEditor()));
        addTierSubmenu.add(addCustomTierItem);
        menuBuilder.addSeparator(".", "");

        var extPts = PluginManager.getInstance().getExtensionPoints(TierLabelMenuHandler.class);
        var tierView = getEditor().getSession().getTierView();
        for (TierViewItem item : tierView) {

            MenuBuilder tviMenuBuilder = new MenuBuilder(menuBuilder.addMenu(".", item.getTierName()));


            for (var extPt : extPts) {
                var menuHandler = extPt.getFactory().createObject();
                menuHandler.addMenuItems(
                    tviMenuBuilder,
                    getEditor().getSession(),
                    getEditor().getEventManager(),
                    getEditor().getUndoSupport(),
                    item
                );
            }

        }


        menuBuilder.addSeparator(".", "");


        JMenuItem toggleAlignmentVisibleItem = new JMenuItem();
        toggleAlignmentVisibleItem.setAction(new ToggleAlignmentVisibleAction(getEditor(), TranscriptView.this));
        menuBuilder.addItem(".", toggleAlignmentVisibleItem);

        JMenuItem toggleAlignmentComponentItem = new JMenuItem();
        toggleAlignmentComponentItem.setAction(new ToggleAlignmentIsComponentAction(getEditor(), TranscriptView.this));
        menuBuilder.addItem(".", toggleAlignmentComponentItem);

        menuBuilder.addSeparator(".", "");

        JMenuItem toggleSyllabificationVisibleItem = new JMenuItem();
        toggleSyllabificationVisibleItem.setAction(new ToggleSyllabificationVisibleAction(getEditor(), TranscriptView.this));
        menuBuilder.addItem(".", toggleSyllabificationVisibleItem);

        JMenuItem toggleSyllabificationComponentItem = new JMenuItem();
        toggleSyllabificationComponentItem.setAction(new ToggleSyllabificationIsComponent(getEditor(), TranscriptView.this));
        menuBuilder.addItem(".", toggleSyllabificationComponentItem);

        menuBuilder.addSeparator(".", "");

        JMenuItem toggleBlindTiersItem = new JMenuItem();
        toggleBlindTiersItem.setAction(new ToggleValidationModeAction(getEditor(), TranscriptView.this));
        menuBuilder.addItem(".", toggleBlindTiersItem);

        JMenuItem toggleChatTierNamesItem = new JMenuItem();
        PhonUIAction<Void> toggleChatTierNamesAct = PhonUIAction.runnable(this::toggleChatTierNamesShown);
        toggleChatTierNamesAct.putValue(PhonUIAction.NAME, "Toggle chat tier names");
        toggleChatTierNamesItem.setAction(toggleChatTierNamesAct);
        menuBuilder.addItem(".", toggleChatTierNamesItem);

        JMenuItem showTierManagementViewItem = new JMenuItem();
        PhonUIAction<Void> showTierManagementViewAct = PhonUIAction.runnable(() -> getEditor().getViewModel().showView(TierManagementView.VIEW_NAME));
        showTierManagementViewAct.putValue(PhonUIAction.NAME, "Show Tier Management view");
        showTierManagementViewItem.setAction(showTierManagementViewAct);
        menuBuilder.addItem(".", showTierManagementViewItem);
    }

    // region Getters and Setters

    @Override
    public String getName() {
        return VIEW_NAME;
    }

    @Override
    public ImageIcon getIcon() {
        final String[] iconData = VIEW_ICON.split(":");
        return IconManager.getInstance().getFontIcon(iconData[0], iconData[1], IconSize.MEDIUM, Color.darkGray);
    }

    @Override
    public JMenu getMenu() {
        final JMenu retVal = new JMenu();

        MenuBuilder menuBuilder = new MenuBuilder(retVal);

        setupSessionInformationMenu(new MenuBuilder(menuBuilder.addMenu(".", "Session information")));
        setupMediaMenu(new MenuBuilder(menuBuilder.addMenu(".", "Media")));
        setupParticipantsMenu(new MenuBuilder(menuBuilder.addMenu(".", "Participants")));
        setupTranscriptMenu(new MenuBuilder(menuBuilder.addMenu(".", "Transcript")));
        setupTiersMenu(new MenuBuilder(menuBuilder.addMenu(".", "Tiers")));

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
        return (boolean) transcriptEditor.getTranscriptDocument().getDocumentPropertyOrDefault(
            SyllabificationExtension.SYLLABIFICATION_IS_VISIBLE,
            SyllabificationExtension.SYLLABIFICATION_IS_VISIBLE_DEFAULT
        );
    }

    public void toggleSyllabificationVisible() {
        transcriptEditor.getTranscriptDocument().putDocumentProperty(
            SyllabificationExtension.SYLLABIFICATION_IS_VISIBLE,
            !isSyllabificationVisible()
        );
    }

    public boolean isSyllabificationComponent() {
        return (boolean) transcriptEditor.getTranscriptDocument().getDocumentPropertyOrDefault(
            SyllabificationExtension.SYLLABIFICATION_IS_COMPONENT,
            SyllabificationExtension.SYLLABIFICATION_IS_COMPONENT_DEFAULT
        );
    }

    public void toggleSyllabificationIsComponent() {
        transcriptEditor.getTranscriptDocument().putDocumentProperty(
            SyllabificationExtension.SYLLABIFICATION_IS_COMPONENT,
            !isSyllabificationComponent()
        );
    }

    public boolean isAlignmentVisible() {
        return (boolean) transcriptEditor.getTranscriptDocument().getDocumentPropertyOrDefault(
            AlignmentExtension.ALIGNMENT_IS_VISIBLE,
            AlignmentExtension.ALIGNMENT_IS_VISIBLE_DEFAULT
        );
    }

    public void toggleAlignmentVisible() {
        transcriptEditor.getTranscriptDocument().putDocumentProperty(
            AlignmentExtension.ALIGNMENT_IS_VISIBLE,
            !isAlignmentVisible()
        );
    }

    public boolean isAlignmentComponent() {
        return (boolean) transcriptEditor.getTranscriptDocument().getDocumentPropertyOrDefault(
            AlignmentExtension.ALIGNMENT_IS_COMPONENT,
            AlignmentExtension.ALIGNMENT_IS_COMPONENT_DEFAULT
        );
    }

    public void toggleAlignmentIsComponent() {
        transcriptEditor.getTranscriptDocument().putDocumentProperty(
            AlignmentExtension.ALIGNMENT_IS_COMPONENT,
            !isAlignmentComponent()
        );
    }

    public float getFontSizeDelta() {
        return fontSizeDelta;
    }

    public void setFontSizeDelta(float fontSizeDelta) {
        float oldVal = this.fontSizeDelta;
        this.fontSizeDelta = fontSizeDelta;
        firePropertyChange("fontSizeDelta", oldVal, fontSizeDelta);
    }

    public boolean isFindAndReplaceVisible() {
        return findAndReplaceVisible;
    }

    public void setFindAndReplaceVisible(boolean findAndReplaceVisible) {
        this.findAndReplaceVisible = findAndReplaceVisible;
        if (findAndReplaceVisible) {
            var editor = getEditor();
            findAndReplacePanel = new FindAndReplacePanel(
                editor.getDataModel(),
                editor.getSelectionModel(),
                editor.getEventManager(),
                editor.getUndoSupport()
            );
            centerPanel.add(findAndReplacePanel, BorderLayout.NORTH);
        }
        else {
            centerPanel.remove(findAndReplacePanel);
            findAndReplacePanel = null;
        }
        revalidate();
        repaint();
    }

    public TranscriptEditor getTranscriptEditor() {
        return transcriptEditor;
    }

    public boolean isValidationMode() {
        return (boolean) transcriptEditor.getTranscriptDocument().getDocumentPropertyOrDefault(
            BlindTranscriptionExtension.VALIDATION_MODE,
            BlindTranscriptionExtension.VALIDATION_MODE_DEFAULT
        );
    }

    public void toggleValidationMode() {
        transcriptEditor.getTranscriptDocument().putDocumentProperty(
            BlindTranscriptionExtension.VALIDATION_MODE,
            !isValidationMode()
        );
    }

    public boolean isHeadersVisible() {
        return (boolean) transcriptEditor.getTranscriptDocument().getDocumentPropertyOrDefault(
            HeaderTierExtension.HEADERS_VISIBLE,
            HeaderTierExtension.DEFAULT_HEADERS_VISIBLE
        );
    }

    public void toggleHeadersVisible() {
        transcriptEditor.getTranscriptDocument().putDocumentProperty(
            HeaderTierExtension.HEADERS_VISIBLE,
            !isHeadersVisible()
        );
    }

    public boolean isChatTierNamesShown() {
        return transcriptEditor.getTranscriptDocument().isChatTierNamesShown();
    }

    public void toggleChatTierNamesShown() {
        transcriptEditor.getTranscriptDocument().setChatTierNamesShown(!isChatTierNamesShown());
    }

    //endregion Getters and Setters

    /**
     * The dialog that shows the metadata for the session
     * */
    private class MetadataDialog extends JDialog {
        private final Map<String, String> data;
        private JXTable metadataTable;
        private DefaultTableModel metadataTableModel;

        /**
         * Constructor
         *
         * @param owner the window that this dialog was opened from
         * */
        public MetadataDialog(Frame owner) {
            super(owner, "Metadata");
            this.data = new HashMap<>();
            this.data.putAll(getTranscriptEditor().getSession().getMetadata());

            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {

                    if (!isMetadataChanged()) {
                        dispose();
                        return;
                    }

                    MessageDialogProperties props = new MessageDialogProperties();
                    props.setParentWindow(CommonModuleFrame.getCurrentFrame());
                    props.setHeader("Unsaved changes");
                    props.setOptions(MessageDialogProperties.yesNoCancelOptions);
                    props.setMessage("Do you want to save your changes?");
                    props.setRunAsync(true);
                    props.setListener(nativeDialogEvent -> {
                        int result = nativeDialogEvent.getDialogResult();
                        switch (result) {
                            case NativeDialogEvent.YES_OPTION -> {
                                saveChanges();
                                dispose();
                            }
                            case NativeDialogEvent.NO_OPTION -> dispose();
                        }
                    });

                    NativeDialogs.showMessageDialog(props);
                }
            });

            init();
        }

        /**
         * Sets up the UI
         * */
        private void init() {
            setLayout(new BorderLayout());

            // region Button Bar

            JButton okButton = new JButton();
            PhonUIAction<Void> okAction = PhonUIAction.runnable(this::saveChanges);
            okAction.putValue(PhonUIAction.NAME, "Ok");
            okButton.setAction(okAction);

            JButton cancelButton = new JButton();
            PhonUIAction<Void> cancelAction = PhonUIAction.runnable(this::dispose);
            cancelAction.putValue(PhonUIAction.NAME, "Cancel");
            cancelButton.setAction(cancelAction);

            JButton addRowButton = new JButton();
            PhonUIAction<Void> addRowAction = PhonUIAction.runnable(this::addRow);
            addRowAction.putValue(PhonUIAction.NAME, "Add row");
            addRowButton.setAction(addRowAction);

            JButton removeRowButton = new JButton();
            PhonUIAction<Void> removeRowAction = PhonUIAction.runnable(this::removeRow);
            removeRowAction.putValue(PhonUIAction.NAME, "Remove row");
            removeRowButton.setAction(removeRowAction);
            removeRowButton.setEnabled(false);

            add(ButtonBarBuilder.buildOkCancelBar(okButton, cancelButton, addRowButton, removeRowButton), BorderLayout.SOUTH);

            // endregion Button Bar

            // region Table

            String[] columnNames = { "Key", "Value" };

            List<String[]> metadataList = new ArrayList<>();
            for (var pair : data.entrySet()) {
                metadataList.add(new String[]{pair.getKey(), pair.getValue()});
            }
            metadataList.sort(Comparator.comparing(array -> array[0]));

            metadataTableModel = new DefaultTableModel(metadataList.toArray(String[][]::new), columnNames);
            metadataTable = new JXTable(metadataTableModel);
            metadataTable.setGridColor(Color.BLACK);
            add(new JScrollPane(metadataTable), BorderLayout.CENTER);

            metadataTable.getSelectionModel().addListSelectionListener(e ->
                removeRowButton.setEnabled(metadataTable.getSelectedRow() != -1)
            );

            // endregion Table
        }

        /**
         * Adds an empty row to the metadata table
         * */
        private void addRow() {
            metadataTableModel.addRow(new String[]{"",""});
        }

        /**
         * Removes the selected row from the metadata table
         * */
        private void removeRow() {
            int selectedRow = metadataTable.getSelectedRow();
            if (selectedRow == -1) return;
            metadataTableModel.removeRow(selectedRow);
            if (metadataTable.getRowCount() == 0) {
                metadataTable.getSelectionModel().clearSelection();
            }
            else {
                int newSelectedRow = Math.max(selectedRow - 1, 0);
                metadataTable.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
            }
        }

        /**
         * Saves the changes from the table to the sessions metadata
         * */
        private void saveChanges() {
            if (isMetadataChanged()) {
                EditSessionMetadata edit = new EditSessionMetadata(getEditor(), exportTableData());
                getEditor().getUndoSupport().postEdit(edit);
            }
            dispose();
        }

        /**
         * Checks if the metadata from the table is different from the sessions current metadata
         *
         * @return if there were any differences
         * */
        private boolean isMetadataChanged() {
            Map<String, String> existingMetadata = getTranscriptEditor().getSession().getMetadata();
            Map<String, String> dialogMetadata = exportTableData();

            if (existingMetadata.size() != dialogMetadata.size()) return true;

            for (var key : dialogMetadata.keySet()) {
                if (!existingMetadata.containsKey(key) || !existingMetadata.get(key).equals(dialogMetadata.get(key))) return true;
            }

            return false;
        }

        /**
         * Converts the data from the table into a map using the appropriate keys and values
         *
         * @return the converted map
         * */
        private Map<String, String> exportTableData() {
            Map<String, String> retVal = new HashMap<>();

            int rowCount = metadataTableModel.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                String key = (String) metadataTableModel.getValueAt(i, 0);
                String value = (String) metadataTableModel.getValueAt(i, 1);

                if (key != null && !key.isBlank() && value != null && !value.isBlank()) {
                    retVal.put(key, value);
                }
            }

            return retVal;
        }
    }
}
