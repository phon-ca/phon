package ca.phon.app.session.editor.view.transcript;

import ca.phon.session.Tier;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;

public class TranscriptStatusBar extends JPanel {

    private JLabel docPositionLabel;
    private JLabel transcriptElementIndexLabel;
    private JLabel recordIndexLabel;
    private JLabel tierNameLabel;
    private JLabel offsetInContentLabel;

    /**
     * Constructor
     *
     * @param transcriptEditor the {@link TranscriptEditor} that the status bar will be connected to
     * */
    public TranscriptStatusBar(TranscriptEditor transcriptEditor) {
        initUI();
        transcriptEditor.addCaretListener(e -> {
            docPositionLabel.setText("Document position: " + e.getDot());

            // Transcript element index
            try {
                int transcriptElementIndex = transcriptEditor.getCurrentElementIndex();
                transcriptElementIndexLabel.setText("Transcript element index: " + (transcriptElementIndex + 1));
            }
            catch (Exception exception) {
                transcriptElementIndexLabel.setText("Transcript element index: ");
            }

            // Record index
            try {
                int recordIndex = transcriptEditor.getCurrentRecordIndex();
                recordIndexLabel.setText("Record index: " + (recordIndex + 1));
            }
            catch (Exception exception) {
                recordIndexLabel.setText("Record index: ");
            }


            // Tier (if available)
            Tier<?> tier = transcriptEditor.getTranscriptDocument().getTier(e.getDot());
            if (tier == null) {
                tierNameLabel.setText("Tier: ");
            }
            else {
                tierNameLabel.setText("Tier: " + tier.getName());
            }

            // Offset in content
            int offsetInContent = transcriptEditor.getTranscriptDocument().getOffsetInContent(e.getDot());
            if (offsetInContent == -1) {
                offsetInContentLabel.setText("Character: ");
            }
            else {
                offsetInContentLabel.setText("Character: " + offsetInContent);
            }
        });
    }

    /**
     * Sets up the UI for the status bar
     * */
    private void initUI() {

        FormLayout layout = new FormLayout("80dlu, 80dlu, 50dlu, 50dlu, 50dlu", "pref");
        final CellConstraints cc = new CellConstraints();

        setLayout(layout);

        final Font font = Font.decode("monospace-PLAIN-10");

        docPositionLabel = new JLabel("Document position: ");
        docPositionLabel.setFont(font);
        add(docPositionLabel, cc.xy(1,1));

        transcriptElementIndexLabel = new JLabel("Transcript element index: ");
        transcriptElementIndexLabel.setFont(font);
        add(transcriptElementIndexLabel, cc.xy(2,1));

        recordIndexLabel = new JLabel("Record index: ");
        recordIndexLabel.setFont(font);
        add(recordIndexLabel, cc.xy(3,1));

        tierNameLabel = new JLabel("Tier: ");
        tierNameLabel.setFont(font);
        add(tierNameLabel, cc.xy(4,1));

        offsetInContentLabel = new JLabel("Character: ");
        offsetInContentLabel.setFont(font);
        add(offsetInContentLabel, cc.xy(5,1));
    }
}
