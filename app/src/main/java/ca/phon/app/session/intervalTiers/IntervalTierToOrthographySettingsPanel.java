package ca.phon.app.session.intervalTiers;

import ca.phon.orthography.TerminatorType;
import ca.phon.session.Session;
import ca.phon.session.UserTierType;

import javax.swing.*;
import java.awt.*;

/**
 * Settings panel for importing an interval tier to an orthography tier.
 */
public class IntervalTierToOrthographySettingsPanel extends JPanel {
    
    private final Session session;
    private final IntervalTierToOrthographySettings settings;
    
    private JComboBox<String> intervalTierNameComboBox;
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
        add(new JLabel("Interval tier Name:"), gbc);
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

        // Import WOR tier checkbox
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        importWorTierCheckbox = new JCheckBox("Populate " + UserTierType.Wor.getPhonTierName() + " (" + UserTierType.Wor.getChatTierName() + ")" + " tier as well");
        importWorTierCheckbox.setSelected(settings.importWorTier());
        add(importWorTierCheckbox, gbc);
        
        // Add terminator checkbox
        gbc.gridy++;
        addTerminatorCheckbox = new JCheckBox("Add terminator if missing");
        addTerminatorCheckbox.setSelected(settings.addTerminator());
        addTerminatorCheckbox.addActionListener(e ->  {
            terminatorTypeCombo.setEnabled(addTerminatorCheckbox.isSelected());
            if(terminatorTypeCombo.getSelectedIndex() == -1) {
                terminatorTypeCombo.setSelectedItem(TerminatorType.PERIOD);
            }
        });
        add(addTerminatorCheckbox, gbc);
        
        // Terminator type
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(new JLabel("Terminator type:"), gbc);
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
                    final TerminatorType tt = (TerminatorType) value;
                    final String text = tt.getText() + " (" + tt.getDisplayName() + ")";
                    setText(text);
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
        String intervalTierName = (String) intervalTierNameComboBox.getSelectedItem();
        if (intervalTierName != null) {
            intervalTierName = intervalTierName.trim();
        }

        return new IntervalTierToOrthographySettings(
            intervalTierName,
            importWorTierCheckbox.isSelected(),
            addTerminatorCheckbox.isSelected(),
            (TerminatorType) terminatorTypeCombo.getSelectedItem()
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
        return null;
    }
}
