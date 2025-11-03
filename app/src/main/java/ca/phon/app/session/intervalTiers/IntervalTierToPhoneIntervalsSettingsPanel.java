package ca.phon.app.session.intervalTiers;

import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.TransliterationDictionaryProvider;
import ca.phon.session.Session;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

/**
 * Settings panel for importing an interval tier to phone intervals.
 */
public class IntervalTierToPhoneIntervalsSettingsPanel extends JPanel {
    
    private static final Logger LOGGER = Logger.getLogger(IntervalTierToPhoneIntervalsSettingsPanel.class.getName());

    private final Session session;
    private final IntervalTierToPhoneIntervalsSettings settings;
    
    private JTextField intervalTierNameField;
    private JComboBox<String> transliterationSchemeComboBox;
    private JCheckBox deleteIntervalTierCheckbox;
    
    public IntervalTierToPhoneIntervalsSettingsPanel(Session session, IntervalTierToPhoneIntervalsSettings settings) {
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
        
        // Transliteration scheme
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(new JLabel("Transliteration Scheme:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        transliterationSchemeComboBox = new JComboBox<>();
        transliterationSchemeComboBox.addItem(""); // Empty option for no transliteration

        TransliterationDictionaryProvider provider = new TransliterationDictionaryProvider();
        for(IPADictionary dict : provider) {
            transliterationSchemeComboBox.addItem(dict.getName());
        }

        if(settings.transliterationScheme() != null && !settings.transliterationScheme().isEmpty()) {
            transliterationSchemeComboBox.setSelectedItem(settings.transliterationScheme());
        }

        add(transliterationSchemeComboBox, gbc);

        // Info label
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel infoLabel = new JLabel("<html><i>Note: The word tier (WOR) must exist and be populated for phone interval import.</i></html>");
        add(infoLabel, gbc);
        
        // Delete interval tier option
        gbc.gridy++;
        deleteIntervalTierCheckbox = new JCheckBox("Delete session-level interval tier after import");
        deleteIntervalTierCheckbox.setSelected(false);
        add(deleteIntervalTierCheckbox, gbc);
    }
    
    public IntervalTierToPhoneIntervalsSettings getSettings() {
        String transliterationScheme = (String) transliterationSchemeComboBox.getSelectedItem();
        if(transliterationScheme != null && transliterationScheme.trim().isEmpty()) {
            transliterationScheme = null;
        }

        return new IntervalTierToPhoneIntervalsSettings(
            intervalTierNameField.getText().trim(),
            transliterationScheme
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
