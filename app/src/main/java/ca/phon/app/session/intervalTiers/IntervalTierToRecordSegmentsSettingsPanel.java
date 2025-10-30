package ca.phon.app.session.intervalTiers;

import ca.phon.session.Participant;
import ca.phon.session.Session;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

/**
 * Settings panel for importing an interval tier to record segments.
 */
public class IntervalTierToRecordSegmentsSettingsPanel extends JPanel {
    
    private final Session session;
    private final IntervalTierToRecordSegmentsSettings settings;
    
    private JTextField intervalTierNameField;
    private JCheckBox groupContiguousIntervalsCheckbox;
    private JFormattedTextField maxGapLengthField;
    private JFormattedTextField paddingField;
    private JComboBox<Participant> speakerCombo;
    private JCheckBox overwriteExistingRecordsCheckbox;
    private JCheckBox deleteIntervalTierCheckbox;
    
    public IntervalTierToRecordSegmentsSettingsPanel(Session session, IntervalTierToRecordSegmentsSettings settings) {
        super();
        this.session = session;
        this.settings = settings;
        
        init();
    }
    
    private void init() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Interval tier name
        add(new JLabel("Interval Tier Name:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        intervalTierNameField = new JTextField(settings.intervalTierName() != null ? settings.intervalTierName() : "", 20);
        add(intervalTierNameField, gbc);
        
        // Speaker
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(new JLabel("Speaker:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        speakerCombo = new JComboBox<>();
        for (Participant participant : session.getParticipants()) {
            speakerCombo.addItem(participant);
        }
        speakerCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Participant) {
                    setText(((Participant) value).getName());
                }
                return this;
            }
        });
        if (settings.speaker() != null) {
            speakerCombo.setSelectedItem(settings.speaker());
        }
        add(speakerCombo, gbc);
        
        // Group contiguous intervals
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        groupContiguousIntervalsCheckbox = new JCheckBox("Group contiguous intervals");
        groupContiguousIntervalsCheckbox.setSelected(settings.groupContiguousIntervals());
        groupContiguousIntervalsCheckbox.addActionListener(e -> {
            boolean enabled = groupContiguousIntervalsCheckbox.isSelected();
            maxGapLengthField.setEnabled(enabled);
        });
        add(groupContiguousIntervalsCheckbox, gbc);
        
        // Max gap length
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(new JLabel("Max Gap Length (s):"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumFractionDigits(1);
        numberFormat.setMaximumFractionDigits(3);
        maxGapLengthField = new JFormattedTextField(numberFormat);
        maxGapLengthField.setValue(settings.maxGapLength());
        maxGapLengthField.setColumns(10);
        maxGapLengthField.setEnabled(settings.groupContiguousIntervals());
        add(maxGapLengthField, gbc);
        
        // Padding
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(new JLabel("Padding (s):"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        paddingField = new JFormattedTextField(numberFormat);
        paddingField.setValue(settings.padding());
        paddingField.setColumns(10);
        add(paddingField, gbc);
        
        // Overwrite existing records
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        overwriteExistingRecordsCheckbox = new JCheckBox("Overwrite existing records");
        overwriteExistingRecordsCheckbox.setSelected(settings.overwriteExistingRecords());
        add(overwriteExistingRecordsCheckbox, gbc);
        
        // Delete interval tier option
        gbc.gridy++;
        deleteIntervalTierCheckbox = new JCheckBox("Delete session-level interval tier after import");
        deleteIntervalTierCheckbox.setSelected(false);
        add(deleteIntervalTierCheckbox, gbc);
    }
    
    public IntervalTierToRecordSegmentsSettings getSettings() {
        float maxGapLength = 0.0f;
        if (maxGapLengthField.getValue() instanceof Number) {
            maxGapLength = ((Number) maxGapLengthField.getValue()).floatValue();
        }
        
        float padding = 0.0f;
        if (paddingField.getValue() instanceof Number) {
            padding = ((Number) paddingField.getValue()).floatValue();
        }
        
        return new IntervalTierToRecordSegmentsSettings(
            intervalTierNameField.getText().trim(),
            groupContiguousIntervalsCheckbox.isSelected(),
            maxGapLength,
            padding,
            (Participant) speakerCombo.getSelectedItem(),
            overwriteExistingRecordsCheckbox.isSelected()
        );
    }
    
    public boolean shouldDeleteIntervalTier() {
        return deleteIntervalTierCheckbox.isSelected();
    }
    
    public String validateSettings() {
        if (intervalTierNameField.getText().trim().isEmpty()) {
            return "Interval tier name cannot be empty";
        }
        if (speakerCombo.getSelectedItem() == null) {
            return "Speaker must be selected";
        }
        
        // Validate numeric fields
        try {
            if (maxGapLengthField.getValue() instanceof Number) {
                float maxGap = ((Number) maxGapLengthField.getValue()).floatValue();
                if (maxGap < 0) {
                    return "Max gap length must be non-negative";
                }
            }
        } catch (Exception e) {
            return "Invalid max gap length value";
        }
        
        try {
            if (paddingField.getValue() instanceof Number) {
                float padding = ((Number) paddingField.getValue()).floatValue();
                if (padding < 0) {
                    return "Padding must be non-negative";
                }
            }
        } catch (Exception e) {
            return "Invalid padding value";
        }
        
        return null;
    }
}
