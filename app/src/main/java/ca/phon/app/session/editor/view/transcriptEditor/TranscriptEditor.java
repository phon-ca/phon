package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.log.LogUtil;
import ca.phon.app.project.DesktopProject;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.ChangeSpeakerEdit;
import ca.phon.app.session.editor.undo.ExcludeRecordEdit;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.plugin.PluginManager;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.ui.DropDownIcon;
import ca.phon.ui.EmptyIcon;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.OSInfo;
import ca.phon.util.Tuple;
import org.jdesktop.swingx.HorizontalLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TranscriptEditor extends JEditorPane {
    private final Session session;
    private final EditorEventManager eventManager;
    private SegmentPlayback segmentPlayback;
    private SessionEditUndoSupport undoSupport;
    private UndoManager undoManager;
    private boolean controlPressed = false;
    private Object currentHighlight;
    DefaultHighlighter.DefaultHighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
    private int currentRecordIndex = -1;
    private boolean singleRecordView = false;

    public TranscriptEditor(
        Session session,
        EditorEventManager eventManager,
        SessionEditUndoSupport undoSupport,
        UndoManager undoManager
    ) {
        super();
        this.session = session;
        this.eventManager = eventManager;
        this.undoSupport = undoSupport;
        this.undoManager = undoManager;
        initActions();
        registerEditorActions();
        super.setEditorKitForContentType(TranscriptEditorKit.CONTENT_TYPE, new TranscriptEditorKit());
        setContentType(TranscriptEditorKit.CONTENT_TYPE);
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!controlPressed) return;
                highlightElementAtPoint(e.getPoint());
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!controlPressed) return;
                int mousePosInDoc = viewToModel2D(e.getPoint());
                var elem = getTranscriptDocument().getCharacterElement(mousePosInDoc);
                Tier<?> tier = (Tier<?>)elem.getAttributes().getAttribute("tier");
                if (tier != null && tier.getValue() instanceof MediaSegment mediaSegment) {
                    if (segmentPlayback != null) {
                        segmentPlayback.playSegment(mediaSegment);
                    }
                }
            }
        });
//        addPropertyChangeListener("currentRecord", e -> {
//            Record record = session.getRecord((Integer) e.getNewValue());
//            Transcript transcript = session.getTranscript();
//            EditorEvent<EditorEventType.RecordChangedData> event = new EditorEvent<>(
//                EditorEventType.RecordChanged,
//                TranscriptEditor.this,
//                new EditorEventType.RecordChangedData(record, transcript.getElementIndex(record), transcript.getRecordPosition(record))
//            );
//            eventManager.queueEvent(event);
//        });

        addCaretListener(e -> {
            TranscriptDocument doc = getTranscriptDocument();
            String transcriptElementType = (String) doc.getCharacterElement(e.getDot()).getAttributes().getAttribute("elementType");
            if (transcriptElementType != null && transcriptElementType.equals("record")) {
                setCurrentRecordIndex(doc.getRecordIndex(e.getDot()));
            }

            // FOR DEBUG PURPOSES ONLY

            //System.out.println(e.getDot());
            /*int cursorPos = e.getDot();
            int recordIndex = doc.getRecordIndex(cursorPos);
            int recordElementIndex = doc.getRecordElementIndex(cursorPos);
            Tier tier = doc.getTier(cursorPos);
            String tierName = tier != null ? tier.getName() : "null";
            System.out.println("Record " + recordIndex + " (Element: " + recordElementIndex + ") : " + tierName);
            System.out.println("Cursor Pos: " + cursorPos);
            System.out.println(doc.getRecordEnd(recordIndex, null));*/
            SimpleAttributeSet attrs = new SimpleAttributeSet(doc.getCharacterElement(e.getDot()).getAttributes().copyAttributes());
            System.out.println(attrs);
        });
    }

    public TranscriptEditor(Session session) {
        this(session, new EditorEventManager(), new SessionEditUndoSupport(), new UndoManager());
    }

     public static void main(String[] args) {
        try {
            // Get session
            DesktopProject project = new DesktopProject(new File("app/src/main/resources/transcriptEditor/nld-clpf"));
            String corpus = project.getCorpora().get(0);
            String sessionName = project.getCorpusSessions(corpus).get(0);
            Session session = project.openSession(corpus, sessionName);
            JFrame frame = new JFrame("Transcript Editor");
            TranscriptEditor editorPane = new TranscriptEditor(session);

            // This should be different somehow
            var doc = editorPane.getTranscriptDocument();

            frame.setSize(350, 275);
            frame.setLayout(new BorderLayout());
            JScrollPane scrollPane = new JScrollPane(editorPane);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            frame.add(scrollPane, BorderLayout.CENTER);
            JPanel bottomButtonPanel = new JPanel(new HorizontalLayout());

            JButton toggleTargetSyllablesButton = new JButton("Target");
            toggleTargetSyllablesButton.addActionListener(e -> {
                editorPane.setTargetSyllablesVisible(!editorPane.getTargetSyllablesVisible());
            });
            bottomButtonPanel.add(toggleTargetSyllablesButton);

            JButton toggleActualSyllablesButton = new JButton("Actual");
            toggleActualSyllablesButton.addActionListener(e -> {
                editorPane.setActualSyllablesVisible(!editorPane.getActualSyllablesVisible());
            });
            bottomButtonPanel.add(toggleActualSyllablesButton);

            JButton toggleAlignmentButton = new JButton("Alignment");
            toggleAlignmentButton.addActionListener(e -> {
                editorPane.setAlignmentVisible(!editorPane.getAlignmentVisible());
            });
            bottomButtonPanel.add(toggleAlignmentButton);

            frame.add(bottomButtonPanel, BorderLayout.SOUTH);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean getTargetSyllablesVisible() {
        return getTranscriptDocument().getTargetSyllablesVisible();
    }
    public boolean getActualSyllablesVisible() {
        return getTranscriptDocument().getActualSyllablesVisible();
    }
    public boolean getAlignmentVisible() {
        return getTranscriptDocument().getAlignmentVisible();
    }
    public void setTargetSyllablesVisible(boolean visible) {
        getTranscriptDocument().setTargetSyllablesVisible(visible);
    }
    public void setActualSyllablesVisible(boolean visible) {
        getTranscriptDocument().setActualSyllablesVisible(visible);
    }
    public void setAlignmentVisible(boolean visible) {
        getTranscriptDocument().setAlignmentVisible(visible);
    }

    // region Input Actions

    private void initActions() {
        InputMap inputMap = super.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = super.getActionMap();

        KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        inputMap.put(tab, "nextTierOrElement");
        PhonUIAction<Void> tabAct = PhonUIAction.runnable(this::nextTierOrElement);
        actionMap.put("nextTierOrElement", tabAct);

        KeyStroke shiftTab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK);
        inputMap.put(shiftTab, "prevTierOrElement");
        PhonUIAction<Void> shiftTabAct = PhonUIAction.runnable(this::prevTierOrElement);
        actionMap.put("prevTierOrElement", shiftTabAct);


        var controlKeyEvent = OSInfo.isMacOs() ? KeyEvent.VK_META : KeyEvent.VK_CONTROL;
        var modifier = OSInfo.isMacOs() ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;

        KeyStroke pressedControl = KeyStroke.getKeyStroke(controlKeyEvent, modifier, false);
        inputMap.put(pressedControl, "pressedControl");
        PhonUIAction<Void> pressedControlAct = PhonUIAction.runnable(this::pressedControl);
        actionMap.put("pressedControl", pressedControlAct);

        KeyStroke releasedControl = KeyStroke.getKeyStroke(controlKeyEvent, 0, true);
        inputMap.put(releasedControl, "releasedControl");
        PhonUIAction<Void> releasedControlAct = PhonUIAction.runnable(this::releasedControl);
        actionMap.put("releasedControl", releasedControlAct);
    }

    public void nextTierOrElement() {
        TranscriptDocument doc = getTranscriptDocument();

        int caretPos = getCaretPosition();
        var elem = doc.getCharacterElement(caretPos);

        Integer recordIndex = (Integer) elem.getAttributes().getAttribute("recordIndex");
        Tier tier = (Tier)elem.getAttributes().getAttribute("tier");
        if (recordIndex == null || tier == null) return;

        String tierName = tier.getName();
        var effectiveTierView = session.getTierView().stream().filter(tv -> tv.isVisible()).toList();
        var tierViewItem = effectiveTierView
            .stream()
            .filter(tv -> tv.getTierName().equals(tierName))
            .findFirst()
            .orElse(null);
        int tierIndex = effectiveTierView.indexOf(tierViewItem);

        int newCaretPos;

        if (tierIndex == effectiveTierView.size() - 1) {
            int newRecordIndex = (recordIndex + 1) % session.getRecordCount();
            newCaretPos = doc.getTierStart(newRecordIndex, effectiveTierView.get(0).getTierName());
        }
        else {
            int newTierIndex = (tierIndex + 1) % effectiveTierView.size();
            newCaretPos = doc.getTierStart(recordIndex, effectiveTierView.get(newTierIndex).getTierName());
        }

        setCaretPosition(newCaretPos);
    }

    public void prevTierOrElement() {
        TranscriptDocument doc = getTranscriptDocument();

        int caretPos = getCaretPosition();
        var elem = doc.getCharacterElement(caretPos);

        Integer recordIndex = (Integer) elem.getAttributes().getAttribute("recordIndex");
        Tier tier = (Tier)elem.getAttributes().getAttribute("tier");
        if (recordIndex == null || tier == null) return;

        String tierName = tier.getName();
        var effectiveTierView = session.getTierView().stream().filter(tv -> tv.isVisible()).toList();
        var tierViewItem = effectiveTierView
            .stream()
            .filter(tv -> tv.getTierName().equals(tierName))
            .findFirst()
            .orElse(null);
        int tierIndex = effectiveTierView.indexOf(tierViewItem);

        int newCaretPos;

        if (tierIndex == 0) {
            int newRecordIndex = recordIndex - 1;
            if (newRecordIndex < 0) {
                newRecordIndex = session.getRecordCount() - 1;
            }
            newCaretPos = doc.getTierStart(
                newRecordIndex,
                effectiveTierView.get(effectiveTierView.size() - 1).getTierName()
            );
        }
        else {
            int newTierIndex = (tierIndex - 1) % effectiveTierView.size();
            newCaretPos = doc.getTierStart(recordIndex, effectiveTierView.get(newTierIndex).getTierName());
        }

        setCaretPosition(newCaretPos);
    }

    public void pressedControl() {
        if (!controlPressed) {
            System.out.println("Press");
            controlPressed = true;
            Point2D mousePoint = MouseInfo.getPointerInfo().getLocation();
            System.out.println(viewToModel2D(mousePoint));
            highlightElementAtPoint(mousePoint);
            repaint();
        }
    }

    public void releasedControl() {
        if (controlPressed) {
            System.out.println("Release");
            controlPressed = false;
            removeCurrentHighlight();
        }
    }

    // endregion Input Actions

    private void createTierLabelPopup(JLabel tierLabel, MouseEvent mouseEvent) {
        JPopupMenu menu = new JPopupMenu();
        MenuBuilder builder = new MenuBuilder(menu);

        var extPts = PluginManager.getInstance().getExtensionPoints(TierLabelMenuHandler.class);

        for (var extPt : extPts) {
            var menuHandler = extPt.getFactory().createObject();
            menuHandler.addMenuItems(builder);
        }

        menu.show(tierLabel, mouseEvent.getX(), mouseEvent.getY());
    }

    public void moveTier(EditorEventType.TierViewChangedData data) {

        // Switch this when fixed
        var startTierView = data.oldTierView();
        var endTierView = data.newTierView();

        List<TierViewItem> movedTiers = new ArrayList<>();
        for (int i = 0; i < startTierView.size(); i++) {
            if (!startTierView.get(i).equals(endTierView.get(i))) {
                movedTiers.add(startTierView.get(i));
            }
        }

        TranscriptDocument doc = getTranscriptDocument();

        // Check if caret affected by move
        int startCaretPos = getCaretPosition();
        var elem = doc.getCharacterElement(startCaretPos);
        Tier caretTier = (Tier)elem.getAttributes().getAttribute("tier");
        int caretTierOffset = -1;

        if (caretTier != null) {
            String caretTierName = caretTier.getName();
            boolean caretTierHasMoved = movedTiers
                .stream()
                .anyMatch(item -> item.getTierName().equals(caretTierName));
            if (caretTierHasMoved) {
                System.out.println("Move the caret");
                caretTierOffset = startCaretPos - elem.getStartOffset();
            }
        }

        // Move tier in doc
        doc.moveTier(movedTiers);

        // Correct caret
        if (caretTierOffset > -1) {
            // Move the caret so that it has the same offset from the tiers new pos
            setCaretPosition(doc.getTierStart(caretTier) + caretTierOffset);
        }
    }

    public void deleteTier(EditorEventType.TierViewChangedData data) {
        TranscriptDocument doc = getTranscriptDocument();

        boolean pastDeletedTier = false;
        List<TierViewItem> movedTiers = new ArrayList<>();
        for (TierViewItem item : data.oldTierView()) {
            if (!pastDeletedTier && data.tierNames().contains(item.getTierName())) {
                pastDeletedTier = true;
                continue;
            }
            if (pastDeletedTier) {
                movedTiers.add(item);
            }
        }

        // Check if caret affected by move
        int startCaretPos = getCaretPosition();
        var elem = doc.getCharacterElement(startCaretPos);
        Tier caretTier = (Tier)elem.getAttributes().getAttribute("tier");
        Integer caretRecordIndex = (Integer) elem.getAttributes().getAttribute("recordIndex");

        int caretTierOffset = -1;

        if (caretTier != null) {
            caretTierOffset = startCaretPos - elem.getStartOffset();
        }


        getTranscriptDocument().deleteTier(data.tierNames());


        // Correct caret
        if (caretTier != null) {
            List<TierViewItem> oldVisibleTierView = data
                .oldTierView()
                .stream()
                .filter(item -> item.isVisible())
                .toList();
            int caretTierIndex = oldVisibleTierView
                .stream()
                .map(item -> item.getTierName())
                .toList()
                .indexOf(caretTier.getName());
            boolean caretTierWasLast = caretTierIndex == oldVisibleTierView.size() - 1;
            boolean caretTierWasDeleted = data
                .tierNames()
                .stream()
                .anyMatch(tierName -> tierName.equals(caretTier.getName()));
            boolean caretTierWasMoved = movedTiers
                .stream()
                .anyMatch(item -> item.getTierName().equals(caretTier.getName()));

            if (caretTierWasDeleted) {
                String newCaretTierName;

                System.out.println(oldVisibleTierView.stream().map(item -> item.getTierName()).toList());
                System.out.println("Caret tier was last: " + caretTierWasLast);
                if (caretTierWasLast) {
                    newCaretTierName = oldVisibleTierView.get(oldVisibleTierView.size() - 2).getTierName();
                }
                else {
                    newCaretTierName = oldVisibleTierView.get(caretTierIndex + 1).getTierName();
                }
                System.out.println(newCaretTierName);
                setCaretPosition(doc.getTierStart(caretRecordIndex, newCaretTierName) + caretTierOffset);
            }
            else if (caretTierWasMoved) {
                setCaretPosition(doc.getTierStart(caretTier) + caretTierOffset);
            }
        }
    }

    public void addTier(EditorEventType.TierViewChangedData data) {
        var doc = getTranscriptDocument();

        int startCaretPos = getCaretPosition();
        var elem = doc.getCharacterElement(startCaretPos);
        Tier caretTier = (Tier)elem.getAttributes().getAttribute("tier");
        int caretTierOffset = -1;

        if (caretTier != null) {
            caretTierOffset = startCaretPos - elem.getStartOffset();
        }

        List<TierViewItem> addedTiers = new ArrayList<>();
        for (TierViewItem item : data.newTierView()) {
            if (!data.oldTierView().contains(item)) {
                addedTiers.add(item);
            }
        }
        doc.addTier(addedTiers);

        // Correct caret
        if (caretTierOffset > -1) {
            // Move the caret so that it has the same offset from the tiers new pos
            setCaretPosition(doc.getTierStart(caretTier) + caretTierOffset);
        }
        else {
            // Put the caret back where it was before the move
            setCaretPosition(startCaretPos);
        }
    }

    public void hideTier(EditorEventType.TierViewChangedData data) {
        TranscriptDocument doc = getTranscriptDocument();

        boolean pastHiddenTier = false;
        List<TierViewItem> movedTiers = new ArrayList<>();
        for (TierViewItem item : data.oldTierView()) {
            if (!pastHiddenTier && data.tierNames().contains(item.getTierName())) {
                pastHiddenTier = true;
                continue;
            }
            if (pastHiddenTier) {
                movedTiers.add(item);
            }
        }

        // Check if caret affected by move
        int startCaretPos = getCaretPosition();
        var elem = doc.getCharacterElement(startCaretPos);
        Tier caretTier = (Tier)elem.getAttributes().getAttribute("tier");
        Integer caretRecordIndex = (Integer) elem.getAttributes().getAttribute("recordIndex");

        int caretTierOffset = -1;

        if (caretTier != null) {
            caretTierOffset = startCaretPos - elem.getStartOffset();
        }

        doc.hideTier(data.tierNames());

        // Correct caret
        if (caretTier != null) {
            List<TierViewItem> oldVisibleTierView = data
                .oldTierView()
                .stream()
                .filter(item -> item.isVisible())
                .toList();
            int caretTierIndex = oldVisibleTierView
                .stream()
                .map(item -> item.getTierName())
                .toList()
                .indexOf(caretTier.getName());
            boolean caretTierWasLast = caretTierIndex == oldVisibleTierView.size() - 1;
            boolean caretTierWasDeleted = data
                .tierNames()
                .stream()
                .anyMatch(tierName -> tierName.equals(caretTier.getName()));
            boolean caretTierWasMoved = movedTiers
                .stream()
                .anyMatch(item -> item.getTierName().equals(caretTier.getName()));

            if (caretTierWasDeleted) {
                String newCaretTierName;

                System.out.println(oldVisibleTierView.stream().map(item -> item.getTierName()).toList());
                System.out.println("Caret tier was last: " + caretTierWasLast);
                if (caretTierWasLast) {
                    newCaretTierName = oldVisibleTierView.get(oldVisibleTierView.size() - 2).getTierName();
                }
                else {
                    newCaretTierName = oldVisibleTierView.get(caretTierIndex + 1).getTierName();
                }
                System.out.println(newCaretTierName);
                setCaretPosition(doc.getTierStart(caretRecordIndex, newCaretTierName) + caretTierOffset);
            }
            else if (caretTierWasMoved) {
                setCaretPosition(doc.getTierStart(caretTier) + caretTierOffset);
            }
        }
    }

    public void showTier(EditorEventType.TierViewChangedData data) {
        var doc = getTranscriptDocument();

        int startCaretPos = getCaretPosition();
        var elem = doc.getCharacterElement(startCaretPos);
        Tier caretTier = (Tier)elem.getAttributes().getAttribute("tier");
        int caretTierOffset = -1;

        if (caretTier != null) {
            caretTierOffset = startCaretPos - elem.getStartOffset();
        }

        List<TierViewItem> shownTiers = new ArrayList<>();
        for (TierViewItem item : data.newTierView()) {
            if (!data.oldTierView().contains(item)) {
                shownTiers.add(item);
            }
        }
        getTranscriptDocument().showTier(shownTiers, data.newTierView());

        // Correct caret
        if (caretTierOffset > -1) {
            // Move the caret so that it has the same offset from the tiers new pos
            setCaretPosition(doc.getTierStart(caretTier) + caretTierOffset);
        }
        else {
            // Put the caret back where it was before the move
            setCaretPosition(startCaretPos);
        }
    }

    public void tierNameChanged(EditorEventType.TierViewChangedData data) {
        System.out.println(data.oldTierView().stream().map(item -> item.getTierName()).toList());
        System.out.println(data.newTierView().stream().map(item -> item.getTierName()).toList());

        List<TierViewItem> oldTiers = new ArrayList<>();
        List<TierViewItem> newTiers = new ArrayList<>();
        for (Integer index : data.viewIndices()) {
            TierViewItem item = data.newTierView().get(index);
            if (item.isVisible()) {
                oldTiers.add(data.oldTierView().get(index));
                newTiers.add(item);
            }
        }

        if (newTiers.isEmpty()) return;

        getTranscriptDocument().tierNameChanged(oldTiers, newTiers);
    }

    public void tierFontChanged(EditorEventType.TierViewChangedData data) {
        int caretPos = getCaretPosition();

        List<TierViewItem> changedTiers = data
            .newTierView()
            .stream()
            .filter(item -> data.tierNames().contains(item.getTierName()))
            .toList();

        if (changedTiers.isEmpty()) return;

        getTranscriptDocument().tierFontChanged(changedTiers);

        setCaretPosition(caretPos);
    }

    public TranscriptDocument getTranscriptDocument() {
        return (TranscriptDocument) getDocument();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    private void registerEditorActions() {
        this.eventManager.registerActionForEvent(EditorEventType.SessionChanged, this::onSessionChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
        this.eventManager.registerActionForEvent(EditorEventType.TierViewChanged, this::onTierViewChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
        this.eventManager.registerActionForEvent(EditorEventType.RecordChanged, this::onRecordChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
    }

    public void removeEditorActions() {
        this.eventManager.removeActionForEvent(EditorEventType.SessionChanged, this::onSessionChanged);
        this.eventManager.removeActionForEvent(EditorEventType.TierViewChanged, this::onTierViewChanged);
        this.eventManager.removeActionForEvent(EditorEventType.RecordChanged, this::onRecordChanged);
    }

    private void onSessionChanged(EditorEvent<Session> editorEvent) {

    }

    private void onTierViewChanged(EditorEvent<EditorEventType.TierViewChangedData> editorEvent) {
        var changeType = editorEvent.data().changeType();
        switch (changeType) {
            case MOVE_TIER -> moveTier(editorEvent.data());
            case RELOAD -> getTranscriptDocument().reload();
            case DELETE_TIER -> deleteTier(editorEvent.data());
            case ADD_TIER -> addTier(editorEvent.data());
            case HIDE_TIER -> hideTier(editorEvent.data());
            case SHOW_TIER -> showTier(editorEvent.data());
            case TIER_NAME_CHANGE -> tierNameChanged(editorEvent.data());
            case TIER_FONT_CHANGE -> tierFontChanged(editorEvent.data());
            default -> {}
        }
    }

    public SegmentPlayback getSegmentPlayback() {
        return segmentPlayback;
    }

    public void setSegmentPlayback(SegmentPlayback segmentPlayback) {
        this.segmentPlayback = segmentPlayback;
    }

    public SessionEditUndoSupport getUndoSupport() {
        return undoSupport;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    private JComponent createTierLabel(Tier<?> tier, TierViewItem item) {
        String tierName = item.getTierName();
        JLabel tierLabel = new JLabel(tierName);

        var labelFont = new Font(tierLabel.getFont().getFontName(), tierLabel.getFont().getStyle(), 12);
        tierLabel.setFont(labelFont);

        tierLabel.setAlignmentY(.8f);
        tierLabel.setMaximumSize(new Dimension(150, tierLabel.getPreferredSize().height));

        tierLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        DropDownIcon dropDownIcon = new DropDownIcon(new EmptyIcon(0, 16), 0, SwingConstants.BOTTOM);
        tierLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        tierLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tierLabel.setIcon(dropDownIcon);

        EmptyBorder tierLabelPadding = new EmptyBorder(0,8,0,8);
        tierLabel.setBorder(tierLabelPadding);
        tierLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getTranscriptDocument().setTierItemViewLocked(tierName, true);
                createTierLabelPopup(tierLabel, e);
            }
        });

        return tierLabel;
    }

    private JComponent createCommentLabel(Comment comment) {
        JLabel commentLabel = new JLabel(comment.getType().getLabel());

        var labelFont = new Font(commentLabel.getFont().getFontName(), commentLabel.getFont().getStyle(), 12);
        commentLabel.setFont(labelFont);

        commentLabel.setAlignmentY(.8f);
        commentLabel.setMaximumSize(new Dimension(150, commentLabel.getPreferredSize().height));

        commentLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        DropDownIcon dropDownIcon = new DropDownIcon(new EmptyIcon(0, 16), 0, SwingConstants.BOTTOM);
        commentLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        commentLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        commentLabel.setIcon(dropDownIcon);

        EmptyBorder tierLabelPadding = new EmptyBorder(0,8,0,8);
        commentLabel.setBorder(tierLabelPadding);
        commentLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();

                JMenu addComment = new JMenu("Add comment");
                addComment.add("Add comment above");
                addComment.add("Add comment below");
                addComment.add("Add comment at bottom");
                menu.add(addComment);

                JMenu addGem = new JMenu("Add gem");
                addGem.add("Add gem above");
                addGem.add("Add gem below");
                addGem.add("Add gem at bottom");
                menu.add(addGem);

                menu.add("Delete me");

                menu.show(commentLabel, e.getX(), e.getY());
            }
        });

        return commentLabel;
    }

    private JComponent createGemLabel(Gem gem) {
        JLabel gemLabel = new JLabel(gem.getType().toString());

        var labelFont = new Font(gemLabel.getFont().getFontName(), gemLabel.getFont().getStyle(), 12);
        gemLabel.setFont(labelFont);

        gemLabel.setAlignmentY(.8f);
        gemLabel.setMaximumSize(new Dimension(150, gemLabel.getPreferredSize().height));

        gemLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        DropDownIcon dropDownIcon = new DropDownIcon(new EmptyIcon(0, 16), 0, SwingConstants.BOTTOM);
        gemLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        gemLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gemLabel.setIcon(dropDownIcon);

        EmptyBorder tierLabelPadding = new EmptyBorder(0,8,0,8);
        gemLabel.setBorder(tierLabelPadding);
        gemLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();

                JMenu addComment = new JMenu("Add comment");
                addComment.add("Add comment above");
                addComment.add("Add comment below");
                addComment.add("Add comment at bottom");
                menu.add(addComment);

                JMenu addGem = new JMenu("Add gem");
                addGem.add("Add gem above");
                addGem.add("Add gem below");
                addGem.add("Add gem at bottom");
                menu.add(addGem);

                menu.add("Delete me");

                menu.show(gemLabel, e.getX(), e.getY());
            }
        });

        return gemLabel;
    }

    private JComponent createSeparator(Record record, Integer recordIndex) {
        JPanel separatorPanel = new JPanel(new HorizontalLayout());
        separatorPanel.setBorder(new EmptyBorder(0,8,0,8));
        separatorPanel.setBackground(Color.WHITE);

        DropDownIcon dropDownIcon = new DropDownIcon(new EmptyIcon(0, 16), 0, SwingConstants.BOTTOM);

        JLabel recordNumberLabel = new JLabel("#" + (recordIndex+1));
        recordNumberLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        recordNumberLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        recordNumberLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        recordNumberLabel.setIcon(dropDownIcon);
        var labelFont = new Font(
                recordNumberLabel.getFont().getFontName(),
                recordNumberLabel.getFont().getStyle(),
                12
        );
        recordNumberLabel.setFont(labelFont);
        recordNumberLabel.setAlignmentY(.8f);
        separatorPanel.add(recordNumberLabel);
        recordNumberLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();
                JCheckBoxMenuItem excludeMenuItem = new JCheckBoxMenuItem("Exclude");
                menu.add(excludeMenuItem);
                excludeMenuItem.setState(record.isExcludeFromSearches());
                excludeMenuItem.addActionListener(evt -> {
                    ExcludeRecordEdit excludeEdit = new ExcludeRecordEdit(
                        session,
                        eventManager,
                        record,
                        excludeMenuItem.getState()
                    );
                    getUndoSupport().postEdit(excludeEdit);
                });
                menu.add(new JMenuItem("Move"));
                menu.show(recordNumberLabel, e.getX(), e.getY());
            }
        });

        JLabel speakerNameLabel = new JLabel(record.getSpeaker().getName());
        speakerNameLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        speakerNameLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        speakerNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        speakerNameLabel.setIcon(dropDownIcon);
        speakerNameLabel.setFont(labelFont);
        speakerNameLabel.setAlignmentY(.8f);
        separatorPanel.add(speakerNameLabel);
        speakerNameLabel.setBorder(new EmptyBorder(0,8,0,8));
        speakerNameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();

                List<Participant> possibleSpeakers = new ArrayList<>(session.getParticipants().stream().toList());
                possibleSpeakers.add(Participant.UNKNOWN);

                for (Participant participant : possibleSpeakers) {
                    PhonUIAction<Tuple<Record, Participant>> changeSpeakerAct = PhonUIAction.consumer(TranscriptEditor.this::changeSpeaker, new Tuple<>(record, participant));
                    changeSpeakerAct.putValue(PhonUIAction.NAME, participant.toString());
                    changeSpeakerAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Set record speaker to " + participant.toString());
                    changeSpeakerAct.putValue(PhonUIAction.SELECTED_KEY, participant.equals(record.getSpeaker()));

                    var menuItem = new JCheckBoxMenuItem(changeSpeakerAct);
                    menu.add(menuItem);
                }

                menu.show(recordNumberLabel, e.getX(), e.getY());
            }
        });

        var sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setPreferredSize(new Dimension(10000, 1));
        separatorPanel.add(sep);

        return separatorPanel;
    }

    public void changeSpeaker(Tuple<Record, Participant> data) {
        ChangeSpeakerEdit edit = new ChangeSpeakerEdit(session, eventManager, data.getObj1(), data.getObj2());
        undoSupport.postEdit(edit);
    }

    private void highlightElementAtPoint(Point2D point) {
        int mousePosInDoc = viewToModel2D(point);
        var elem = getTranscriptDocument().getCharacterElement(mousePosInDoc);
        try {
            removeCurrentHighlight();
            currentHighlight = getHighlighter().addHighlight(
                elem.getStartOffset(),
                elem.getEndOffset(),
                highlightPainter
            );
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void removeCurrentHighlight() {
        if (currentHighlight != null) {
            getHighlighter().removeHighlight(currentHighlight);
        }
    }

    private void onRecordChanged(EditorEvent<EditorEventType.RecordChangedData> editorEvent) {
        if (hasFocus()) return;

        int recordStartPos = getTranscriptDocument().getRecordStart(editorEvent.data().recordIndex());
        int recordEndPos = getTranscriptDocument().getRecordEnd(editorEvent.data().recordIndex());
        try {
            var startRect = modelToView2D(recordStartPos);
            var endRect = modelToView2D(recordEndPos);

            Rectangle scrollToRect = new Rectangle(
                (int) startRect.getMinX(),
                (int) startRect.getMinY(),
                (int) (endRect.getMaxX() - startRect.getMinX()),
                (int) (endRect.getMaxY() - startRect.getMinY())
            );

            super.scrollRectToVisible(scrollToRect);
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    public int getCurrentElementIndex() {
        Element elem = getTranscriptDocument().getCharacterElement(getCaretPosition());
        if (elem == null) return -1;

        String elementType = (String) elem.getAttributes().getAttribute("elementType");
        if (elementType == null) return -1;

        if (elementType.equals("comment")) {
            Comment comment = (Comment) elem.getAttributes().getAttribute("comment");
            if (comment != null) {
                for (Transcript.Element transcriptElem : session.getTranscript()) {
                    if (transcriptElem.isComment() && transcriptElem.asComment().equals(comment)) {
                        return session.getTranscript().getElementIndex(transcriptElem);
                    }
                }
            }
        }
        else if (elementType.equals("gem")) {
            Gem gem = (Gem) elem.getAttributes().getAttribute("gem");
            if (gem != null) {
                for (Transcript.Element transcriptElem : session.getTranscript()) {
                    if (transcriptElem.isGem() && transcriptElem.asGem().equals(gem)) {
                        return session.getTranscript().getElementIndex(transcriptElem);
                    }
                }
            }
        }
        else if (elementType.equals("record")) {
            Integer recordIndex = (Integer) elem.getAttributes().getAttribute("recordIndex");
            if (recordIndex != null) {
                return session.getRecordElementIndex(recordIndex);
            }
        }

        return -1;
    }

    public void setCurrentElementIndex(int index) {

        Transcript.Element transcriptElem = session.getTranscript().getElementAt(index);
        String transcriptElemType;
        if (transcriptElem.isComment()) {transcriptElemType = "comment";}
        else if (transcriptElem.isGem()) {transcriptElemType = "gem";}
        else {transcriptElemType = "record";}

        var root = getTranscriptDocument().getDefaultRootElement();
        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            for (int j = 0; j < elem.getElementCount(); j++) {
                Element innerElem = elem.getElement(j);
                String elemType = (String) innerElem.getAttributes().getAttribute("elementType");
                if (elemType != null && elemType.equals(transcriptElemType)) {
                    if (transcriptElem.isComment()) {
                        Comment comment = (Comment) innerElem.getAttributes().getAttribute("comment");
                        if (comment.equals(transcriptElem.asComment())) setCaretPosition(innerElem.getStartOffset());
                    }
                    else if (transcriptElem.isGem()) {
                        Gem gem = (Gem) innerElem.getAttributes().getAttribute("gem");
                        if (gem.equals(transcriptElem.asGem())) setCaretPosition(innerElem.getStartOffset());
                    }
                    else {
                        Record record = (Record) innerElem.getAttributes().getAttribute("record");
                        if (record.equals(transcriptElem.asRecord())) setCaretPosition(innerElem.getStartOffset());
                    }
                }
            }
        }
    }

    public void setCurrentRecordIndex(int index) {
        int oldIndex = this.currentRecordIndex;
        this.currentRecordIndex = index;
        super.firePropertyChange("currentRecordIndex", oldIndex, this.currentRecordIndex);
    }

    public int getCurrentRecordIndex() {
        Element elem = getTranscriptDocument().getCharacterElement(getCaretPosition());
        Integer recordIndex = (Integer) elem.getAttributes().getAttribute("recordIndex");
        if (recordIndex != null) {
            return recordIndex;
        }

        Transcript transcript = session.getTranscript();
        for (int i = getCurrentElementIndex(); i < transcript.getNumberOfElements(); i++) {
            Transcript.Element transcriptElem = transcript.getElementAt(i);
            if (transcriptElem.isRecord()) {
                return transcript.getRecordPosition(transcriptElem.asRecord());
            }
        }

        return -1;
    }

    public boolean isSingleRecordView() {
        return singleRecordView;
    }

    public void setSingleRecordView(boolean singleRecordView) {
        this.singleRecordView = singleRecordView;
    }

    public void setSession() {
        TranscriptDocument doc = (TranscriptDocument) getEditorKit().createDefaultDocument();
        doc.setTierLabelFactory(this::createTierLabel);
        doc.setCommentLabelFactory(this::createCommentLabel);
        doc.setGemLabelFactory(this::createGemLabel);
        doc.setSeparatorFactory(this::createSeparator);
        doc.setSession(session);
        setDocument(doc);
    }
}
