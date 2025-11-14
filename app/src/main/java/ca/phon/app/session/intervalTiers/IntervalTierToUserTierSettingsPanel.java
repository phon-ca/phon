package ca.phon.app.session.intervalTiers;

import ca.phon.session.Session;
import ca.phon.session.UserTierType;

import javax.swing.*;
import java.awt.*;

/**
 * Settings panel for importing an interval tier to a user-defined tier.
 */
public class IntervalTierToUserTierSettingsPanel extends JPanel {
    
    private final Session session;
    private final IntervalTierToUserTierSettings settings;
    
    private JComboBox<String> intervalTierNameComboBox;
    private JComboBox<String> recordTierNameField;
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
        intervalTierNameComboBox = new JComboBox<>();
        intervalTierNameComboBox.setEditable(false);
        for (String tierName : session.getTimeline().getTierNames()) {
            intervalTierNameComboBox.addItem(tierName);
        }
        if (settings.intervalTierName() != null && !settings.intervalTierName().isEmpty()) {
            intervalTierNameComboBox.setSelectedItem(settings.intervalTierName());
        }
        add(intervalTierNameComboBox, gbc);

        // Record tier name
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(new JLabel("User tier:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        recordTierNameField = new JComboBox<>();
        recordTierNameField.setEditable(true);

        // Populate with UserTierType values
        for (UserTierType tierType : UserTierType.values()) {
            recordTierNameField.addItem(tierType.getPhonTierName());
        }

        // Set custom renderer to show "Phon name (CHAT name)"
        recordTierNameField.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null && index >= 0) {
                    String phonTierName = value.toString();
                    UserTierType tierType = UserTierType.fromPhonTierName(phonTierName);
                    if (tierType != null && !tierType.getChatTierName().isEmpty()) {
                        setText(phonTierName + " (" + tierType.getChatTierName() + ")");
                    }
                }
                return this;
            }
        });

        // Set initial value from settings
        if (settings.recordTierName() != null && !settings.recordTierName().isEmpty()) {
            recordTierNameField.setSelectedItem(settings.recordTierName());
        }

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
        String intervalTierName = (String) intervalTierNameComboBox.getSelectedItem();
        if (intervalTierName != null) {
            intervalTierName = intervalTierName.trim();
        }

        String recordTierName = (String) recordTierNameField.getSelectedItem();
        if (recordTierName != null) {
            recordTierName = recordTierName.trim();
        } else {
            recordTierName = "";
        }

        return new IntervalTierToUserTierSettings(
            intervalTierName,
            recordTierName,
            includeIntervalTextCheckbox.isSelected()
        );
    }
    
    public boolean shouldDeleteIntervalTier() {
        return deleteIntervalTierCheckbox.isSelected();
    }
    
    public String validateSettings() {
        String intervalTierName = (String) intervalTierNameComboBox.getSelectedItem();
        if (intervalTierName == null || intervalTierName.trim().isEmpty()) {
            return "Interval tier name cannot be empty";
        }
        String recordTierName = (String) recordTierNameField.getSelectedItem();
        if (recordTierName == null || recordTierName.trim().isEmpty()) {
            return "User tier name cannot be empty";
        }
        return null;
    }
}
