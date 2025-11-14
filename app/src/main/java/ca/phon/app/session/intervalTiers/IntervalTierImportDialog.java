package ca.phon.app.session.intervalTiers;

import ca.phon.app.session.RecordFilterPanel;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.RemoveTimelineTierEdit;
import ca.phon.session.IntervalTier;
import ca.phon.session.Session;
import ca.phon.session.filter.RecordFilter;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Generic dialog for importing interval tier data with configurable settings panel,
 * importer, and validation.
 *
 * @param <T> Settings type
 * @param <R> Importer type extending IntervalTierImporter
 */
public class IntervalTierImportDialog<T, R extends IntervalTierImporter> extends JDialog {
    
    private final SessionEditor editor;
    private final JPanel settingsPanel;
    private final Function<T, R> importerFactory;
    private final Supplier<T> settingsSupplier;
    private final Supplier<String> validationSupplier;
    private final Supplier<Boolean> shouldDeleteTierSupplier;
    private final String intervalTierName;
    private final boolean showRecordFilterButtons;

    private boolean wasCancelled = true;
    private RecordFilter recordFilter = null;
    private JButton resetFilterButton;

    /**
     * Create a new interval tier import dialog.
     *
     * @param owner the parent frame
     * @param editor the session editor
     * @param title dialog title
     * @param description dialog description
     * @param intervalTierName name of the interval tier being imported
     * @param settingsPanel the settings panel
     * @param settingsSupplier supplier to get settings from the panel
     * @param importerFactory factory to create importer from settings
     * @param validationSupplier supplier to validate settings (returns null if valid, error message otherwise)
     * @param shouldDeleteTierSupplier supplier to check if tier should be deleted after import
     * @param showRecordFilterButtons whether to show record filter buttons
     */
    public IntervalTierImportDialog(
            Frame owner,
            SessionEditor editor,
            String title,
            String description,
            String intervalTierName,
            JPanel settingsPanel,
            Supplier<T> settingsSupplier,
            Function<T, R> importerFactory,
            Supplier<String> validationSupplier,
            Supplier<Boolean> shouldDeleteTierSupplier,
            boolean showRecordFilterButtons) {
        super(owner, title, true);
        this.editor = editor;
        this.intervalTierName = intervalTierName;
        this.settingsPanel = settingsPanel;
        this.settingsSupplier = settingsSupplier;
        this.importerFactory = importerFactory;
        this.validationSupplier = validationSupplier;
        this.shouldDeleteTierSupplier = shouldDeleteTierSupplier;
        this.showRecordFilterButtons = showRecordFilterButtons;

        init(title, description);
    }
    
    private void init(String title, String description) {
        setLayout(new BorderLayout());

        setModal(false);

        // Header
        DialogHeader header = new DialogHeader(title, description);
        add(header, BorderLayout.NORTH);
        
        // Settings panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.add(settingsPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
        
        // Button bar
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(e -> onOk());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> onCancel());
        
        JButton filterButton = new JButton("Select records...");
        filterButton.addActionListener(e -> showRecordFilterDialog());
        filterButton.setVisible(showRecordFilterButtons);

        resetFilterButton = new JButton("Reset filter");
        resetFilterButton.addActionListener(e -> resetRecordFilter());
        resetFilterButton.setVisible(false);

        final JComponent buttonBar = ButtonBarBuilder.buildOkCancelBar(okButton, cancelButton, filterButton, resetFilterButton);
        buttonBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        add(buttonBar, BorderLayout.SOUTH);
        
        getRootPane().setDefaultButton(okButton);
        
        pack();
        setLocationRelativeTo(getOwner());
    }
    
    private void showRecordFilterDialog() {
        JDialog filterDialog = new JDialog(this, "Select records", true);
        filterDialog.setLayout(new BorderLayout());

        RecordFilterPanel filterPanel = new RecordFilterPanel(editor.getProject(), editor.getSession());
        filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        filterDialog.add(filterPanel, BorderLayout.CENTER);

        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> {
            if (filterPanel.validatePanel()) {
                recordFilter = filterPanel.getRecordFilter();
                updateFilterStatus();
                filterDialog.setVisible(false);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> filterDialog.setVisible(false));

        JComponent buttonBar = ButtonBarBuilder.buildOkCancelBar(applyButton, cancelButton);
        buttonBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        filterDialog.add(buttonBar, BorderLayout.SOUTH);

        filterDialog.pack();
        filterDialog.setLocationRelativeTo(this);
        filterDialog.setVisible(true);
    }

    private void resetRecordFilter() {
        recordFilter = null;
        updateFilterStatus();
    }

    private void updateFilterStatus() {
        resetFilterButton.setVisible(showRecordFilterButtons && recordFilter != null);
    }

    private void onOk() {
        // Validate settings
        String validationError = validationSupplier.get();
        if (validationError != null) {
            JOptionPane.showMessageDialog(this,
                validationError,
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get settings
        T settings = settingsSupplier.get();
        
        // Create importer
        R importer = importerFactory.apply(settings);
        
        // Import tier
        editor.getUndoSupport().beginUpdate("Import interval tier: " + intervalTierName);
        try {
            Session session = editor.getSession();

            int startIndex = 0;
            if(importer instanceof IntervalTierToRecordSegments) {
                // Start after existing records if not overwriting
                IntervalTierToRecordSegments segmentImporter = (IntervalTierToRecordSegments)importer;
                if(!segmentImporter.getSettings().overwriteExistingRecords()) {
                    startIndex = session.getRecordCount();
                }
            }

            // Use record filter if set, otherwise accept all records
            RecordFilter filter = (recordFilter != null) ? recordFilter : (r) -> true;
            final int[] modifiedRecords = importer.importTier(session, editor.getEventManager(), editor.getUndoSupport(), filter);

            // Delete interval tier if requested
            if (shouldDeleteTierSupplier.get()) {
                deleteIntervalTier();
            }

            editor.setCurrentRecordIndex(modifiedRecords.length > 0 ? modifiedRecords[0] : startIndex);

            wasCancelled = false;
            setVisible(false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error importing tier: " + ex.getMessage(),
                "Import Error",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            editor.getUndoSupport().endUpdate();
        }
    }
    
    private void deleteIntervalTier() {
        Session session = editor.getSession();
        IntervalTier tier = session.getTimeline().getTier(intervalTierName);
        if (tier != null) {
            RemoveTimelineTierEdit edit = new RemoveTimelineTierEdit(session, editor.getEventManager(), tier);
            editor.getUndoSupport().postEdit(edit);
        }
    }
    
    private void onCancel() {
        wasCancelled = true;
        setVisible(false);
    }
    
    public boolean wasCancelled() {
        return wasCancelled;
    }
    
    /**
     * Factory method to create a dialog for importing to IPA tier.
     */
    public static IntervalTierImportDialog<IntervalTierToIPATierSettings, IntervalTierToIPATier> createIPATierImportDialog(
            Frame owner,
            SessionEditor editor,
            String intervalTierName) {
        Session session = editor.getSession();
        IntervalTierToIPATierSettings defaultSettings = new IntervalTierToIPATierSettings(
            intervalTierName, "IPA Target", null, null, null);
        IntervalTierToIPATierSettingsPanel panel = new IntervalTierToIPATierSettingsPanel(session, defaultSettings);
        
        return new IntervalTierImportDialog<>(
            owner,
            editor,
            "Import to IPA Tier",
            "Import interval tier data to an IPA tier",
            intervalTierName,
            panel,
            panel::getSettings,
            IntervalTierToIPATier::new,
            panel::validateSettings,
            panel::shouldDeleteIntervalTier,
            true
        );
    }
    
    /**
     * Factory method to create a dialog for importing to orthography tier.
     */
    public static IntervalTierImportDialog<IntervalTierToOrthographySettings, IntervalTierToOrthography> createOrthographyImportDialog(
            Frame owner,
            SessionEditor editor,
            String intervalTierName) {
        Session session = editor.getSession();
        IntervalTierToOrthographySettings defaultSettings = new IntervalTierToOrthographySettings(
            intervalTierName, false, false, intervalTierName);
        IntervalTierToOrthographySettingsPanel panel = new IntervalTierToOrthographySettingsPanel(session, defaultSettings);
        
        return new IntervalTierImportDialog<>(
            owner,
            editor,
            "Import into Orthography tier",
            "Import interval tier data to orthography tier",
            intervalTierName,
            panel,
            panel::getSettings,
            IntervalTierToOrthography::new,
            panel::validateSettings,
            panel::shouldDeleteIntervalTier,
            true
        );
    }
    
    /**
     * Factory method to create a dialog for importing to phone intervals.
     */
    public static IntervalTierImportDialog<IntervalTierToPhoneIntervalsSettings, IntervalTierToPhoneIntervals> createPhoneIntervalsImportDialog(
            Frame owner,
            SessionEditor editor,
            String intervalTierName) {
        Session session = editor.getSession();
        IntervalTierToPhoneIntervalsSettings defaultSettings = new IntervalTierToPhoneIntervalsSettings(
            intervalTierName, null, null);
        IntervalTierToPhoneIntervalsSettingsPanel panel = new IntervalTierToPhoneIntervalsSettingsPanel(session, defaultSettings);
        
        return new IntervalTierImportDialog<>(
            owner,
            editor,
            "Import to Phone intervals",
            "Import interval tier data to phone intervals",
            intervalTierName,
            panel,
            panel::getSettings,
            IntervalTierToPhoneIntervals::new,
            panel::validateSettings,
            panel::shouldDeleteIntervalTier,
            true
        );
    }
    
    /**
     * Factory method to create a dialog for importing to record segments.
     */
    public static IntervalTierImportDialog<IntervalTierToRecordSegmentsSettings, IntervalTierToRecordSegments> createRecordSegmentsImportDialog(
            Frame owner,
            SessionEditor editor,
            String intervalTierName) {
        Session session = editor.getSession();
        // Default to first participant or null
        IntervalTierToRecordSegmentsSettings defaultSettings = new IntervalTierToRecordSegmentsSettings(
            intervalTierName, false, 0.0f, 0.0f, 
            session.getParticipantCount() > 0 ? session.getParticipant(0) : null,
            false);
        IntervalTierToRecordSegmentsSettingsPanel panel = new IntervalTierToRecordSegmentsSettingsPanel(session, defaultSettings);
        
        return new IntervalTierImportDialog<>(
            owner,
            editor,
            "Import as records",
            "Create records from interval tier data or overwrite existing record segments",
            intervalTierName,
            panel,
            panel::getSettings,
            IntervalTierToRecordSegments::new,
            panel::validateSettings,
            panel::shouldDeleteIntervalTier,
            false
        );
    }
    
    /**
     * Factory method to create a dialog for importing to user tier.
     */
    public static IntervalTierImportDialog<IntervalTierToUserTierSettings, IntervalTierToUserTier> createUserTierImportDialog(
            Frame owner,
            SessionEditor editor,
            String intervalTierName) {
        Session session = editor.getSession();
        IntervalTierToUserTierSettings defaultSettings = new IntervalTierToUserTierSettings(
            intervalTierName, intervalTierName, false);
        IntervalTierToUserTierSettingsPanel panel = new IntervalTierToUserTierSettingsPanel(session, defaultSettings);
        
        return new IntervalTierImportDialog<>(
            owner,
            editor,
            "Import into User tier",
            "Import interval tier data to a user-defined tier",
            intervalTierName,
            panel,
            panel::getSettings,
            IntervalTierToUserTier::new,
            panel::validateSettings,
            panel::shouldDeleteIntervalTier,
            true
        );
    }

    /**
     * Factory method to create a dialog for importing to word intervals.
     */
    public static IntervalTierImportDialog<IntervalTierToWordIntervalsSettings, IntervalTierToWordIntervals> createWordIntervalsImportDialog(
            Frame owner,
            SessionEditor editor,
            String intervalTierName) {
        Session session = editor.getSession();
        IntervalTierToWordIntervalsSettings defaultSettings = new IntervalTierToWordIntervalsSettings(
            intervalTierName, false, ca.phon.orthography.TerminatorType.PERIOD);
        IntervalTierToWordIntervalsSettingsPanel panel = new IntervalTierToWordIntervalsSettingsPanel(session, defaultSettings);

        return new IntervalTierImportDialog<>(
            owner,
            editor,
            "Import as Word intervals",
            "Import interval tier data to word intervals",
            intervalTierName,
            panel,
            panel::getSettings,
            IntervalTierToWordIntervals::new,
            panel::validateSettings,
            panel::shouldDeleteIntervalTier,
            true
        );
    }
}
