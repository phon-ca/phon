package ca.phon.app.session.intervalTiers;

import ca.phon.fontconv.TranscriptConverter;
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
    
    private JComboBox<String> intervalTierNameComboBox;
    private JComboBox<String> transliterationSchemeComboBox;
    private JComboBox<String> fontConversionSchemeComboBox;
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
        add(new JLabel("Interval tier:"), gbc);
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

        // Transliteration scheme
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(new JLabel("Transliteration scheme:"), gbc);
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

        // Font conversion scheme
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(new JLabel("Font conversion scheme:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        fontConversionSchemeComboBox = new JComboBox<>();
        fontConversionSchemeComboBox.addItem(""); // Empty option for no font conversion

        for(String converterName : TranscriptConverter.getAvailableConverterNames()) {
            fontConversionSchemeComboBox.addItem(converterName);
        }

        if(settings.fontConversionScheme() != null && !settings.fontConversionScheme().isEmpty()) {
            fontConversionSchemeComboBox.setSelectedItem(settings.fontConversionScheme());
        }

        add(fontConversionSchemeComboBox, gbc);

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

        String fontConversionScheme = (String) fontConversionSchemeComboBox.getSelectedItem();
        if(fontConversionScheme != null && fontConversionScheme.trim().isEmpty()) {
            fontConversionScheme = null;
        }

        String intervalTierName = (String) intervalTierNameComboBox.getSelectedItem();
        if (intervalTierName != null) {
            intervalTierName = intervalTierName.trim();
        }

        return new IntervalTierToPhoneIntervalsSettings(
            intervalTierName,
            transliterationScheme,
            fontConversionScheme
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
