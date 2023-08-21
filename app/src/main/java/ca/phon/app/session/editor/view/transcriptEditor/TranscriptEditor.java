package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.project.DesktopProject;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SegmentPlayback;
import ca.phon.plugin.PluginManager;
import ca.phon.session.Session;
import ca.phon.session.Tier;
import ca.phon.session.TierViewItem;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import org.jdesktop.swingx.HorizontalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TranscriptEditor extends JEditorPane {
    private final Session session;
    private final EditorEventManager eventManager;
    private SegmentPlayback segmentPlayback;

    public TranscriptEditor(Session session, EditorEventManager eventManager) {
        super();
        this.session = session;
        this.eventManager = eventManager;
        initActions();
        registerEditorActions();
        super.setEditorKitForContentType(TranscriptEditorKit.CONTENT_TYPE, new TranscriptEditorKit(session));
        setContentType(TranscriptEditorKit.CONTENT_TYPE);
        // FOR DEBUG PURPOSES ONLY
        addCaretListener(e -> {
            TranscriptDocument doc = getTranscriptDocument();
            int cursorPos = e.getDot();
            int recordIndex = doc.getRecordIndex(cursorPos);
            int recordElementIndex = doc.getRecordElementIndex(cursorPos);
            Tier tier = doc.getTier(cursorPos);
            String tierName = tier != null ? tier.getName() : "null";
            System.out.println("Record " + recordIndex + " (Element: " + recordElementIndex + ") : " + tierName);
            System.out.println("Cursor Pos: " + cursorPos);
            System.out.println(doc.getRecordEnd(recordIndex, null));
        });
    }

    public TranscriptEditor(Session session) {
        this(session, new EditorEventManager(null));
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
        var startTierView = data.newTierView();
        var endTierView = data.oldTierView();

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
        else {
            // Put the caret back where it was before the move
            setCaretPosition(startCaretPos);
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
        int caretTierOffset = -1;

        if (caretTier != null) {
            caretTierOffset = startCaretPos - elem.getStartOffset();
        }


        getTranscriptDocument().deleteTier(data.tierNames());


        // Correct caret
        if (caretTierOffset == -1) {
            // Put the caret back where it was before the move
            setCaretPosition(startCaretPos);
        }
        else {
            int caretTierIndex = data
                .oldTierView()
                .stream()
                .map(item -> item.getTierName())
                .toList()
                .indexOf(caretTier.getName());
            boolean caretTierWasLast = caretTierIndex == data.oldTierView().size() - 1;
            boolean caretTierWasDeleted = data
                .tierNames()
                .stream()
                .anyMatch(tierName -> tierName.equals(caretTier.getName()));
            boolean caretTierWasMoved = movedTiers
                .stream()
                .anyMatch(item -> item.getTierName().equals(caretTier.getName()));

            if (caretTierWasDeleted) {
                if (caretTierWasLast) {

                }
                else {

                }
            }
            else if (caretTierWasMoved) {

            }
        }



        if (caretTierOffset > -1) {
            // Move the caret so that it has the same offset from the tiers new pos
            setCaretPosition(doc.getTierStart(caretTier) + caretTierOffset);
        }
        else {

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
        getTranscriptDocument().hideTier(data.tierNames());
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
        getTranscriptDocument().showTier(shownTiers);

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
    }

    public void removeEditorActions() {
        this.eventManager.removeActionForEvent(EditorEventType.SessionChanged, this::onSessionChanged);
        this.eventManager.removeActionForEvent(EditorEventType.TierViewChanged, this::onTierViewChanged);
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
            default -> {}
        }
    }

    public SegmentPlayback getSegmentPlayback() {
        return segmentPlayback;
    }

    public void setSegmentPlayback(SegmentPlayback segmentPlayback) {
        this.segmentPlayback = segmentPlayback;
    }
}
