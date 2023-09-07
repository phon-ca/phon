package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.log.LogUtil;
import ca.phon.app.project.DesktopProject;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.ChangeSpeakerEdit;
import ca.phon.app.session.editor.undo.ExcludeRecordEdit;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.formatter.MediaTimeFormat;
import ca.phon.formatter.MediaTimeFormatStyle;
import ca.phon.formatter.MediaTimeFormatter;
import ca.phon.plugin.PluginManager;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.ui.DropDownIcon;
import ca.phon.ui.EmptyIcon;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.OSInfo;
import ca.phon.util.Tuple;
import org.jdesktop.swingx.HorizontalLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
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
    public final static EditorEventType<Void> recordChangedInSingleRecordMode = new EditorEventType<>("recordChangedInSingleRecordMode", Void.class);

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
            try {
                TranscriptDocument doc = getTranscriptDocument();
                String transcriptElementType = (String) doc.getCharacterElement(e.getDot()).getAttributes().getAttribute("elementType");
                if (transcriptElementType != null && transcriptElementType.equals("record")) {
                    setCurrentRecordIndex(doc.getRecordIndex(e.getDot()));
                }

                // FOR DEBUG PURPOSES ONLY
                /*int cursorPos = e.getDot();
                int recordIndex = doc.getRecordIndex(cursorPos);
                int recordElementIndex = doc.getRecordElementIndex(cursorPos);
                Tier tier = doc.getTier(cursorPos);
                String tierName = tier != null ? tier.getName() : "null";
                System.out.println("Record " + recordIndex + " (Element: " + recordElementIndex + ") : " + tierName);
                System.out.println("Cursor Pos: " + cursorPos);
                System.out.println(doc.getRecordEnd(recordIndex, null));*/
                SimpleAttributeSet attrs = new SimpleAttributeSet(doc.getCharacterElement(e.getDot()).getAttributes().copyAttributes());
                System.out.println(e.getDot() + ": " + doc.getCharAtPos(e.getDot()));
                //System.out.println(attrs);
            }
            catch (Exception exception) {
                System.out.println(exception);
            }
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

            frame.add(bottomButtonPanel, BorderLayout.SOUTH);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
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


        KeyStroke right = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
        inputMap.put(right, "nextEditableIndex");
        PhonUIAction<Void> rightAct = PhonUIAction.runnable(() -> setCaretPosition(getNextEditableIndex(getCaretPosition() + 1)));
        actionMap.put("nextEditableIndex", rightAct);

        KeyStroke left = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
        inputMap.put(left, "prevEditableIndex");
        PhonUIAction<Void> leftAct = PhonUIAction.runnable(() -> setCaretPosition(getPrevEditableIndex(getCaretPosition() - 1)));
        actionMap.put("prevEditableIndex", leftAct);


        KeyStroke up = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
        inputMap.put(up, "sameOffsetPrevTier");
        PhonUIAction<Void> upAct = PhonUIAction.runnable(this::sameOffsetInPrevTierOrElement);
        actionMap.put("sameOffsetPrevTier", upAct);

        KeyStroke down = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
        inputMap.put(down, "sameOffsetNextTier");
        PhonUIAction<Void> downAct = PhonUIAction.runnable(this::sameOffsetInNextTierOrElement);
        actionMap.put("sameOffsetNextTier", downAct);
    }

    public void nextTierOrElement() {
        int caretPos = getCaretPosition();

        int newCaretPos = getStartOfNextTierOrElement(caretPos);
        if (newCaretPos == -1) return;

        setCaretPosition(newCaretPos);
    }

    public void prevTierOrElement() {
        int caretPos = getCaretPosition();

        int newCaretPos = getStartOfPrevTierOrElement(caretPos);
        if (newCaretPos == -1) return;

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
        long startTimeMS = new Date().getTime();

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

        Document blank = new DefaultStyledDocument();
        setDocument(blank);
        // Move tier in doc
        doc.moveTier(movedTiers);
        setDocument(doc);

        // Correct caret
        if (caretTierOffset > -1) {
            // Move the caret so that it has the same offset from the tiers new pos
            setCaretPosition(doc.getTierStart(caretTier) + caretTierOffset);
        }

        System.out.println("Time to move tiers: " + (new Date().getTime() - startTimeMS)/1000f + " seconds");
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



        Document blank = new DefaultStyledDocument();
        setDocument(blank);
        // Delete tier in doc
        doc.deleteTier(data.tierNames());
        setDocument(doc);



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

        Document blank = new DefaultStyledDocument();
        setDocument(blank);
        // Add tier in doc
        doc.addTier(addedTiers);
        setDocument(doc);

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

        Document blank = new DefaultStyledDocument();
        setDocument(blank);
        // Hide tier in doc
        doc.hideTier(data.tierNames());
        setDocument(doc);

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

        Document blank = new DefaultStyledDocument();
        setDocument(blank);
        // Show tier in doc
        doc.showTier(shownTiers, data.newTierView());
        setDocument(doc);

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
        TranscriptDocument doc = getTranscriptDocument();
        int caretPos = getCaretPosition();

        List<TierViewItem> changedTiers = data
            .newTierView()
            .stream()
            .filter(item -> data.tierNames().contains(item.getTierName()))
            .toList();

        if (changedTiers.isEmpty()) return;

        Document blank = new DefaultStyledDocument();
        setDocument(blank);
        // Change tier font in doc
        getTranscriptDocument().tierFontChanged(changedTiers);
        setDocument(doc);

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

        tierLabel.setFont(FontPreferences.getTierFont());

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

        commentLabel.setFont(FontPreferences.getTierFont());

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

        gemLabel.setFont(FontPreferences.getTierFont());

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
        JLabel speakerNameLabel = new JLabel(record.getSpeaker().toString());
        speakerNameLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        speakerNameLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        speakerNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        speakerNameLabel.setIcon(dropDownIcon);
        speakerNameLabel.setFont(FontPreferences.getTierFont());
        speakerNameLabel.setAlignmentY(.8f);
        separatorPanel.add(speakerNameLabel);
        speakerNameLabel.setBorder(new EmptyBorder(0,0,0,8));
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

                menu.show(speakerNameLabel, e.getX(), e.getY());
            }
        });

        JLabel segmentLabel = new JLabel();
        MediaSegment segment = record.getMediaSegment();
        StringBuilder segmentLabelTextBuilder = new StringBuilder();
        segmentLabelTextBuilder.append("•");
        segmentLabelTextBuilder.append(MediaTimeFormatter.timeToString(segment.getStartValue(), MediaTimeFormatStyle.PADDED_MINUTES_AND_SECONDS));
        segmentLabelTextBuilder.append("-");
        segmentLabelTextBuilder.append(MediaTimeFormatter.timeToString(segment.getEndValue(), MediaTimeFormatStyle.PADDED_MINUTES_AND_SECONDS));
        segmentLabelTextBuilder.append("•");
        segmentLabel.setText(segmentLabelTextBuilder.toString());
        segmentLabel.setFont(FontPreferences.getTierFont());
        segmentLabel.setAlignmentY(.8f);
        segmentLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        segmentLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        separatorPanel.add(segmentLabel);

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
        TranscriptDocument doc = getTranscriptDocument();

        doc.setSingleRecordIndex(editorEvent.data().recordIndex());

        if (doc.getSingleRecordView()) {
            final EditorEvent<Void> e = new EditorEvent<>(recordChangedInSingleRecordMode, this, null);
            eventManager.queueEvent(e);
        }

        if (hasFocus()) return;

        int recordStartPos = doc.getRecordStart(editorEvent.data().recordIndex());
        int recordEndPos = doc.getRecordEnd(editorEvent.data().recordIndex());

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

        switch (elementType) {
            case "comment" -> {
                Comment comment = (Comment) elem.getAttributes().getAttribute("comment");
                return session.getTranscript().getElementIndex(comment);
            }
            case "gem" -> {
                Gem gem = (Gem) elem.getAttributes().getAttribute("gem");
                return session.getTranscript().getElementIndex(gem);
            }
            case "record" -> {
                Integer recordIndex = (Integer) elem.getAttributes().getAttribute("recordIndex");
                return session.getTranscript().getRecordElementIndex(recordIndex);
            }
            default -> {
                return -1;
            }
        }
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
        Element firstInnerElem = elem.getElement(0);
        if (firstInnerElem != null) {
            Integer recordIndex = (Integer) firstInnerElem.getAttributes().getAttribute("recordIndex");
            if (recordIndex != null) {
                return recordIndex;
            }
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

    public int getNextEditableIndex(int currentPos) {
        TranscriptDocument doc = getTranscriptDocument();

        int docLen = doc.getLength();

        Element elem = doc.getCharacterElement(currentPos);
        while (elem.getAttributes().getAttribute("notEditable") != null && currentPos < docLen) {
            currentPos++;
            elem = doc.getCharacterElement(currentPos);
        }

        if (currentPos == docLen) return -1;

        return currentPos;
    }

    public int getPrevEditableIndex(int currentPos) {
        TranscriptDocument doc = getTranscriptDocument();

        Element elem = doc.getCharacterElement(currentPos);
        while (elem.getAttributes().getAttribute("notEditable") != null && currentPos >= 0) {
            currentPos--;
            elem = doc.getCharacterElement(currentPos);
        }

        return currentPos;
    }

    public void sameOffsetInPrevTierOrElement() {
        TranscriptDocument doc = getTranscriptDocument();

        int caretPos = getCaretPosition();
        int offsetInContent = doc.getOffsetInContent(caretPos);

        int start = getStartOfPrevTierOrElement(caretPos);

        if (start == -1) return;

        int end;

        AttributeSet prevElementAttributes = doc.getCharacterElement(start).getAttributes();

        String elementType = (String) prevElementAttributes.getAttribute("elementType");

        if (elementType == null) {
            return;
        }
        else if (elementType.equals("record")) {
            end = doc.getTierEnd((Tier<?>) prevElementAttributes.getAttribute("tier"));
        }
        else if (elementType.equals("comment")) {
            end = doc.getCommentEnd((Comment) prevElementAttributes.getAttribute("comment"));
        }
        else if (elementType.equals("gem")) {
            end = doc.getGemEnd((Gem) prevElementAttributes.getAttribute("gem"));
        }
        else {
            return;
        }

        int newCaretPos = Math.min(end - 1, start + offsetInContent);

        setCaretPosition(newCaretPos);
    }

    public void sameOffsetInNextTierOrElement() {
        TranscriptDocument doc = getTranscriptDocument();

        int caretPos = getCaretPosition();
        int offsetInContent = doc.getOffsetInContent(caretPos);

        int start = getStartOfNextTierOrElement(caretPos);
        if (start == -1) return;

        int end;

        AttributeSet nextElementAttributes = doc.getCharacterElement(start).getAttributes();

        String elementType = (String) nextElementAttributes.getAttribute("elementType");

        if (elementType == null) {
            return;
        }
        else if (elementType.equals("record")) {
            end = doc.getTierEnd((Tier<?>) nextElementAttributes.getAttribute("tier"));
        }
        else if (elementType.equals("comment")) {
            end = doc.getCommentEnd((Comment) nextElementAttributes.getAttribute("comment"));
        }
        else if (elementType.equals("gem")) {
            end = doc.getGemEnd((Gem) nextElementAttributes.getAttribute("gem"));
        }
        else {
            return;
        }

        int newCaretPos = Math.min(end - 1, start + offsetInContent);

        setCaretPosition(newCaretPos);
    }

    public int getStartOfPrevTierOrElement(int caretPos) {
        TranscriptDocument doc = getTranscriptDocument();
        Transcript transcript = session.getTranscript();
        Element elem = doc.getCharacterElement(caretPos);
        AttributeSet currentPosAttrs = elem.getAttributes();

        int currentTranscriptElementIndex = -1;
        int prevTranscriptElementIndex = -1;
        int prevTierIndex = -1;

        List<TierViewItem> recordVisibleTierView = null;

        // Try to get the current record index
        Integer currentRecordIndex = (Integer) currentPosAttrs.getAttribute("recordIndex");

        // If the record index is null, you're not in a record
        if (currentRecordIndex == null) {
            // Get the type of transcript element you're currently in
            String elementType = (String) currentPosAttrs.getAttribute("elementType");
            // If it's a comment
            if (elementType.equals("comment")) {
                // Get a reference to it and get its transcript element index
                Comment comment = (Comment) currentPosAttrs.getAttribute(elementType);
                currentTranscriptElementIndex = transcript.getElementIndex(comment);
            }
            // If it's a gem
            else if (elementType.equals("gem")) {
                // Get a reference to it and get its transcript element index
                Gem gem = (Gem) currentPosAttrs.getAttribute(elementType);
                currentTranscriptElementIndex = transcript.getElementIndex(gem);
            }
            // If it's neither of those, return -1
            else return -1;
        }
        // Otherwise you're in a record
        else {
            // Get a reference to the record
            Record record = session.getRecord(currentRecordIndex);
            // Get a list of all the visible tiers in the record
            recordVisibleTierView = session.getTierView()
                .stream()
                .filter(item -> record.hasTier(item.getTierName()) && item.isVisible())
                .toList();

            // Get the current tier from the current position attributes
            Tier<?> currentTier = (Tier<?>) currentPosAttrs.getAttribute("tier");

            // Get the index of the current tier among the visible tiers of the record
            int currentTierIndex = -1;
            for (int i = 0; i < recordVisibleTierView.size(); i++) {
                if (recordVisibleTierView.get(i).getTierName().equals(currentTier.getName())) {
                    currentTierIndex = i;
                    break;
                }
            }

            // If it's still -1, return -1
            if (currentTierIndex == -1) {
                return -1;
            }
            // If it's 0
            else if (currentTierIndex == 0) {
                // Get the current element index since we will be moving between elements
                currentTranscriptElementIndex = transcript.getElementIndex(record);
            }
            // If it's not -1 or 0
            else {
                // Set the previous tier index to 1 less than the current tier index
                prevTierIndex = currentTierIndex - 1;
            }
        }

        // If the previous transcript element index is still -1, we're just moving within a record
        if (currentTranscriptElementIndex != -1) {
            // If we're already at the first transcript element, loop back to the last
            if (currentTranscriptElementIndex == 0) {
                prevTranscriptElementIndex = transcript.getNumberOfElements() - 1;
            }
            // Otherwise, just subtract 1
            else {
                prevTranscriptElementIndex = currentTranscriptElementIndex - 1;
            }
        }

        // Movement within a record
        if (currentTranscriptElementIndex == -1) {
            // Get the name of the previous tier
            String prevTierName = recordVisibleTierView.get(prevTierIndex).getTierName();
            // Return the start of the previous tier
            return doc.getTierStart(currentRecordIndex, prevTierName);
        }
        // Movement between elements
        else {
            Transcript.Element prevTranscriptElement = transcript.getElementAt(prevTranscriptElementIndex);
            // Moving into a record
            if (prevTranscriptElement.isRecord()) {
                // Get a reference to the record and get its index
                Record record = prevTranscriptElement.asRecord();
                int recordIndex = transcript.getRecordPosition(record);
                // Get a list of all the visible tiers in the record and use it to get the last visible tier name
                recordVisibleTierView = session.getTierView()
                    .stream()
                    .filter(item -> record.hasTier(item.getTierName()) && item.isVisible())
                    .toList();
                String prevTierName = recordVisibleTierView.get(recordVisibleTierView.size() - 1).getTierName();
                // Return the start of the previous tier
                return doc.getTierStart(recordIndex, prevTierName);
            }
            // Moving to a non-record element
            else {
                // If element is comment
                if (prevTranscriptElement.isComment()) {
                    // Return the start pos of that comment
                    return doc.getCommentStart(prevTranscriptElement.asComment());
                }
                // If element is gem
                else {
                    // Return the start pos of that gem
                    return doc.getGemStart(prevTranscriptElement.asGem());
                }
            }
        }
    }

    public int getStartOfNextTierOrElement(int caretPos) {
        TranscriptDocument doc = getTranscriptDocument();
        Transcript transcript = session.getTranscript();
        Element elem = doc.getCharacterElement(caretPos);
        AttributeSet currentPosAttrs = elem.getAttributes();

        int currentTranscriptElementIndex = -1;
        int nextTranscriptElementIndex = -1;
        int nextTierIndex = -1;

        List<TierViewItem> recordVisibleTierView = null;

        // Try to get the current record index
        Integer currentRecordIndex = (Integer) currentPosAttrs.getAttribute("recordIndex");

        // If the record index is null, you're not in a record
        if (currentRecordIndex == null) {
            // Get the type of transcript element you're currently in
            String elementType = (String) currentPosAttrs.getAttribute("elementType");
            // If it's a comment
            if (elementType.equals("comment")) {
                // Get a reference to it and get its transcript element index
                Comment comment = (Comment) currentPosAttrs.getAttribute(elementType);
                currentTranscriptElementIndex = transcript.getElementIndex(comment);
            }
            // If it's a gem
            else if (elementType.equals("gem")) {
                // Get a reference to it and get its transcript element index
                Gem gem = (Gem) currentPosAttrs.getAttribute(elementType);
                currentTranscriptElementIndex = transcript.getElementIndex(gem);
            }
            // If it's neither of those, return -1
            else return -1;
        }
        // Otherwise you're in a record
        else {
            // Get a reference to the record
            Record record = session.getRecord(currentRecordIndex);
            // Get a list of all the visible tiers in the record
            recordVisibleTierView = session.getTierView()
                .stream()
                .filter(item -> record.hasTier(item.getTierName()) && item.isVisible())
                .toList();

            // Get the current tier from the current position attributes
            Tier<?> currentTier = (Tier<?>) currentPosAttrs.getAttribute("tier");

            // Get the index of the current tier among the visible tiers of the record
            int currentTierIndex = -1;
            for (int i = 0; i < recordVisibleTierView.size(); i++) {
                if (recordVisibleTierView.get(i).getTierName().equals(currentTier.getName())) {
                    currentTierIndex = i;
                    break;
                }
            }

            // If it's still -1, return -1
            if (currentTierIndex == -1) {
                return -1;
            }
            // If it's the last visible tier in the record
            else if (currentTierIndex == recordVisibleTierView.size() - 1) {
                // Get the current element index since we will be moving between elements
                currentTranscriptElementIndex = transcript.getElementIndex(record);
            }
            // If it's not -1 or the last visible tier
            else {
                // Set the next tier index to 1 more than the current tier index
                nextTierIndex = currentTierIndex + 1;
            }
        }

        // If the next transcript element index is still -1, we're just moving within a record
        if (currentTranscriptElementIndex != -1) {
            // If we're already at the last transcript element, loop back to the first
            if (currentTranscriptElementIndex == transcript.getNumberOfElements() - 1) {
                nextTranscriptElementIndex = 0;
            }
            // Otherwise, just add 1
            else {
                nextTranscriptElementIndex = currentTranscriptElementIndex + 1;
            }
        }

        // Movement within a record
        if (currentTranscriptElementIndex == -1) {
            // Get the name of the next tier
            String nextTierName = recordVisibleTierView.get(nextTierIndex).getTierName();
            // Return the start of the next tier
            return doc.getTierStart(currentRecordIndex, nextTierName);
        }
        // Movement between elements
        else {
            Transcript.Element nextTranscriptElement = transcript.getElementAt(nextTranscriptElementIndex);
            // Moving into a record
            if (nextTranscriptElement.isRecord()) {
                // Get a reference to the record and get its index
                Record record = nextTranscriptElement.asRecord();
                int recordIndex = transcript.getRecordPosition(record);
                // Get a list of all the visible tiers in the record and use it to get the first visible tier name
                recordVisibleTierView = session.getTierView()
                    .stream()
                    .filter(item -> record.hasTier(item.getTierName()) && item.isVisible())
                    .toList();
                String nextTierName = recordVisibleTierView.get(0).getTierName();
                // Return the start of the next tier
                return doc.getTierStart(recordIndex, nextTierName);
            }
            // Moving to a non-record element
            else {
                // If element is comment
                if (nextTranscriptElement.isComment()) {
                    // Return the start pos of that comment
                    return doc.getCommentStart(nextTranscriptElement.asComment());
                }
                // If element is gem
                else {
                    // Return the start pos of that gem
                    return doc.getGemStart(nextTranscriptElement.asGem());
                }
            }
        }
    }

    public boolean isSyllabificationVisible() {
        return getTranscriptDocument().isSyllabificationVisible();
    }

    public void setSyllabificationVisible(boolean visible) {
        TranscriptDocument doc = getTranscriptDocument();

        var oldVal = doc.isSyllabificationVisible();
        doc.setSyllabificationVisible(visible);

        super.firePropertyChange("syllabificationVisible", oldVal, visible);
    }

    public boolean isSyllabificationComponent() {
        return getTranscriptDocument().isSyllabificationComponent();
    }

    public void setSyllabificationIsComponent(boolean isComponent) {
        TranscriptDocument doc = getTranscriptDocument();

        var oldVal = doc.isSyllabificationComponent();
        doc.setSyllabificationIsComponent(isComponent);

        super.firePropertyChange("syllabificationIsComponent", oldVal, isComponent);
    }

    public boolean isAlignmentVisible() {
        return getTranscriptDocument().isAlignmentVisible();
    }

    public void setAlignmentIsVisible(boolean visible) {
        TranscriptDocument doc = getTranscriptDocument();

        var oldVal = doc.isAlignmentVisible();
        doc.setAlignmentVisible(visible);

        super.firePropertyChange("alignmentVisible", oldVal, visible);
    }

    public boolean isAlignmentComponent() {
        return getTranscriptDocument().isSyllabificationComponent();
    }

    public void setAlignmentIsComponent(boolean isComponent) {
        TranscriptDocument doc = getTranscriptDocument();

        var oldVal = doc.isSyllabificationComponent();
        doc.setAlignmentIsComponent(isComponent);

        super.firePropertyChange("alignmentIsComponent", oldVal, isComponent);
    }

    public EditorEventManager getEventManager() {
        return eventManager;
    }
}