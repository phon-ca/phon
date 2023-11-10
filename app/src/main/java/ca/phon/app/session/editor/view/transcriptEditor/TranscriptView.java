package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.actions.*;
import ca.phon.app.session.editor.search.FindAndReplacePanel;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
import ca.phon.app.session.editor.view.tier_management.actions.NewTierAction;
import ca.phon.app.session.editor.view.timeline.TimelineView;
import ca.phon.app.session.editor.view.transcriptEditor.actions.*;
import ca.phon.app.session.editor.view.transcriptEditor.extensions.AlignmentExtension;
import ca.phon.app.session.editor.view.transcriptEditor.extensions.BlindTranscriptionExtension;
import ca.phon.app.session.editor.view.transcriptEditor.extensions.HeaderTierExtension;
import ca.phon.app.session.editor.view.transcriptEditor.extensions.SyllabificationExtension;
import ca.phon.plugin.PluginManager;
import ca.phon.session.*;
import ca.phon.ui.CalloutWindow;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.DropDownIcon;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import org.jdesktop.swingx.HorizontalLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class TranscriptView extends EditorView {

    public final static String VIEW_NAME = "Transcript Editor";
    public final static String VIEW_ICON = "blank";
    private final TranscriptEditor transcriptEditor;
    private TranscriptScrollPane transcriptScrollPane;
    public final static String FONT_SIZE_DELTA_PROP = TranscriptView.class.getName() + ".fontSizeDelta";
    public final static float DEFAULT_FONT_SIZE_DELTA = 0.0f;
    public float fontSizeDelta = PrefHelper.getFloat(FONT_SIZE_DELTA_PROP, DEFAULT_FONT_SIZE_DELTA);
    private boolean findAndReplaceVisible = false;
    private FindAndReplacePanel findAndReplacePanel;
    private JPanel centerPanel;

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
    }

    private void initUI() {

        setLayout(new BorderLayout());
        centerPanel = new JPanel(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);

        transcriptScrollPane = new TranscriptScrollPane(transcriptEditor);
        transcriptScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        centerPanel.add(transcriptScrollPane, BorderLayout.CENTER);
        centerPanel.add(new TranscriptStatusBar(transcriptEditor), BorderLayout.SOUTH);

        JPanel toolbar = new JPanel(new HorizontalLayout());
        add(toolbar, BorderLayout.NORTH);

        var blankIcon = IconManager.getInstance().getIcon("blank", IconSize.SMALL);

        JButton sessionInfoMenuButton = new JButton();
        PhonUIAction<Void> sessionInfoMenuAct = PhonUIAction.eventConsumer(this::showSessionInfoMenu);
        sessionInfoMenuAct.putValue(PhonUIAction.NAME, "Session Information");
        sessionInfoMenuButton.setAction(sessionInfoMenuAct);
        toolbar.add(sessionInfoMenuButton);

        JButton mediaMenuButton = new JButton();
        PhonUIAction<Void> mediaMenuAct = PhonUIAction.eventConsumer(this::showMediaMenu);
        mediaMenuAct.putValue(PhonUIAction.NAME, "Media");
        mediaMenuButton.setAction(mediaMenuAct);
        toolbar.add(mediaMenuButton);

        JButton participantsMenuButton = new JButton();
        PhonUIAction<Void> participantsMenuAct = PhonUIAction.eventConsumer(this::showParticipantsMenu);
        participantsMenuAct.putValue(PhonUIAction.NAME, "Participants");
        participantsMenuButton.setAction(participantsMenuAct);
        toolbar.add(participantsMenuButton);

        JButton transcriptMenuButton = new JButton();
        PhonUIAction<Void> transcriptMenuAct = PhonUIAction.eventConsumer(this::showTranscriptMenu);
        transcriptMenuAct.putValue(PhonUIAction.NAME, "Transcript");
        transcriptMenuButton.setAction(transcriptMenuAct);
        toolbar.add(transcriptMenuButton);

        JButton tiersMenuButton = new JButton();
        PhonUIAction<Void> tiersMenuAct = PhonUIAction.eventConsumer(this::showTiersMenu);
        tiersMenuAct.putValue(PhonUIAction.NAME, "Tiers");
        tiersMenuAct.putValue(PhonUIAction.SMALL_ICON, new DropDownIcon(blankIcon, SwingConstants.BOTTOM));
        tiersMenuButton.setAction(tiersMenuAct);
        tiersMenuButton.setHorizontalTextPosition(SwingConstants.LEFT);
        toolbar.add(tiersMenuButton);


        JButton findReplaceButton = new JButton();
        findReplaceButton.setAction(new FindAndReplaceAction(getEditor()));
        toolbar.add(findReplaceButton);


        JButton fontScaleMenuButton = new JButton();
        DropDownIcon fontScaleIcon = new DropDownIcon(
                IconManager.getInstance().getIcon("apps/preferences-desktop-font", IconSize.SMALL),
                2,
                SwingConstants.BOTTOM
        );
        PhonUIAction<Void> fontScaleMenuAct = PhonUIAction.eventConsumer(this::showFontScaleMenu, null);
        fontScaleMenuAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show font scale menu");
        fontScaleMenuAct.putValue(PhonUIAction.SMALL_ICON, fontScaleIcon);
        fontScaleMenuAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
        fontScaleMenuAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);
        fontScaleMenuButton.setAction(fontScaleMenuAct);
        toolbar.add(fontScaleMenuButton);
    }

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
                System.out.println("Date header end: " + dateHeaderEnd);
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

        JMenuItem addLanguageItem = new JMenuItem();
        PhonUIAction<Void> addLanguageAct = PhonUIAction.runnable(() -> {System.out.println("Add language");});
        addLanguageAct.putValue(PhonUIAction.NAME, "Add language");
        addLanguageItem.setAction(addLanguageAct);
        languagesMenu.add(addLanguageItem);

        JMenuItem removeLanguageItem = new JMenuItem();
        PhonUIAction<Void> removeLanguageAct = PhonUIAction.runnable(() -> {System.out.println("Remove language");});
        removeLanguageAct.putValue(PhonUIAction.NAME, "Remove language");
        removeLanguageItem.setAction(removeLanguageAct);
        languagesMenu.add(removeLanguageItem);


        JMenuItem viewMetadataItem = new JMenuItem();
        PhonUIAction<Void> viewMetadataAct = PhonUIAction.runnable(() -> {System.out.println("View metadata");});
        viewMetadataAct.putValue(PhonUIAction.NAME, "View metadata");
        viewMetadataItem.setAction(viewMetadataAct);
        menuBuilder.addItem(".", viewMetadataItem);

        JMenuItem showSessionInfoViewItem = new JMenuItem();
        PhonUIAction<Void> showSessionInfoViewAct = PhonUIAction.runnable(
                () -> getEditor().getViewModel().showView(SessionInfoEditorView.VIEW_TITLE)
        );
        showSessionInfoViewAct.putValue(PhonUIAction.NAME, "Show Session Information view");
        showSessionInfoViewItem.setAction(showSessionInfoViewAct);
        menuBuilder.addItem(".", showSessionInfoViewItem);
    }

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
        PhonUIAction<Void> showTimelineViewAct = PhonUIAction.runnable(() -> getEditor().getViewModel().showView(TimelineView.VIEW_TITLE));
        showTimelineViewAct.putValue(PhonUIAction.NAME, "Show Timeline view");
        showTimelineViewItem.setAction(showTimelineViewAct);
        menuBuilder.addItem(".", showTimelineViewItem);

        JMenuItem showMediaPlayerViewItem = new JMenuItem();
        PhonUIAction<Void> showMediaPlayerViewAct = PhonUIAction.runnable(() -> getEditor().getViewModel().showView(MediaPlayerEditorView.VIEW_TITLE));
        showMediaPlayerViewAct.putValue(PhonUIAction.NAME, "Show Media Player view");
        showMediaPlayerViewItem.setAction(showMediaPlayerViewAct);
        menuBuilder.addItem(".", showMediaPlayerViewItem);

        JMenuItem showSpeechAnalysisViewItem = new JMenuItem();
        PhonUIAction<Void> showSpeechAnalysisViewAct = PhonUIAction.runnable(() -> getEditor().getViewModel().showView(SpeechAnalysisEditorView.VIEW_TITLE));
        showSpeechAnalysisViewAct.putValue(PhonUIAction.NAME, "Show Speech Analysis view");
        showSpeechAnalysisViewItem.setAction(showSpeechAnalysisViewAct);
        menuBuilder.addItem(".", showSpeechAnalysisViewItem);
    }

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
                () -> getEditor().getViewModel().showView(SessionInfoEditorView.VIEW_TITLE)
        );
        showSessionInfoViewAct.putValue(PhonUIAction.NAME, "Show Session Information view");
        showSessionInfoViewItem.setAction(showSessionInfoViewAct);
        menuBuilder.addItem(".", showSessionInfoViewItem);
    }

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

    private void setupTranscriptMenu(MenuBuilder menuBuilder) {

        SessionLocation sessionLocation = getTranscriptEditor().getCurrentSessionLocation();
        int currentTranscriptElementIndex = sessionLocation == null ? -1 : sessionLocation.getElementIndex();
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
                currentTranscriptElementIndex
            );
            getEditor().getUndoSupport().postEdit(edit);
        });
        insertGemBelowAct.putValue(PhonUIAction.NAME, "Insert gem below");
        insertGemBelowItem.setAction(insertGemBelowAct);
        menuBuilder.addItem(".", insertGemBelowItem);
    }

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
//                getEditor().getSession().
            });
            addTierAct.putValue(PhonUIAction.NAME, userTierType.name());
            addTierItem.setAction(addTierAct);
            addTierSubmenu.add(addTierItem);
        }

        addTierSubmenu.add(new JSeparator());

        JMenuItem addCustomTierItem = new JMenuItem();
        addCustomTierItem.setAction(new NewTierAction(getEditor(), (TierOrderingEditorView) getEditor().getViewModel().getView(TierOrderingEditorView.VIEW_TITLE)));
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
        PhonUIAction<Void> toggleChatTierNamesAct = PhonUIAction.runnable(() -> {System.out.println("Toggle chat tier names");});
        toggleChatTierNamesAct.putValue(PhonUIAction.NAME, "Toggle chat tier names");
        toggleChatTierNamesItem.setAction(toggleChatTierNamesAct);
        menuBuilder.addItem(".", toggleChatTierNamesItem);

        JMenuItem showTierManagementViewItem = new JMenuItem();
        PhonUIAction<Void> showTierManagementViewAct = PhonUIAction.runnable(() -> getEditor().getViewModel().showView(TierOrderingEditorView.VIEW_TITLE));
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
        return IconManager.getInstance().getIcon(VIEW_ICON, IconSize.SMALL);
    }

    @Override
    public JMenu getMenu() {
        final JMenu retVal = new JMenu();

        MenuBuilder menuBuilder = new MenuBuilder(retVal);

//        retVal.add(new ToggleSingleRecordAction(getEditor(), this));
//        retVal.add(new ToggleRecordNumbersAction(getEditor(), this));
//        retVal.add(new ToggleSyllabificationVisibleAction(getEditor(), this));
//        retVal.add(new ToggleSyllabificationIsComponent(getEditor(), this));
//        retVal.add(new ToggleAlignmentVisibleAction(getEditor(), this));
//        retVal.add(new ToggleAlignmentIsComponentAction(getEditor(), this));
//        retVal.add(new ToggleValidationModeAction(getEditor(), this));
//        retVal.add(new ToggleHeadersVisibleAction(getEditor(), this));
//        retVal.add(new FindAndReplaceAction(getEditor()));
//        retVal.add(new ExportAsPDFAction(getEditor(), this));

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
        System.out.println("Find and replace visible?: " + findAndReplaceVisible);
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

    //endregion Getters and Setters
}
