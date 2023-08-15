package ca.phon.app.session.editor.transcriptEditor;

import ca.phon.app.project.DesktopProject;
import ca.phon.session.Session;
import ca.phon.session.Tier;
import ca.phon.session.TierViewItem;
import ca.phon.ui.action.PhonUIAction;
import org.jdesktop.swingx.HorizontalLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TranscriptEditor extends JEditorPane {
    private final Session session;

    public TranscriptEditor(Session session) {
        super();
        this.session = session;
        initActions();
        super.setEditorKitForContentType(TranscriptEditorKit.CONTENT_TYPE, new TranscriptEditorKit(session));
        setContentType(TranscriptEditorKit.CONTENT_TYPE);
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
            TranscriptDocument doc = (TranscriptDocument)editorPane.getDocument();
            doc.setOpenLabelPopup((tierLabel, e) -> editorPane.createTierLabelPopup(tierLabel, e));

            editorPane.addCaretListener(e -> {
                int cursorPos = e.getDot();
                int recordIndex = doc.getRecordIndex(cursorPos);
                int recordElementIndex = doc.getRecordElementIndex(cursorPos);
                Tier tier = doc.getTier(cursorPos);
                String tierName = tier != null ? tier.getName() : "null";
                System.out.println("Record " + recordIndex + " (Element: " + recordElementIndex + ") : " + tierName);
                System.out.println("Cursor Pos: " + cursorPos);
            });
            frame.setSize(350, 275);
            frame.setLayout(new BorderLayout());
            JScrollPane scrollPane = new JScrollPane(editorPane);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            frame.add(scrollPane, BorderLayout.CENTER);
            JPanel bottomButtonPanel = new JPanel(new HorizontalLayout());

            /*JButton debugButton = new JButton("Debug");
            debugButton.addActionListener(e -> {
                ((TranscriptEditorKit)editorPane.getEditorKit()).debugInfo();
            });
            bottomButtonPanel.add(debugButton);*/

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
        return ((TranscriptDocument)getDocument()).getTargetSyllablesVisible();
    }
    public boolean getActualSyllablesVisible() {
        return ((TranscriptDocument)getDocument()).getActualSyllablesVisible();
    }
    public boolean getAlignmentVisible() {
        return ((TranscriptDocument)getDocument()).getAlignmentVisible();
    }
    public void setTargetSyllablesVisible(boolean visible) {
        ((TranscriptDocument)getDocument()).setTargetSyllablesVisible(visible);
    }
    public void setActualSyllablesVisible(boolean visible) {
        ((TranscriptDocument)getDocument()).setActualSyllablesVisible(visible);
    }
    public void setAlignmentVisible(boolean visible) {
        ((TranscriptDocument)getDocument()).setAlignmentVisible(visible);
    }

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
        TranscriptDocument doc = (TranscriptDocument) getDocument();

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
        TranscriptDocument doc = (TranscriptDocument) getDocument();

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

    private void createTierLabelPopup(JLabel tierLabel, MouseEvent mouseEvent) {
        JPopupMenu menu = new JPopupMenu();

        var tierView = session.getTierView();
        String tierName = tierLabel.getText().substring(0, tierLabel.getText().length()-1);
        var selectedTierView = tierView
                .stream()
                .filter(tv -> tv.getTierName().equals(tierName))
                .findFirst()
                .orElse(null);
        int tierStartIndex = tierView.indexOf(selectedTierView);

        JMenuItem hideTier = new JMenuItem("Hide tier");
        menu.add(hideTier);
        hideTier.addActionListener(e -> hideTier(tierStartIndex, tierName));

        // If it's not the top tier in a record
        if (tierStartIndex > 0) {
            JMenuItem moveUp = new JMenuItem("Move up");
            menu.add(moveUp);
            moveUp.addActionListener(e -> moveTierUp(tierStartIndex));
        }

        // If it's not the bottom tier in a record
        if (tierStartIndex < tierView.size() - 1) {
            JMenuItem moveDown = new JMenuItem("Move down");
            menu.add(moveDown);
            moveDown.addActionListener(e -> moveTierDown(tierStartIndex));
        }

        menu.show(tierLabel, mouseEvent.getX(), mouseEvent.getY());
    }

    // region Hide/Move Tiers

    private void hideTier(int tierStartIndex, String tierName) {
        var startingTierView = session.getTierView();
        TranscriptDocument doc = (TranscriptDocument) getDocument();

        int startCaretPos = getCaretPosition();
        var elem = doc.getCharacterElement(startCaretPos);
        Tier caretTier = (Tier)elem.getAttributes().getAttribute("tier");

        int caretTierOffset = -1;
        // If the caret has a valid tier and record index
        if (caretTier != null) {
            String caretTierName = caretTier.getName();
            var caretTierView = startingTierView
                .stream()
                .filter(tv -> tv.getTierName().equals(caretTierName))
                .findFirst()
                .orElse(null);
            int caretTierIndex = startingTierView.indexOf(caretTierView);
            // If the caret is on a later line
            if (caretTierIndex > tierStartIndex) {
                caretTierOffset = startCaretPos - elem.getStartOffset();
            }
        }

        // Hide the tier in the document
        doc.hideTier(tierStartIndex, tierName);

        if (caretTierOffset > -1) {
            // Move the caret so that it has the same offset from the tiers new pos
            setCaretPosition(doc.getTierStart(caretTier) + caretTierOffset);
        }
        else {
            // Put the caret back where it was before the move
            setCaretPosition(startCaretPos);
        }
    }
    private void moveTierUp(int tierStartIndex) {
        var startingTierView = session.getTierView();
        TranscriptDocument doc = (TranscriptDocument) getDocument();
        String movingTierName = startingTierView.get(tierStartIndex).getTierName();
        String aboveTierName = startingTierView.get(tierStartIndex - 1).getTierName();

        int startCaretPos = getCaretPosition();
        var elem = doc.getCharacterElement(startCaretPos);
        Tier caretTier = (Tier)elem.getAttributes().getAttribute("tier");
        int caretTierOffset = -1;

        if (caretTier != null) {
            String caretTierName = caretTier.getName();

            if (caretTierName.equals(movingTierName) || caretTierName.equals(aboveTierName)) {
                caretTierOffset = startCaretPos - elem.getStartOffset();
            }
        }

        // Move the tier in the document
        doc.moveTierUp(tierStartIndex, movingTierName, aboveTierName);

        if (caretTierOffset > -1) {
            // Move the caret so that it has the same offset from the tiers new pos
            setCaretPosition(doc.getTierStart(caretTier) + caretTierOffset);
        }
        else {
            // Put the caret back where it was before the move
            setCaretPosition(startCaretPos);
        }
    }
    private void moveTierDown(int tierStartIndex) {
        var startingTierView = session.getTierView();
        TranscriptDocument doc = (TranscriptDocument) getDocument();
        String movingTierName = startingTierView.get(tierStartIndex).getTierName();
        String belowTierName = startingTierView.get(tierStartIndex + 1).getTierName();

        int startCaretPos = getCaretPosition();
        var elem = doc.getCharacterElement(startCaretPos);
        Tier caretTier = (Tier)elem.getAttributes().getAttribute("tier");
        int caretTierOffset = -1;

        if (caretTier != null) {
            String caretTierName = caretTier.getName();
            if (caretTierName.equals(movingTierName) || caretTierName.equals(belowTierName)) {
                caretTierOffset = startCaretPos - elem.getStartOffset();
            }
        }

        // Move the tier in the document
        doc.moveTierDown(tierStartIndex, movingTierName, belowTierName);

        if (caretTierOffset > -1) {
            // Move the caret so that it has the same offset from the tiers new pos
            setCaretPosition(doc.getTierStart(caretTier) + caretTierOffset);
        }
        else {
            // Put the caret back where it was before the move
            setCaretPosition(startCaretPos);
        }
    }

    // endregion Hide/Move Tiers

}
