package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.app.session.editor.view.transcriptEditor.actions.*;
import ca.phon.session.MediaSegment;
import ca.phon.session.MediaUnit;
import ca.phon.session.SessionFactory;
import ca.phon.ui.CalloutWindow;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.DropDownIcon;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class TranscriptView extends EditorView {

    public final static String VIEW_NAME = "Transcript Editor";
    public final static String VIEW_ICON = "blank";
    private final TranscriptEditor transcriptEditor;
    private TranscriptScrollPane transcriptScrollPane;
    public final static String FONT_SIZE_DELTA_PROP = TranscriptView.class.getName() + ".fontSizeDelta";
    public final static float DEFAULT_FONT_SIZE_DELTA = 0.0f;
    public float fontSizeDelta = PrefHelper.getFloat(FONT_SIZE_DELTA_PROP, DEFAULT_FONT_SIZE_DELTA);

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

        addPropertyChangeListener("fontSizeDelta", e -> {
            PrefHelper.getUserPreferences().putFloat(FONT_SIZE_DELTA_PROP, getFontSizeDelta());
            transcriptEditor.getTranscriptDocument().reload();
        });
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

        JButton showCalloutButton = new JButton("Test callout");
        showCalloutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            CalloutWindow.showCallout(
                CommonModuleFrame.getCurrentFrame(),
                new JLabel("Testing some stuff................."),
                SwingConstants.SOUTH,
                SwingConstants.LEADING,
                e.getLocationOnScreen()
            );
            }
        });
        toolbar.add(showCalloutButton);


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
        scaleSlider.addChangeListener(changeEvent -> setFontSizeDelta(scaleSlider.getValue()));

        JComponent fontComp = new JPanel(new HorizontalLayout());
        fontComp.setOpaque(false);
        fontComp.add(smallLbl);
        fontComp.add(scaleSlider);
        fontComp.add(largeLbl);

        fontScaleMenu.add(fontComp, BorderLayout.CENTER);

        JButton defaultSizeButton = new JButton();
        final PhonUIAction<Void> useDefaultFontSizeAct = PhonUIAction.runnable(() -> {
            setFontSizeDelta(0.0f);
            scaleSlider.setValue(0);
        });
        useDefaultFontSizeAct.putValue(PhonUIAction.NAME, "Use default font size");
        useDefaultFontSizeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Reset font size");
        defaultSizeButton.setAction(useDefaultFontSizeAct);
        fontScaleMenu.add(defaultSizeButton, BorderLayout.SOUTH);

        JComponent source = (JComponent) pae.getActionEvent().getSource();
        Point point = source.getLocationOnScreen();

        CalloutWindow.showCallout(
            CommonModuleFrame.getCurrentFrame(),
            fontScaleMenu,
            SwingConstants.TOP,
            SwingConstants.LEADING,
            point
        );
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

    public float getFontSizeDelta() {
        return fontSizeDelta;
    }

    public void setFontSizeDelta(float fontSizeDelta) {
        float oldVal = this.fontSizeDelta;
        this.fontSizeDelta = fontSizeDelta;
        firePropertyChange("fontSizeDelta", oldVal, fontSizeDelta);
    }

    //endregion Getters and Setters
}
