package ca.phon.app.session.intervalTiers;

import ca.phon.session.Session;

import javax.swing.*;
import java.awt.*;

/**
 * Settings panel for importing an interval tier to a user-defined tier.
 */
public class IntervalTierToUserTierSettingsPanel extends JPanel {
    
    private final Session session;
    private final IntervalTierToUserTierSettings settings;
    
    private JTextField intervalTierNameField;
    private JTextField recordTierNameField;
    private JCheckBox includeIntervalTextCheckbox;
    private JCheckBox deleteIntervalTierCheckbox;
    
    public IntervalTierToUserTierSettingsPanel(Session session, IntervalTierToUserTierSettings settings) {
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
        
        // Record tier name
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(new JLabel("User Tier Name:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        recordTierNameField = new JTextField(settings.recordTierName() != null ? settings.recordTierName() : "", 20);
        add(recordTierNameField, gbc);
        
        // Include interval text checkbox
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        includeIntervalTextCheckbox = new JCheckBox("Include interval timing information");
        includeIntervalTextCheckbox.setSelected(settings.includeIntervalText());
        add(includeIntervalTextCheckbox, gbc);
        
        // Delete interval tier option
        gbc.gridy++;
        deleteIntervalTierCheckbox = new JCheckBox("Delete session-level interval tier after import");
        deleteIntervalTierCheckbox.setSelected(false);
        add(deleteIntervalTierCheckbox, gbc);
    }
    
    public IntervalTierToUserTierSettings getSettings() {
        return new IntervalTierToUserTierSettings(
            intervalTierNameField.getText().trim(),
            recordTierNameField.getText().trim(),
            includeIntervalTextCheckbox.isSelected()
        );
    }
    
    public boolean shouldDeleteIntervalTier() {
        return deleteIntervalTierCheckbox.isSelected();
    }
    
    public String validateSettings() {
        if (intervalTierNameField.getText().trim().isEmpty()) {
            return "Interval tier name cannot be empty";
        }
        if (recordTierNameField.getText().trim().isEmpty()) {
            return "User tier name cannot be empty";
        }
        return null;
    }
}
