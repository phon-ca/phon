package ca.phon.app.session.intervalTiers;

import ca.phon.app.session.RecordFilterSelectorPanel;
import ca.phon.fontconv.TranscriptConverter;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.TransliterationDictionaryProvider;
import ca.phon.project.Project;
import ca.phon.session.*;
import ca.phon.syllabifier.*;
import ca.phon.ui.SyllabifierSelector;
import ca.phon.util.Language;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Settings panel for importing an interval tier to an IPA tier.
 */
public class IntervalTierToIPATierSettingsPanel extends JPanel {
    
    private static final Logger LOGGER = Logger.getLogger(IntervalTierToIPATierSettingsPanel.class.getName());

    private final Session session;
    private final IntervalTierToIPATierSettings settings;
    
    private JComboBox<String> intervalTierNameComboBox;
    private JComboBox<String> recordTierNameComboBox;
    private SyllabifierSelector syllabifierSelector;
    private JComboBox<String> transliterationSchemeComboBox;
    private JComboBox<String> fontConversionSchemeComboBox;
    private JCheckBox deleteIntervalTierCheckbox;
    private RecordFilterSelectorPanel recordFilterSelector;

    public IntervalTierToIPATierSettingsPanel(Session session, IntervalTierToIPATierSettings settings) {
        this(session, settings, null);
    }
    
    public IntervalTierToIPATierSettingsPanel(Session session, IntervalTierToIPATierSettings settings, Project project) {
        super();
        this.session = session;
        this.settings = settings;
        
        init(project);
    }
    
    private void init(Project project) {
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
        add(new JLabel("IPA tier:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        final List<String> recordTierNames = new ArrayList<>();
        recordTierNames.add(SystemTierType.IPATarget.getName());
        recordTierNames.add(SystemTierType.IPAActual.getName());
        for(TierDescription td:session.getUserTiers()) {
            if(td.getDeclaredType() == IPATranscript.class) {
                recordTierNames.add(td.getName());
            }
        }
        recordTierNameComboBox = new JComboBox<>(recordTierNames.toArray(new String[recordTierNames.size()]));
        recordTierNameComboBox.setEditable(false);
        if (settings.recordTierName() != null && !settings.recordTierName().isEmpty()) {
            recordTierNameComboBox.setSelectedItem(settings.recordTierName());
        }
        add(recordTierNameComboBox, gbc);

        // Syllabifier language
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        syllabifierSelector = new SyllabifierSelector();
        final JScrollPane syllabifierScroller = new JScrollPane(syllabifierSelector);
        if (settings.language() != null && !settings.language().isEmpty()) {
            syllabifierSelector.setSelectedLanguage(Language.parseLanguage(settings.language()));
        } else {
            Syllabifier syllabifier = SyllabifierOptions.findSyllabifier(session, null, settings.recordTierName());
            if (syllabifier != null) {
                syllabifierSelector.setSelectedLanguage(syllabifier.getLanguage());
            } else {
                syllabifierSelector.setSelectedLanguage(SyllabifierLibrary.getInstance().defaultSyllabifierLanguage());
            }
            SwingUtilities.invokeLater(() -> {
                Language lang = syllabifierSelector.getSelectedSyllabifier().getLanguage();
                if(syllabifierSelector.getSelectedIndex() >= 0) {
                    var p = syllabifierSelector.indexToLocation(syllabifierSelector.getSelectedIndex());
                    syllabifierScroller.getViewport().setViewPosition(new Point(0, p.y - 10));
                }
            });
        }
        syllabifierScroller.setBorder(BorderFactory.createTitledBorder("Syllabifier Language"));
        syllabifierScroller.setPreferredSize(new Dimension(400, 150));
        add(syllabifierScroller, gbc);
        
        // Transliteration scheme
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0.0;
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
        add(new JLabel("Font conversion Scheme:"), gbc);
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

        // Delete interval tier option
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        deleteIntervalTierCheckbox = new JCheckBox("Delete session-level interval tier after import");
        deleteIntervalTierCheckbox.setSelected(false);
        add(deleteIntervalTierCheckbox, gbc);
        
        // Record filter selector
        if(project != null) {
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            recordFilterSelector = new RecordFilterSelectorPanel(project, session);
            recordFilterSelector.setBorder(BorderFactory.createTitledBorder("Record Filter"));
            if(settings.recordFilter() != null) {
                recordFilterSelector.setRecordFilter(settings.recordFilter());
            }
            add(recordFilterSelector, gbc);
        }
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

        String fontConversionScheme = (String) fontConversionSchemeComboBox.getSelectedItem();
        if(fontConversionScheme != null && fontConversionScheme.trim().isEmpty()) {
            fontConversionScheme = null;
        }

        String intervalTierName = (String) intervalTierNameComboBox.getSelectedItem();
        if (intervalTierName != null) {
            intervalTierName = intervalTierName.trim();
        }

        return new IntervalTierToIPATierSettings(
            intervalTierName,
            (String) recordTierNameComboBox.getSelectedItem(),
            language,
            transliterationScheme,
            fontConversionScheme,
            recordFilterSelector != null ? recordFilterSelector.getRecordFilter() : null
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
        if (recordTierNameComboBox.getSelectedItem() == null) {
            return "IPA tier name cannot be empty";
        }
        return null;
    }
}
