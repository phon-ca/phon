package ca.phon.app.session.intervalTiers;

import ca.phon.orthography.TerminatorType;
import ca.phon.session.Session;

import javax.swing.*;
import java.awt.*;

/**
 * Settings panel for importing an interval tier to an orthography tier.
 */
public class IntervalTierToOrthographySettingsPanel extends JPanel {
    
    private final Session session;
    private final IntervalTierToOrthographySettings settings;
    
    private JTextField intervalTierNameField;
    private JCheckBox importWorTierCheckbox;
    private JCheckBox addTerminatorCheckbox;
    private JComboBox<TerminatorType> terminatorTypeCombo;
    private JCheckBox deleteIntervalTierCheckbox;
    
    public IntervalTierToOrthographySettingsPanel(Session session, IntervalTierToOrthographySettings settings) {
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
        
        // Import WOR tier checkbox
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        importWorTierCheckbox = new JCheckBox("Import to word tier (WOR) as well");
        importWorTierCheckbox.setSelected(settings.importWorTier());
        add(importWorTierCheckbox, gbc);
        
        // Add terminator checkbox
        gbc.gridy++;
        addTerminatorCheckbox = new JCheckBox("Add terminator");
        addTerminatorCheckbox.setSelected(settings.addTerminator());
        addTerminatorCheckbox.addActionListener(e -> terminatorTypeCombo.setEnabled(addTerminatorCheckbox.isSelected()));
        add(addTerminatorCheckbox, gbc);
        
        // Terminator type
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(new JLabel("Terminator Type:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        terminatorTypeCombo = new JComboBox<>(TerminatorType.values());
        terminatorTypeCombo.setSelectedItem(settings.terminatorType());
        terminatorTypeCombo.setEnabled(settings.addTerminator());
        terminatorTypeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TerminatorType) {
                    setText(((TerminatorType) value).getDisplayName());
                }
                return this;
            }
        });
        add(terminatorTypeCombo, gbc);
        
        // Delete interval tier option
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        deleteIntervalTierCheckbox = new JCheckBox("Delete session-level interval tier after import");
        deleteIntervalTierCheckbox.setSelected(false);
        add(deleteIntervalTierCheckbox, gbc);
    }
    
    public IntervalTierToOrthographySettings getSettings() {
        return new IntervalTierToOrthographySettings(
            intervalTierNameField.getText().trim(),
            importWorTierCheckbox.isSelected(),
            addTerminatorCheckbox.isSelected(),
            (TerminatorType) terminatorTypeCombo.getSelectedItem()
        );
    }
    
    public boolean shouldDeleteIntervalTier() {
        return deleteIntervalTierCheckbox.isSelected();
    }
    
    public String validateSettings() {
        if (intervalTierNameField.getText().trim().isEmpty()) {
            return "Interval tier name cannot be empty";
        }
        return null;
    }
}
