package ca.phon.app.session.intervalTiers;

import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.TransliterationDictionaryProvider;
import ca.phon.session.*;
import ca.phon.syllabifier.*;
import ca.phon.ui.SyllabifierSelector;
import ca.phon.util.Language;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Settings panel for importing an interval tier to an IPA tier.
 */
public class IntervalTierToIPATierSettingsPanel extends JPanel {
    
    private static final Logger LOGGER = Logger.getLogger(IntervalTierToIPATierSettingsPanel.class.getName());

    private final Session session;
    private final IntervalTierToIPATierSettings settings;
    
    private JTextField intervalTierNameField;
    private JTextField recordTierNameField;
    private SyllabifierSelector syllabifierSelector;
    private JComboBox<String> transliterationSchemeComboBox;
    private JCheckBox deleteIntervalTierCheckbox;
    
    public IntervalTierToIPATierSettingsPanel(Session session, IntervalTierToIPATierSettings settings) {
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
        add(new JLabel("IPA Tier Name:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        recordTierNameField = new JTextField(settings.recordTierName() != null ? settings.recordTierName() : "", 20);
        add(recordTierNameField, gbc);
        
        // Syllabifier language
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        syllabifierSelector = new SyllabifierSelector();
        if (settings.language() != null && !settings.language().isEmpty()) {
            syllabifierSelector.setSelectedLanguage(Language.parseLanguage(settings.language()));
        }
        JScrollPane syllabifierScroller = new JScrollPane(syllabifierSelector);
        syllabifierScroller.setBorder(BorderFactory.createTitledBorder("Syllabifier Language"));
        syllabifierScroller.setPreferredSize(new Dimension(400, 150));
        add(syllabifierScroller, gbc);
        
        // Transliteration scheme
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0.0;
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

        // Delete interval tier option
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        deleteIntervalTierCheckbox = new JCheckBox("Delete session-level interval tier after import");
        deleteIntervalTierCheckbox.setSelected(false);
        add(deleteIntervalTierCheckbox, gbc);
    }
    
    public IntervalTierToIPATierSettings getSettings() {
        String language = null;
        if (syllabifierSelector.getSelectedSyllabifier() != null) {
            language = syllabifierSelector.getSelectedSyllabifier().getLanguage().toString();
        }
        
        String transliterationScheme = (String) transliterationSchemeComboBox.getSelectedItem();
        if(transliterationScheme != null && transliterationScheme.trim().isEmpty()) {
            transliterationScheme = null;
        }

        return new IntervalTierToIPATierSettings(
            intervalTierNameField.getText().trim(),
            recordTierNameField.getText().trim(),
            language,
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
        if (recordTierNameField.getText().trim().isEmpty()) {
            return "IPA tier name cannot be empty";
        }
        return null;
    }
}
