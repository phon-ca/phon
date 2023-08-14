package ca.phon.app.session.editor.transcriptEditor;

import ca.phon.app.project.DesktopProject;
import ca.phon.session.Session;
import ca.phon.session.Tier;
import ca.phon.ui.action.PhonUIAction;
import org.jdesktop.swingx.HorizontalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;

public class TranscriptEditor extends JEditorPane {
    public TranscriptEditor(Session session) {
        super();
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
            TranscriptDocument doc = ((TranscriptDocument)editorPane.getDocument());
            doc.setSetCaretPosCallback((pos) -> editorPane.setCaretPosition(pos));
            doc.setGetCaretPosCallback(() -> editorPane.getCaretPosition());
            /*editorPane.addCaretListener(e -> {
                int cursorPos = e.getDot();
                int recordIndex = doc.getRecordIndex(cursorPos);
                int recordElementIndex = doc.getRecordElementIndex(cursorPos);
                Tier tier = doc.getTier(cursorPos);
                String tierName = tier != null ? tier.getName() : "null";
                System.out.println("Record " + recordIndex + " (Element: " + recordElementIndex + ") : " + tierName);
                System.out.println("Cursor Pos: " + cursorPos);
            });*/
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
    }

    public void nextTierOrElement() {
        System.out.println("Testing");
    }
}
