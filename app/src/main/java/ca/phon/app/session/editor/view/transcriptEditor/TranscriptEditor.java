package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.ChangeSpeakerEdit;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.plugin.PluginManager;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.OSInfo;
import ca.phon.util.Tuple;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
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
    private int prevMousePosInDoc = -1;
    private Element hoverElem = null;
    private Object currentUnderline;
    TranscriptUnderlinePainter underlinePainter = new TranscriptUnderlinePainter();
    private MediaSegment selectedSegment = null;

    public TranscriptEditor(
        Session session,
        EditorEventManager eventManager,
        SessionEditUndoSupport undoSupport,
        UndoManager undoManager
    ) {
        super();
        final TranscriptEditorCaret caret = new TranscriptEditorCaret();
        setCaret(caret);
        getCaret().deinstall(this);
        caret.install(this);
        this.session = session;
        this.eventManager = eventManager;
        this.undoSupport = undoSupport;
        this.undoManager = undoManager;
        initActions();
        registerEditorActions();
        super.setEditorKitForContentType(TranscriptEditorKit.CONTENT_TYPE, new TranscriptEditorKit());
        setContentType(TranscriptEditorKit.CONTENT_TYPE);
        setOpaque(false);
        setNavigationFilter(new TranscriptNavigationFilter());
        TranscriptMouseAdapter mouseAdapter = new TranscriptMouseAdapter();
        addMouseMotionListener(mouseAdapter);
        addMouseListener(mouseAdapter);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (selectedSegment != null && e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (segmentPlayback != null) {
                        segmentPlayback.playSegment(selectedSegment);
                    }
                }
            }
        });
        addCaretListener(e -> {
            TranscriptDocument doc = getTranscriptDocument();
            String transcriptElementType = (String) doc.getCharacterElement(e.getDot()).getAttributes().getAttribute("elementType");
            if (transcriptElementType != null && transcriptElementType.equals("record")) {
                setCurrentRecordIndex(doc.getRecordIndex(e.getDot()));
            }

            // FOR DEBUG PURPOSES ONLY
//            SimpleAttributeSet attrs = new SimpleAttributeSet(doc.getCharacterElement(e.getDot()).getAttributes().copyAttributes());
//            System.out.println("Font size: " + StyleConstants.getFontSize(attrs));
            System.out.println("Dot: " + e.getDot());
//            System.out.println(attrs);
        });
    }

    public TranscriptEditor(Session session) {
        this(session, new EditorEventManager(), new SessionEditUndoSupport(), new UndoManager());
    }


    // region Init

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
        PhonUIAction<Void> rightAct = PhonUIAction.runnable(() -> setCaretPosition(getNextEditableIndex(getCaretPosition() + 1, true)));
        actionMap.put("nextEditableIndex", rightAct);

        KeyStroke left = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
        inputMap.put(left, "prevEditableIndex");
        PhonUIAction<Void> leftAct = PhonUIAction.runnable(() -> setCaretPosition(getPrevEditableIndex(getCaretPosition() - 1, true)));
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

    private void registerEditorActions() {
        this.eventManager.registerActionForEvent(EditorEventType.SessionChanged, this::onSessionChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
        this.eventManager.registerActionForEvent(EditorEventType.TierViewChanged, this::onTierViewChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
        this.eventManager.registerActionForEvent(EditorEventType.RecordChanged, this::onRecordChanged, EditorEventManager.RunOn.AWTEventDispatchThread);

        this.eventManager.registerActionForEvent(EditorEventType.RecordAdded, this::onRecordAdded, EditorEventManager.RunOn.AWTEventDispatchThread);
        this.eventManager.registerActionForEvent(EditorEventType.RecordDeleted, this::onRecordDeleted, EditorEventManager.RunOn.AWTEventDispatchThread);
        this.eventManager.registerActionForEvent(EditorEventType.RecordMoved, this::onRecordMoved, EditorEventManager.RunOn.AWTEventDispatchThread);

        this.eventManager.registerActionForEvent(EditorEventType.SpeakerChanged, this::onSpeakerChanged, EditorEventManager.RunOn.AWTEventDispatchThread);

        this.eventManager.registerActionForEvent(EditorEventType.TierChanged, this::onTierDataChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
    }

    // endregion Init


    // region Getters and Setters

    public TranscriptDocument getTranscriptDocument() {
        return (TranscriptDocument) getDocument();
    }

    public void setAlignmentVisible(boolean visible) {
        getTranscriptDocument().setAlignmentVisible(visible);
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

    public void setCurrentRecordIndex(int index) {
        int oldIndex = this.currentRecordIndex;
        this.currentRecordIndex = index;
        super.firePropertyChange("currentRecordIndex", oldIndex, this.currentRecordIndex);
    }

    public boolean isSingleRecordView() {
        return singleRecordView;
    }

    public void setSingleRecordView(boolean singleRecordView) {
        this.singleRecordView = singleRecordView;
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

    public MediaSegment getSelectedSegment() {
        return selectedSegment;
    }

    public void setSelectedSegment(MediaSegment selectedSegment) {
        this.selectedSegment = selectedSegment;
    }

    // endregion Getters and Setters


    // region Input Actions

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


    // region Tier View Changes

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

    public void moveTier(EditorEventType.TierViewChangedData data) {
        long startTimeMS = new Date().getTime();

        var startTierView = data.oldTierView();
        var endTierView = data.newTierView();

        System.out.println(startTierView.stream().map(item -> item.getTierName()).toList());
        System.out.println(endTierView.stream().map(item -> item.getTierName()).toList());

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

        Document blank = getEditorKit().createDefaultDocument();
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



        Document blank = getEditorKit().createDefaultDocument();
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

        Document blank = getEditorKit().createDefaultDocument();
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

        Document blank = getEditorKit().createDefaultDocument();
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

        Document blank = getEditorKit().createDefaultDocument();
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

        int caretPos = getCaretPosition();

        TranscriptDocument doc = getTranscriptDocument();
        Document blank = getEditorKit().createDefaultDocument();
        setDocument(blank);
        doc.tierNameChanged(oldTiers, newTiers);
        setDocument(doc);

        setCaretPosition(caretPos);
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

        Document blank = getEditorKit().createDefaultDocument();
        setDocument(blank);
        // Change tier font in doc
        doc.tierFontChanged(changedTiers);
        setDocument(doc);

        setCaretPosition(caretPos);
    }

    // endregion Tier View Changes


    // region Record Changes

    private void onRecordChanged(EditorEvent<EditorEventType.RecordChangedData> editorEvent) {
        TranscriptDocument doc = getTranscriptDocument();

        // Update the single record index in the doc
        doc.setSingleRecordIndex(editorEvent.data().recordIndex());

        // If it's currently in single record view fire the appropriate event
        if (doc.getSingleRecordView()) {
            final EditorEvent<Void> e = new EditorEvent<>(recordChangedInSingleRecordMode, this, null);
            eventManager.queueEvent(e);
        }

        // If the transcript editor is currently in focus, stop here
        if (hasFocus()) return;

        try {
            // Get rects for the start and end positions of the record
            int recordStartPos = doc.getRecordStart(editorEvent.data().recordIndex());
            int recordEndPos = doc.getRecordEnd(editorEvent.data().recordIndex());
            var startRect = modelToView2D(recordStartPos);
            var endRect = modelToView2D(recordEndPos);

            // Create a rect that contains the whole record
            Rectangle scrollToRect = new Rectangle(
                (int) startRect.getMinX(),
                (int) startRect.getMinY(),
                (int) (endRect.getMaxX() - startRect.getMinX()),
                (int) (endRect.getMaxY() - startRect.getMinY())
            );
            // Scroll to a point where that new rect is visible
            super.scrollRectToVisible(scrollToRect);
        }
        catch (BadLocationException e) {
            LogUtil.severe(e);
        }
    }

    private void onRecordAdded(EditorEvent<EditorEventType.RecordAddedData> editorEvent) {
        var data = editorEvent.data();
        // Get the new record
        Record addedRecord = session.getRecord(data.recordIndex());
        // Add it to the doc
        getTranscriptDocument().addRecord(addedRecord);
    }

    private void onRecordDeleted(EditorEvent<EditorEventType.RecordDeletedData> editorEvent) {
        // Record caret pos
        int caretPos = getCaretPosition();

        // Delete the record from the doc
        var data = editorEvent.data();
        getTranscriptDocument().deleteRecord(data.recordIndex(), data.elementIndex());

        // Set the caret to the editable pos closest to the original pos
        setCaretPosition(getNextEditableIndex(caretPos, false));
    }

    private void onRecordMoved(EditorEvent<EditorEventType.RecordMovedData> editorEvent) {
        // Record caret pos
        int caretPos = getCaretPosition();

        // Move the records in the doc
        var data = editorEvent.data();
        getTranscriptDocument().moveRecord(
            data.fromRecordIndex(),
            data.toRecordIndex(),
            data.fromElementIndex(),
            data.toElementIndex()
        );

        // Set the caret to the editable pos closest to the original pos
        setCaretPosition(getNextEditableIndex(caretPos, false));
    }

    private void onSpeakerChanged(EditorEvent<EditorEventType.SpeakerChangedData> editorEvent) {
        var data = editorEvent.data();
        // Update the speaker on the separator in the doc
        getTranscriptDocument().changeSpeaker(data.record());
    }

    private void onTierDataChanged(EditorEvent<EditorEventType.TierChangeData> editorEvent) {
        var data = editorEvent.data();
        // Update the changed tier data in the doc
        getTranscriptDocument().onTierDataChanged(data.tier());
    }

    // endregion Record Changes


    // region On Click

    private void onClickTierLabel(Point2D point) {
        // Build a new popup menu
        JPopupMenu menu = new JPopupMenu();
        MenuBuilder builder = new MenuBuilder(menu);

        var extPts = PluginManager.getInstance().getExtensionPoints(TierLabelMenuHandler.class);

        for (var extPt : extPts) {
            var menuHandler = extPt.getFactory().createObject();
            menuHandler.addMenuItems(builder);
        }

        // Show it where the user clicked
        menu.show(this, (int) point.getX(), (int) point.getY());
    }

    private void onClickCommentLabel(Point2D point) {
        // Build a new popup menu
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

        // Show it where the user clicked
        menu.show(this, (int) point.getX(), (int) point.getY());
    }

    private void onClickGemLabel(Point2D point) {
        // Build a new popup menu
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

        // Show it where the user clicked
        menu.show(this, (int) point.getX(), (int) point.getY());
    }

    // endregion On Click


    @Override
    protected void paintComponent(Graphics g) {
        TranscriptDocument doc = getTranscriptDocument();
        // Get the clip bounds of the current view
        Rectangle drawHere = g.getClipBounds();

        // Fill the background with the appropriate color
        g.setColor(UIManager.getColor(TranscriptEditorUIProps.BACKGROUND));
        g.fillRect(0, drawHere.y, drawHere.width, drawHere.height);

        // Fill the label column background with the appropriate color
        g.setColor(UIManager.getColor(TranscriptEditorUIProps.LABEL_BACKGROUND));
        FontMetrics fontMetrics = g.getFontMetrics(FontPreferences.getMonospaceFont().deriveFont(14.0f));
        char[] template = new char[getTranscriptDocument().getLabelColumnWidth() + 1];
        Arrays.fill(template, ' ');
        int labelColWidth = fontMetrics.stringWidth(new String(template));
        Rectangle labelColRect = new Rectangle(0, 0, labelColWidth, getHeight());
        if (labelColRect.intersects(drawHere)) {
            g.fillRect(0, (int) drawHere.getMinY(), labelColWidth, drawHere.height);
        }

        g.setColor(UIManager.getColor(TranscriptEditorUIProps.SEPARATOR_LINE));
        int sepLineHeight = 1;
        int fontHeight = fontMetrics.getHeight();
        Element root = doc.getDefaultRootElement();
        if (root.getElementCount() == 0) return;
        float lineSpacing = StyleConstants.getLineSpacing(root.getElement(0).getAttributes());
        int sepLineOffset = (int) (((fontHeight * lineSpacing) + sepLineHeight) / 2);
        // For every element
        for (int i = 0; i < root.getElementCount(); i++) {
            Element elem = root.getElement(i);
            if (elem.getElementCount() == 0) continue;
            Element innerElem = elem.getElement(0);
            AttributeSet attrs = innerElem.getAttributes();
            // If it's a separator
            if (attrs.getAttribute("sep") != null) {
                try {
                    var sepRect = modelToView2D(innerElem.getStartOffset());
                    if (sepRect == null) continue;
                    boolean topVisible = sepRect.getMinY() >= drawHere.getMinY() && sepRect.getMinY() <= drawHere.getMaxY();
                    boolean bottomVisible = sepRect.getMaxY() >= drawHere.getMinY() && sepRect.getMaxY() <= drawHere.getMaxY();
                    // And it's onscreen
                    if (!topVisible && !bottomVisible) continue;
                    // Draw the separator line
                    g.fillRect(
                        drawHere.x,
                        ((int) sepRect.getMinY()) - sepLineOffset,
                        drawHere.width,
                        sepLineHeight
                    );
                } catch (BadLocationException e) {
                    LogUtil.severe(e);
                }
            }
        }

        super.paintComponent(g);
    }

    public void removeEditorActions() {
        this.eventManager.removeActionForEvent(EditorEventType.SessionChanged, this::onSessionChanged);
        this.eventManager.removeActionForEvent(EditorEventType.TierViewChanged, this::onTierViewChanged);
        this.eventManager.removeActionForEvent(EditorEventType.RecordChanged, this::onRecordChanged);
    }

    private void onSessionChanged(EditorEvent<Session> editorEvent) {

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

    public void loadSession() {
        TranscriptDocument doc = (TranscriptDocument) getEditorKit().createDefaultDocument();
        doc.setSession(session);
        setDocument(doc);
    }

    public int getNextEditableIndex(int currentPos, boolean looping) {
        TranscriptDocument doc = getTranscriptDocument();

        int docLen = doc.getLength();

        Element elem = doc.getCharacterElement(currentPos);
        while (elem.getAttributes().getAttribute("notEditable") != null && currentPos < docLen) {
            currentPos++;
            elem = doc.getCharacterElement(currentPos);
        }

        if (currentPos == docLen) {
            if (looping) {
                return getNextEditableIndex(0, true);
            }
            else {
                return -1;
            }
        }

        return currentPos;
    }

    public int getPrevEditableIndex(int currentPos, boolean looping) {
        TranscriptDocument doc = getTranscriptDocument();

        Element elem = doc.getCharacterElement(currentPos);
        while (elem.getAttributes().getAttribute("notEditable") != null && currentPos >= 0) {
            currentPos--;
            elem = doc.getCharacterElement(currentPos);
        }

        if (looping && currentPos < 0) {
            return getPrevEditableIndex(doc.getLength()-1, true);
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

        List<String> recordVisibleTierNames = new ArrayList<>();

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
            recordVisibleTierNames.addAll(session.getTierView()
                .stream()
                .filter(item -> record.hasTier(item.getTierName()) && item.isVisible())
                .map(item -> item.getTierName())
                .toList()
            );

            int ipaTargetIndex = recordVisibleTierNames.indexOf("IPA Target");
            if (ipaTargetIndex != -1 && isSyllabificationVisible()) {
                recordVisibleTierNames.add(ipaTargetIndex + 1, SystemTierType.TargetSyllables.getName());
            }
            int ipaActualIndex = recordVisibleTierNames.indexOf("IPA Actual");
            if (ipaActualIndex != -1 && isSyllabificationVisible()) {
                recordVisibleTierNames.add(ipaActualIndex + 1, SystemTierType.ActualSyllables.getName());
            }

            if (isAlignmentVisible()) {
                int alignmentIndex = recordVisibleTierNames.indexOf(doc.getAlignmentTierView().getTierName()) + 1;
                recordVisibleTierNames.add(alignmentIndex, SystemTierType.PhoneAlignment.getName());
            }

            // Get the current tier from the current position attributes
            Tier<?> currentTier = (Tier<?>) currentPosAttrs.getAttribute("tier");
            // Get the index of the current tier among the visible tiers of the record
            int currentTierIndex = recordVisibleTierNames.indexOf(currentTier.getName());

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
            String prevTierName = recordVisibleTierNames.get(prevTierIndex);
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
                recordVisibleTierNames.addAll(
                    session.getTierView()
                        .stream()
                        .filter(item -> record.hasTier(item.getTierName()) && item.isVisible())
                        .map(item -> item.getTierName())
                        .toList()
                );

                int ipaTargetIndex = recordVisibleTierNames.indexOf("IPA Target");
                if (ipaTargetIndex != -1 && isSyllabificationVisible()) {
                    recordVisibleTierNames.add(ipaTargetIndex + 1, SystemTierType.TargetSyllables.getName());
                }
                int ipaActualIndex = recordVisibleTierNames.indexOf("IPA Actual");
                if (ipaActualIndex != -1 && isSyllabificationVisible()) {
                    recordVisibleTierNames.add(ipaActualIndex + 1, SystemTierType.ActualSyllables.getName());
                }

                if (isAlignmentVisible()) {
                    int alignmentIndex = recordVisibleTierNames.indexOf(doc.getAlignmentTierView().getTierName()) + 1;
                    recordVisibleTierNames.add(alignmentIndex, SystemTierType.PhoneAlignment.getName());
                }

                String prevTierName = recordVisibleTierNames.get(recordVisibleTierNames.size() - 1);
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

        List<String> recordVisibleTierNames = new ArrayList<>();

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
            recordVisibleTierNames.addAll(
                session.getTierView()
                    .stream()
                    .filter(item -> record.hasTier(item.getTierName()) && item.isVisible())
                    .map(item -> item.getTierName())
                    .toList()
            );

            int ipaTargetIndex = recordVisibleTierNames.indexOf("IPA Target");
            if (ipaTargetIndex != -1 && isSyllabificationVisible()) {
                recordVisibleTierNames.add(ipaTargetIndex + 1, SystemTierType.TargetSyllables.getName());
            }
            int ipaActualIndex = recordVisibleTierNames.indexOf("IPA Actual");
            if (ipaActualIndex != -1 && isSyllabificationVisible()) {
                recordVisibleTierNames.add(ipaActualIndex + 1, SystemTierType.ActualSyllables.getName());
            }

            if (isAlignmentVisible()) {
                int alignmentIndex = recordVisibleTierNames.indexOf(doc.getAlignmentTierView().getTierName()) + 1;
                recordVisibleTierNames.add(alignmentIndex, SystemTierType.PhoneAlignment.getName());
            }

            // Get the current tier from the current position attributes
            Tier<?> currentTier = (Tier<?>) currentPosAttrs.getAttribute("tier");
            // Get the index of the current tier among the visible tiers of the record
            int currentTierIndex = recordVisibleTierNames.indexOf(currentTier.getName());

            // If it's still -1, return -1
            if (currentTierIndex == -1) {
                return -1;
            }
            // If it's the last visible tier in the record
            else if (currentTierIndex == recordVisibleTierNames.size() - 1) {
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
            String nextTierName = recordVisibleTierNames.get(nextTierIndex);
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
                recordVisibleTierNames.addAll(
                    session.getTierView()
                        .stream()
                        .filter(item -> record.hasTier(item.getTierName()) && item.isVisible())
                        .map(item -> item.getTierName())
                        .toList()
                );

                int ipaTargetIndex = recordVisibleTierNames.indexOf("IPA Target");
                if (ipaTargetIndex != -1 && isSyllabificationVisible()) {
                    recordVisibleTierNames.add(ipaTargetIndex + 1, SystemTierType.TargetSyllables.getName());
                }
                int ipaActualIndex = recordVisibleTierNames.indexOf("IPA Actual");
                if (ipaActualIndex != -1 && isSyllabificationVisible()) {
                    recordVisibleTierNames.add(ipaActualIndex + 1, SystemTierType.ActualSyllables.getName());
                }

                if (isAlignmentVisible()) {
                    int alignmentIndex = recordVisibleTierNames.indexOf(doc.getAlignmentTierView().getTierName()) + 1;
                    recordVisibleTierNames.add(alignmentIndex, SystemTierType.PhoneAlignment.getName());
                }

                String nextTierName = recordVisibleTierNames.get(0);
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

    private void underlineElement(Element elem) {
        try {
            removeCurrentUnderline();
            currentUnderline = getHighlighter().addHighlight(
                elem.getStartOffset(),
                elem.getEndOffset(),
                underlinePainter
            );
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void removeCurrentUnderline() {
        if (currentUnderline != null) {
            getHighlighter().removeHighlight(currentUnderline);
            currentUnderline = null;
        }
    }


    private class TranscriptNavigationFilter extends NavigationFilter {
        @Override
        public void setDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
            setSelectedSegment(null);
            TranscriptDocument doc = getTranscriptDocument();
            Element elem = doc.getCharacterElement(dot);
            AttributeSet attrs = elem.getAttributes();
            boolean isLabel = attrs.getAttribute("label") != null;
            boolean isSegment = attrs.getAttribute("mediaSegment") != null;

            if (isLabel && !isSegment) return;

            fb.setDot(dot, bias);
        }
        @Override
        public void moveDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
            fb.moveDot(dot, bias);
        }
    }

    private class TranscriptUnderlinePainter implements Highlighter.HighlightPainter {
        @Override
        public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
            try {
                var firstCharRect = modelToView2D(p0);
                var lastCharRect = modelToView2D(p1);
                g.setColor(UIManager.getColor(TranscriptEditorUIProps.CLICKABLE_HOVER_UNDERLINE));
                int lineY = ((int) firstCharRect.getMaxY()) - 9;
                g.drawLine((int) firstCharRect.getMinX(), lineY, (int) lastCharRect.getMaxX(), lineY);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class TranscriptMouseAdapter extends MouseAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            TranscriptDocument doc = getTranscriptDocument();

            if (controlPressed) {
                highlightElementAtPoint(e.getPoint());
            }

            int mousePosInDoc = viewToModel2D(e.getPoint());

            Element elem = doc.getCharacterElement(mousePosInDoc);
            if (elem != null) {
                if (elem.equals(hoverElem)) return;
                AttributeSet attrs = elem.getAttributes();
                boolean isLabel = attrs.getAttribute("clickable") != null;
                boolean isWhitespace = doc.getCharAtPos(mousePosInDoc).equals(' ');
                if (isLabel && !isWhitespace) {
                    hoverElem = elem;
                    underlineElement(elem);
                    return;
                }
            }
            if (hoverElem != null) {
                hoverElem = null;
                removeCurrentUnderline();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            TranscriptDocument doc = getTranscriptDocument();
            int mousePosInDoc = viewToModel2D(e.getPoint());

            Element elem = doc.getCharacterElement(mousePosInDoc);
            AttributeSet attrs = elem.getAttributes();

            MediaSegment mediaSegment = (MediaSegment) attrs.getAttribute("mediaSegment");
            if (mediaSegment != null) {
                var segmentBounds = doc.getSegmentBounds(mediaSegment, elem);
                System.out.println("Segment bounds: " + segmentBounds);
                setCaretPosition(segmentBounds.getObj1());
                setSelectedSegment(mediaSegment);
                moveCaretPosition(segmentBounds.getObj2()+1);
                return;
            }

            if (attrs.getAttribute("label") != null) {
                String elementType = (String) attrs.getAttribute("elementType");
                if (elementType != null) {
                    if (e.getClickCount() > 1) {
                        switch (elementType) {
                            case "record" -> {
                                Tier<?> tier = (Tier<?>) attrs.getAttribute("tier");
                                select(doc.getTierStart(tier), doc.getTierEnd(tier));
                            }
                            case "comment" -> {
                                Comment comment = (Comment) attrs.getAttribute("comment");
                                select(doc.getCommentStart(comment), doc.getCommentEnd(comment));
                            }
                            case "gem" -> {
                                Gem gem = (Gem) attrs.getAttribute("gem");
                                select(doc.getGemStart(gem), doc.getGemEnd(gem));
                            }
                        }
                    }
                    else {
                        switch (elementType) {
                            case "record" -> setCaretPosition(doc.getTierStart((Tier<?>) attrs.getAttribute("tier")));
                            case "comment" -> setCaretPosition(doc.getCommentStart((Comment) attrs.getAttribute("comment")));
                            case "gem" -> setCaretPosition(doc.getGemStart((Gem) attrs.getAttribute("gem")));
                        }
                    }

                    if (attrs.getAttribute("clickable") != null) {
                        switch (elementType) {
                            case "record" -> onClickTierLabel(e.getPoint());
                            case "comment" -> onClickCommentLabel(e.getPoint());
                            case "gem" -> onClickGemLabel(e.getPoint());
                        }
                    }
                }
            }



//            if (controlPressed) {
//                if (tier != null && tier.getValue() instanceof MediaSegment mediaSegment) {
//                    if (segmentPlayback != null) {
//                        segmentPlayback.playSegment(mediaSegment);
//                    }
//                }
//            }
        }
    }
}