package ca.phon.app.csv;

import ca.hedlund.desktopicons.MacOSStockIcon;
import ca.hedlund.desktopicons.WindowsStockIcon;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.LogUtil;
import ca.phon.app.session.SessionSelector;
import ca.phon.app.welcome.WelcomeWindow;
import ca.phon.formatter.*;
import ca.phon.project.Project;

import ca.phon.session.*;
import ca.phon.session.format.MediaSegmentFormatter;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.ui.DropDownIcon;
import ca.phon.ui.MultiActionButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.dnd.FileTransferHandler;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.wizard.BreadcrumbWizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.OSInfo;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ItemEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.List;
import java.util.stream.StreamSupport;

public class CSVExportWizard extends BreadcrumbWizardFrame {

    private final static String WINDOW_TITLE = "CSV Export";
    private final WizardStep execStep;
    private final WizardStep settingsStep;
    private final WizardStep tierStep;

    private BufferPanel bufferPanel;
    private JXBusyLabel busyLabel;
    private JLabel currentSessionLabel;
    private SessionSelector sessionSelector;
    private JDialog csvSettingsWindow;
    private CSVSettingsPanel csvSettingsPanel;
    private MultiActionButton exportAsBtn;
    private PhonUIAction<Void> exportAsAct;
    private PhonTask currentTask;
    private String exportFolderPath = null;
    private String exportFilePath = null;
    private final List<CSVColumn> selectedExportColumns = new ArrayList<>();
    private JPanel tierStepRightPanel;
    private JXTable selectedColumnTable;
    private SelectedTierTableModel selectedColumnTableModel;

    public CSVExportWizard(Project project) {
        super(WINDOW_TITLE + " : " + project.getName());

        // some window menu and management functions rely on this extension
        putExtension(Project.class, project);

        this.settingsStep = createStep1();
        this.settingsStep.setNextStep(1);
        this.tierStep = createStep2();
        this.tierStep.setPrevStep(0);
        this.tierStep.setNextStep(2);
        this.execStep = createExecStep();
        this.execStep.setPrevStep(1);
        this.execStep.setNextStep(-1);

        addWizardStep(this.settingsStep);
        addWizardStep(this.tierStep);
        addWizardStep(this.execStep);
    }

    public Project getProject() {
        return getExtension(Project.class);
    }

    private WizardStep createStep1() {
        final WizardStep step = new WizardStep();
        step.setTitle("Settings");
        final DialogHeader header = new DialogHeader(
            WINDOW_TITLE,
            "Choose whether to export the data as either a single file or one file per session"
        );
        step.setLayout(new BorderLayout());
        step.add(header, BorderLayout.NORTH);


        JPanel contentPanel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel(new VerticalLayout());
        contentPanel.add(buttonPanel, BorderLayout.NORTH);

        step.add(contentPanel, BorderLayout.CENTER);

        buttonPanel.add(createExportAsButton());

        sessionSelector = new SessionSelector(getProject());
        sessionSelector.expandAll();
        JScrollPane sessionSelectorScrollPane = new JScrollPane(sessionSelector);
        TitledPanel sessionSelectorWrapper = new TitledPanel("Select Sessions", sessionSelectorScrollPane);

        contentPanel.add(sessionSelectorWrapper, BorderLayout.CENTER);


        return step;
    }

    //region Select folder button

    private MultiActionButton createExportAsButton() {
        exportAsBtn = new MultiActionButton();

        exportAsAct = PhonUIAction.runnable(this::showExportAsMenu);
        exportAsAct.putValue(PhonUIAction.NAME, "Export as...");
        var smallFolderIcn = IconManager.getInstance().getSystemStockIcon(OSInfo.isMacOs() ?
                MacOSStockIcon.GenericFolderIcon : WindowsStockIcon.FOLDER, IconSize.SMALL);
        var medFolderIcn = IconManager.getInstance().getSystemStockIcon(OSInfo.isMacOs() ?
                MacOSStockIcon.GenericFolderIcon : WindowsStockIcon.FOLDER, IconSize.MEDIUM);
        var lrgFolderIcn = IconManager.getInstance().getSystemStockIcon(OSInfo.isMacOs() ?
                MacOSStockIcon.GenericFolderIcon : WindowsStockIcon.FOLDER, IconSize.LARGE);
        exportAsAct.putValue(PhonUIAction.SMALL_ICON, smallFolderIcn);
        exportAsAct.putValue(PhonUIAction.LARGE_ICON_KEY, lrgFolderIcn);
        exportAsAct.putValue(
            PhonUIAction.SHORT_DESCRIPTION,
            "Choose whether to export the data as either a single file or one file per session"
        );
        exportAsBtn.setDefaultAction(exportAsAct);

        createCSVSettingsWindow();

        PhonUIAction<Void> showCSVSettingsAct = PhonUIAction.runnable(this::showCSVSettingsWindow);
        showCSVSettingsAct.putValue(PhonUIAction.NAME, "Show CSV settings");
        showCSVSettingsAct.putValue(
            Action.SMALL_ICON,
            IconManager.getInstance().getIcon("actions/settings-black", IconSize.SMALL)
        );
        showCSVSettingsAct.putValue(
            Action.LARGE_ICON_KEY,
            IconManager.getInstance().getIcon("actions/settings-black", IconSize.SMALL)
        );
        showCSVSettingsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "CSV settings");
        exportAsBtn.addAction(showCSVSettingsAct);

        exportAsBtn.setBackground(Color.white);
        var exportAsTopTitle = exportAsBtn.getTopLabel();
        exportAsTopTitle.setFont(FontPreferences.getTitleFont());
        exportAsTopTitle.setText("Export as...");
        exportAsTopTitle.setIcon(medFolderIcn);
        exportAsTopTitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        exportAsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        var exportAsBottomTitle = exportAsBtn.getBottomLabel();
        exportAsBottomTitle.setForeground(Color.gray);
        exportAsBottomTitle.setText("<html></html>");

        WelcomeWindow.BtnBgPainter btnBgPainter = new WelcomeWindow.BtnBgPainter();
        exportAsBtn.addMouseListener(btnBgPainter);
        exportAsBtn.setBackgroundPainter(btnBgPainter);

        exportAsBtn.setTransferHandler(new FileSelectionTransferHandler());

        return exportAsBtn;
    }

    private void showExportAsMenu() {
        JPopupMenu exportAsMenu = new JPopupMenu();

        JMenuItem singleFile = new JMenuItem("Single file");
        Action singleFileAct = PhonUIAction.runnable(this::saveAsFile);
        singleFileAct.putValue(PhonUIAction.NAME, "Single file");
        singleFile.setAction(singleFileAct);
        exportAsMenu.add(singleFile);

        JMenuItem oneFilePerSession = new JMenuItem("One file per session");
        Action selectFolderAct = PhonUIAction.runnable(this::selectFolder);
        selectFolderAct.putValue(PhonUIAction.NAME, "One file per session");
        oneFilePerSession.setAction(selectFolderAct);
        exportAsMenu.add(oneFilePerSession);

        exportAsMenu.setInvoker(exportAsBtn);
        exportAsMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
        exportAsMenu.setVisible(true);
    }

    private void selectFolder() {
        OpenDialogProperties props = new OpenDialogProperties();
        props.setRunAsync(true);
        props.setCanChooseDirectories(true);
        props.setCanChooseFiles(false);
        props.setAllowMultipleSelection(false);
        props.setCanCreateDirectories(true);
        props.setTitle("Select an export folder");
        props.setFileFilter(FileFilter.csvFilter);
        props.setListener(nativeDialogEvent -> {
            if (nativeDialogEvent.getDialogResult() == NativeDialogEvent.OK_OPTION) {
                final String selectedFolder = (String)nativeDialogEvent.getDialogData();
                SwingUtilities.invokeLater(() -> {
                    try {
                        onSelectFolder(selectedFolder);
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
        NativeDialogs.showOpenDialog(props);
    }

    private void onSelectFolder(String folderPath) {
        exportFolderPath = folderPath;
        exportFilePath = null;
        exportAsBtn.setBottomLabelText(folderPathFormatter(folderPath));
        exportAsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, folderPath);
        exportAsBtn.setTopLabelText("Export as one file per session");
    }

    private String folderPathFormatter(String path) {
        String userPath = System.getProperty("user.home");
        Path userPathObj = Paths.get(userPath).normalize().toAbsolutePath();
        Path pathObj = Paths.get(path).normalize().toAbsolutePath();

        if (pathObj.startsWith(userPathObj)) {
            return "~" + path.substring(userPath.length(), path.length());
        }

        if (path.length() > 50) {
            return "..." + path.substring(path.length() - 50, path.length());
        }

        return path;
    }

    private void saveAsFile() {
        SaveDialogProperties props = new SaveDialogProperties();
        props.setRunAsync(true);
        props.setCanCreateDirectories(true);
        props.setTitle("Save as...");
        props.setFileFilter(FileFilter.csvFilter);
        props.setListener(nativeDialogEvent -> {
            if (nativeDialogEvent.getDialogResult() == NativeDialogEvent.OK_OPTION) {
                final String filePath = (String)nativeDialogEvent.getDialogData();
                SwingUtilities.invokeLater(() -> {
                    try {
                        onSaveAs(filePath);
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
        NativeDialogs.showSaveDialog(props);
    }

    private void onSaveAs(String filePath) {
        exportFilePath = filePath;
        exportFolderPath = null;
        exportAsBtn.setBottomLabelText(folderPathFormatter(filePath));
        exportAsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, filePath);
        exportAsBtn.setTopLabelText("Export as single file");
    }

    //endregion Select folder button

    private void loadColumnSettingsCard(int index) {
        if (index == -1) return;
        CSVColumn column = selectedExportColumns.get(index);
        String id = column.id;

        if (Arrays.stream(tierStepRightPanel.getComponents()).noneMatch(c -> c.getName().equals(id))) {
            var card = new ColumnSettingsCard(column);
            tierStepRightPanel.add(card, id);
            System.out.println("Adding card for: " + column.getOption("name"));
        }

        ((CardLayout)tierStepRightPanel.getLayout()).show(tierStepRightPanel, id);
    }

    private void createCSVSettingsWindow() {
        csvSettingsWindow = new JDialog();
        csvSettingsWindow.setLayout(new BorderLayout());
        csvSettingsPanel = new CSVSettingsPanel(false, true);
        csvSettingsPanel.setBorder(new EmptyBorder(8,8,0,8));
        csvSettingsWindow.add(
            new DialogHeader("CSV Settings", "Settings for the exported CSV files"),
            BorderLayout.NORTH
        );
        csvSettingsWindow.add(csvSettingsPanel, BorderLayout.CENTER);

        JCheckBox firstRowHeaderCheckBox = new JCheckBox("Use First Row as Header");
        firstRowHeaderCheckBox.addActionListener(e -> {
            csvSettingsPanel.setFirstRowHeader(firstRowHeaderCheckBox.isSelected());
        });
        csvSettingsPanel.setFirstRowHeader(true);
        firstRowHeaderCheckBox.setSelected(true);
        var otherOptionsPanel = csvSettingsPanel.getOtherOptionsContentPanel();
        otherOptionsPanel.setLayout(new HorizontalLayout());
        otherOptionsPanel.add(firstRowHeaderCheckBox);

        FlowLayout buttonPanelLayout = new FlowLayout();
        buttonPanelLayout.setAlignment(FlowLayout.RIGHT);
        JPanel buttonPanel = new JPanel(buttonPanelLayout);
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(e -> {
            csvSettingsWindow.setVisible(false);
        });
        buttonPanel.add(okButton);
        csvSettingsWindow.add(buttonPanel, BorderLayout.SOUTH);
        csvSettingsWindow.pack();
        csvSettingsWindow.setLocationByPlatform(true);
    }

    private void showCSVSettingsWindow() {
        csvSettingsWindow.setVisible(true);
    }

    private WizardStep createStep2() {
        final WizardStep step = new WizardStep();
        step.setTitle("Columns");
        final DialogHeader header = new DialogHeader(
            WINDOW_TITLE,
            "Map session data to CSV columns"
        );
        step.setLayout(new BorderLayout());
        step.add(header, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout());
        step.add(contentPanel, BorderLayout.CENTER);

        selectedColumnTableModel = new SelectedTierTableModel();
        selectedColumnTable = new JXTable(selectedColumnTableModel);
        selectedColumnTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        selectedColumnTable.setDragEnabled(true);
        selectedColumnTable.setDropMode(DropMode.INSERT_ROWS);
        selectedColumnTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectedColumnTable.setTransferHandler(new ReorderableTableTransferHandler());
        selectedColumnTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            loadColumnSettingsCard(selectedColumnTable.getSelectedRow());
        });
        JScrollPane selectedColumnScrollPane = new JScrollPane(selectedColumnTable);

        GridBagConstraints c = new GridBagConstraints();

        JPanel selectedColumnPanel = new JPanel(new GridBagLayout());
        c.fill = GridBagConstraints.VERTICAL;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 1;
        selectedColumnPanel.add(selectedColumnScrollPane, c);


        JPanel selectedColumnTopButtons = new JPanel(new BorderLayout());

        var addDropDownIcon = new DropDownIcon(
            IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL)
        );
        JButton addDropDownButton = new JButton("Add column", addDropDownIcon);
        addDropDownButton.setToolTipText("Add column");
        addDropDownButton.setHorizontalTextPosition(SwingConstants.LEFT);
        selectedColumnTopButtons.add(addDropDownButton, BorderLayout.EAST);
        addDropDownButton.addActionListener(e -> showAddTierPopupMenu(addDropDownButton));
        JPanel selectedColumnRightButtons = new JPanel(new VerticalLayout());

        JButton upButton = new JButton(IconManager.getInstance().getIcon("actions/go-up", IconSize.SMALL));
        upButton.addActionListener(e -> {
            int selectedElementIndex = selectedColumnTable.getSelectedRow();
            if (selectedElementIndex != -1 && selectedElementIndex != 0) {
                moveSelectedColumn(selectedElementIndex, selectedElementIndex - 1);
            }
        });
        JButton downButton = new JButton(
            IconManager.getInstance().getIcon("actions/go-down", IconSize.SMALL)
        );
        downButton.addActionListener(e -> {
            int selectedElementIndex = selectedColumnTable.getSelectedRow();
            if (selectedElementIndex != -1 && selectedElementIndex != selectedExportColumns.size() - 1) {
                moveSelectedColumn(selectedElementIndex, selectedElementIndex + 1);
            }
        });
        JButton removeTierButton = new JButton(
            IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL)
        );
        removeTierButton.addActionListener(e -> {
            int selectedElementIndex = selectedColumnTable.getSelectedRow();
            if (selectedElementIndex != -1) {
                var tierColumn = selectedExportColumns.remove(selectedElementIndex);
                selectedColumnTableModel.fireTableRowsDeleted(selectedElementIndex, selectedElementIndex);
                if (selectedExportColumns.size() <= selectedElementIndex) {
                    selectedElementIndex = selectedExportColumns.size() - 1;
                }
                var card = getColumnSettingsCard(tierColumn.id);
                if (card.isPresent()) {
                    tierStepRightPanel.remove(card.get());
                }
                System.out.println(tierStepRightPanel.getComponents().length);
                System.out.println(selectedElementIndex);
                selectedColumnTable.getSelectionModel().setSelectionInterval(selectedElementIndex, selectedElementIndex);
                refreshExportColumnIndices();
            }
        });

        selectedColumnRightButtons.add(upButton);
        selectedColumnRightButtons.add(downButton);
        selectedColumnRightButtons.add(removeTierButton);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        selectedColumnPanel.add(selectedColumnTopButtons, c);
        c.fill = GridBagConstraints.VERTICAL;
        c.weightx = 0;
        c.weighty = 1;
        c.gridx = 1;
        c.gridy = 1;
        selectedColumnPanel.add(selectedColumnRightButtons, c);
        selectedColumnPanel.setBorder(new EmptyBorder(6,6,6,6));
        TitledPanel selectedColumnWrapper = new TitledPanel("Selected Columns", selectedColumnPanel);
        contentPanel.add(selectedColumnWrapper, BorderLayout.WEST);

        tierStepRightPanel = new JPanel(new CardLayout());
        ((CardLayout)tierStepRightPanel.getLayout()).first(tierStepRightPanel);
        TitledPanel optionsWrapperPanel = new TitledPanel("Options", tierStepRightPanel);

        contentPanel.add(optionsWrapperPanel, BorderLayout.CENTER);

        return step;
    }

    private void showAddTierPopupMenu(JButton addDropDownButton) {

        JPopupMenu menu = new JPopupMenu();


        addMenuOption(menu, CSVColumnType.CORPUS_NAME);

        JMenu sessionsSubmenu = new JMenu("Session");
        menu.add(sessionsSubmenu);
        addMenuOption(sessionsSubmenu, CSVColumnType.SESSION_PATH);
        addMenuOption(sessionsSubmenu, CSVColumnType.SESSION_NAME);
        addMenuOption(sessionsSubmenu, CSVColumnType.SESSION_DATE);
        addMenuOption(sessionsSubmenu, CSVColumnType.SESSION_MEDIA);

        JMenu participantSubmenu = new JMenu("Participant");
        menu.add(participantSubmenu);
        addMenuOption(participantSubmenu, CSVColumnType.PARTICIPANT_ID);
        addMenuOption(participantSubmenu, CSVColumnType.PARTICIPANT_NAME);
        addMenuOption(participantSubmenu, CSVColumnType.PARTICIPANT_ROLE);
        addMenuOption(participantSubmenu, CSVColumnType.PARTICIPANT_LANGUAGE);
        addMenuOption(participantSubmenu, CSVColumnType.PARTICIPANT_BIRTHDAY);
        addMenuOption(participantSubmenu, CSVColumnType.PARTICIPANT_AGE);
        addMenuOption(participantSubmenu, CSVColumnType.PARTICIPANT_EDUCATION);
        addMenuOption(participantSubmenu, CSVColumnType.PARTICIPANT_SEX);
        addMenuOption(participantSubmenu, CSVColumnType.PARTICIPANT_SES);

        JMenu recordSubmenu = new JMenu("Record");
        menu.add(recordSubmenu);
        addMenuOption(recordSubmenu, CSVColumnType.RECORD_ID);
        addMenuOption(recordSubmenu, CSVColumnType.RECORD_NUMBER);
        addMenuOption(recordSubmenu, CSVColumnType.RECORD_LANGUAGE);

        JMenu segmentSubmenu = new JMenu("Segment");
        menu.add(segmentSubmenu);
        addMenuOption(segmentSubmenu, CSVColumnType.SEGMENT);
        addMenuOption(segmentSubmenu, CSVColumnType.SEGMENT_START);
        addMenuOption(segmentSubmenu, CSVColumnType.SEGMENT_END);
        addMenuOption(segmentSubmenu, CSVColumnType.SEGMENT_DURATION);

        JMenu tiersSubmenu = new JMenu("Tiers");
        menu.add(tiersSubmenu);
        addMenuOption(tiersSubmenu, CSVColumnType.ORTHOGRAPHY);
        addMenuOption(tiersSubmenu, CSVColumnType.IPA_TARGET);
        addMenuOption(tiersSubmenu, CSVColumnType.IPA_ACTUAL);
        addMenuOption(tiersSubmenu, CSVColumnType.PHONE_ALIGNMENT);
        addMenuOption(tiersSubmenu, CSVColumnType.NOTES);

        JMenu userTiersSubmenu = new JMenu("User tiers");
        menu.add(userTiersSubmenu);
        addUserTierMenuOption(userTiersSubmenu);


        menu.show(addDropDownButton, 0, addDropDownButton.getHeight());
    }

    private void addMenuOption(JPopupMenu parent, CSVColumnType type) {
        JMenuItem menuItem = new JMenuItem(type.getReadableName());
        parent.add(menuItem);
        addTierPopupListener(menuItem, type);
    }

    private void addMenuOption(JMenu parent, CSVColumnType type) {
        JMenuItem menuItem = new JMenuItem(type.getReadableName());
        parent.add(menuItem);
        addTierPopupListener(menuItem, type);
    }

    private void addTierPopupListener(JMenuItem menuItem, CSVColumnType type) {
        menuItem.addActionListener(e -> addExportColumn(type, true));
    }

    private void addUserTierMenuOption(JMenu userTiersSubmenu) {
        Set<String> userTierNames = new HashSet<>();

        for (SessionPath sessionPath : sessionSelector.getSelectedSessions()) {
            try {
                Session session = getProject().openSession(sessionPath.getFolder(), sessionPath.getSessionFile());
                StreamSupport
                    .stream(session.getUserTiers().spliterator(), false)
                    .map(td -> td.getName())
                    .forEach(userTierName -> userTierNames.add(userTierName));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for (String userTierName : userTierNames.stream().sorted().toList()) {
            JMenuItem menuItem = new JMenuItem(userTierName);
            userTiersSubmenu.add(menuItem);
            menuItem.addActionListener(e -> addExportUserTier(userTierName));
        }
    }

    private WizardStep createExecStep() {
        final WizardStep step = new WizardStep();
        step.setTitle("Export files");
        step.setLayout(new BorderLayout());
        final DialogHeader header = new DialogHeader(WINDOW_TITLE, "Exporting files...");
        step.add(header, BorderLayout.NORTH);
        bufferPanel = new BufferPanel("Log");
        TitledPanel bufferPanelWrapper = new TitledPanel("Exporting files", bufferPanel);
        busyLabel = new JXBusyLabel(new Dimension(16, 16));
        currentSessionLabel = new JLabel();
        bufferPanelWrapper.setLeftDecoration(busyLabel);
        bufferPanelWrapper.setRightDecoration(currentSessionLabel);
        step.add(bufferPanelWrapper, BorderLayout.CENTER);
        return step;
    }

    private void startExport(OutputStream out) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            CSVExporter csvExporter = new CSVExporter();
            csvExporter.setCurrentlyExportingLabel(currentSessionLabel);

            WizardExporterListener exporterListener = new WizardExporterListener(writer);
            csvExporter.addListener(exporterListener);

            CSVExportSettings settings = csvSettingsPanel.getExportSettings();
            settings.setExportColumnList(selectedExportColumns);

            if (exportFilePath == null) {
                System.out.println("Export single file");
                for (SessionPath sessionPath : sessionSelector.getSelectedSessions()) {
                    String sessionName = sessionPath.getSessionFile();
                    String corpusName = sessionPath.getFolder();
                    Session session = getProject().openSession(corpusName, sessionName);
                    String filePath = exportFolderPath + "/" + corpusName + "_" + sessionName + ".csv";
                    csvExporter.exportCSV(new Session[]{session}, settings, filePath);
                }
            }
            else {
                System.out.println("Export one file per session");
                List<Session> selectedSessionList = new ArrayList<>();
                for (SessionPath sessionPath : sessionSelector.getSelectedSessions()) {
                    String sessionName = sessionPath.getSessionFile();
                    String corpusName = sessionPath.getFolder();
                    Session session = getProject().openSession(corpusName, sessionName);
                    selectedSessionList.add(session);
                }
                csvExporter.exportCSV(selectedSessionList.toArray(Session[]::new), settings, exportFilePath);
            }

            int selectedSessionCount = sessionSelector.getSelectedSessions().size();
            String sessionCountString = selectedSessionCount + " session";
            if (selectedSessionCount != 1) sessionCountString += "s";

            String fileCountString;
            if (exportFilePath == null) {
                fileCountString = selectedSessionCount + " file";
                if (selectedSessionCount != 1) fileCountString += "s";
            }
            else {
                fileCountString = "1 file";
            }

            String warningCountString = exporterListener.getWarningCount() + " warning";
            if (exporterListener.getWarningCount() != 1) warningCountString += "s";

            int response = showMessage(
                "Complete",
                "Exported " + sessionCountString +
                        " into " + fileCountString +
                        " with " + warningCountString,
                new String[]{"Ok", "Close"}
            );

            if (response == 1) close();
        }
        catch (IOException e) {
            LogUtil.warning(e);
        }
    }

    @Override
    public void gotoStep(int stepIndex) {
        if(getWizardStep(stepIndex) == execStep) {

            // Validate

            if (selectedExportColumns.isEmpty()) {
                showMessage("Error", "You must have at least one column selected to export");
                return;
            }

            Set<String> columnNames = new HashSet<>();
            for (CSVColumn column : selectedExportColumns) {
                String name = column.getOption("name");
                if (columnNames.contains(name)) {
                    showMessage("Error", "All column names must be unique");
                    return;
                }
                else {
                    columnNames.add(name);
                }

                if (name.strip().equals("")) {
                    showMessage("Error", "No column names can be empty or just whitespace");
                    return;
                }
            }

            if (currentTask == null || currentTask.getStatus() != PhonTask.TaskStatus.RUNNING) {
                busyLabel.setBusy(true);
                // start export thread
                currentTask = PhonWorker.invokeOnNewWorker(() -> startExport(bufferPanel.getLogBuffer().getStdOutStream()));
                currentTask.addTaskListener(new PhonTaskListener() {
                    @Override
                    public void statusChanged(PhonTask phonTask, PhonTask.TaskStatus oldStatus, PhonTask.TaskStatus newStatus) {
                        if (newStatus != PhonTask.TaskStatus.RUNNING) {
                            SwingUtilities.invokeLater(bufferPanel::showBuffer);
                            SwingUtilities.invokeLater(() -> busyLabel.setBusy(false));
                        }
                    }

                    @Override
                    public void propertyChanged(PhonTask phonTask, String s, Object o, Object o1) {

                    }
                });
            }
        }
        else if (getWizardStep(stepIndex) == tierStep) {

            // Validate

            if (this.exportFolderPath == null && this.exportFilePath == null) {
                showMessage("Error", "You must choose whether to export the data as either a single file or one file per session");
                return;
            }
            if (this.sessionSelector.getSelectedSessions().isEmpty()) {
                showMessage("Error", "No sessions have been selected for export");
                return;
            }
            if (this.csvSettingsPanel.getSeparators().isEmpty()) {
                showMessage("Error", "No separators have been selected");
                return;
            }


            if (selectedExportColumns.isEmpty()) {
                addExportColumn(CSVColumnType.ORTHOGRAPHY, false);
                addExportColumn(CSVColumnType.IPA_TARGET, false);
                addExportColumn(CSVColumnType.IPA_ACTUAL, false);
                addExportColumn(CSVColumnType.SEGMENT_START, false);
                addExportColumn(CSVColumnType.SEGMENT_END, false);
                addExportColumn(CSVColumnType.NOTES, false);
                selectedColumnTable.getSelectionModel().setSelectionInterval(0, 0);
            }
        }

        super.gotoStep(stepIndex);
    }

    private int showMessage(String header, String message) {
        return showMessage(header, message, null);
    }

    private int showMessage(String header, String message, String[] options) {
        MessageDialogProperties props = new MessageDialogProperties();
        props.setOptions(options != null ? options : MessageDialogProperties.okOptions);
        props.setParentWindow(this);
        props.setHeader(header);
        props.setMessage(message);
        props.setRunAsync(false);
        return NativeDialogs.showMessageDialog(props);
    }

    private void moveSelectedColumn(int start, int end) {
        CSVColumn column = selectedExportColumns.remove(start);
        selectedColumnTableModel.fireTableRowsDeleted(start, start);
        selectedExportColumns.add(end, column);
        selectedColumnTableModel.fireTableRowsInserted(end, end);
        selectedColumnTable.getSelectionModel().setSelectionInterval(end, end);
        refreshExportColumnIndices();
    }

    private Optional<ColumnSettingsCard> getColumnSettingsCard(String key) {
        return Arrays
            .stream(tierStepRightPanel.getComponents())
            .filter(c -> c.getName().equals(key))
            .map(c -> (ColumnSettingsCard)c)
            .findFirst();
    }

    private void addExportColumn(CSVColumnType type, boolean selectNewColumn) {
        CSVColumn tierColumn = new CSVColumn();
        tierColumn.columnType = type;
        tierColumn.csvColumnIndex = selectedExportColumns.size();
        initDefaultOptions(tierColumn);

        String name = tierColumn.getOption("name");
        final String nameFromColumnType = name;
        int duplicateCounter = 0;
        boolean nameIsAlreadyPresent = selectedExportColumns
            .stream()
            .anyMatch(column -> column.getOption("name").equals(nameFromColumnType));
        while (nameIsAlreadyPresent) {
            duplicateCounter++;
            name = nameFromColumnType + " " + duplicateCounter;
            nameIsAlreadyPresent = false;
            for (CSVColumn column : selectedExportColumns) {
                if (column.getOption("name").equals(name)) {
                    nameIsAlreadyPresent = true;
                }
            }
        }
        tierColumn.setOption("name", name);

        selectedExportColumns.add(tierColumn);
        selectedColumnTableModel.fireTableRowsInserted(selectedExportColumns.size() - 1, selectedExportColumns.size() - 1);
        if (selectNewColumn) {
            selectedColumnTable.getSelectionModel().setSelectionInterval(
                selectedExportColumns.size() - 1,
                selectedExportColumns.size() - 1
            );
            tierStepRightPanel.revalidate();
        }
    }

    private void addExportUserTier(String name) {
        CSVColumn userTierColumn = new CSVColumn();
        userTierColumn.columnType = CSVColumnType.USER_TIER;
        userTierColumn.csvColumnIndex = selectedExportColumns.size();
        initDefaultOptions(userTierColumn);
        final String userTierName = name;
        userTierColumn.setOption(CSVExportSettings.USER_TIER_NAME_KEY, userTierName);

        int duplicateCounter = 0;
        boolean nameIsAlreadyPresent = selectedExportColumns
            .stream()
            .anyMatch(column -> column.getOption("name").equals(userTierName));
        while (nameIsAlreadyPresent) {
            duplicateCounter++;
            name = userTierName + " " + duplicateCounter;
            nameIsAlreadyPresent = false;
            for (CSVColumn column : selectedExportColumns) {
                if (column.getOption("name").equals(name)) {
                    nameIsAlreadyPresent = true;
                }
            }
        }
        userTierColumn.setOption("name", name);

        selectedExportColumns.add(userTierColumn);
        selectedColumnTableModel.fireTableRowsInserted(selectedExportColumns.size() - 1, selectedExportColumns.size() - 1);
        selectedColumnTable.getSelectionModel().setSelectionInterval(
                selectedExportColumns.size() - 1,
                selectedExportColumns.size() - 1
        );
        tierStepRightPanel.revalidate();
    }

    private void initDefaultOptions(CSVColumn column) {
        column.setOption("name", column.columnType.getReadableName());
        column.setOption("dateFormat", "ISO");
        column.setOption("locale", Locale.getDefault().getDisplayName());
        column.setOption(
                "syllabifierLanguage",
                SyllabifierLibrary.getInstance().defaultSyllabifierLanguage().toString()
        );
        column.setOption("wordsOnly", "false");
        column.setOption("includeSyllabification", "false");
        column.setOption("stripDiacritics", "false");
        column.setOption("ageFormat", "PHON");
    }

    private void refreshExportColumnIndices() {
        for (int i = 0; i < selectedExportColumns.size(); i++) {
            var column = selectedExportColumns.get(i);
            column.csvColumnIndex = i;
            var settingsCard = Arrays
                .stream(tierStepRightPanel.getComponents())
                .filter(c -> c.getName().equals(column.id))
                .map(c -> (ColumnSettingsCard)c)
                .findFirst();
            if (settingsCard.isPresent()) {
                settingsCard.get().refreshIndex();
            }
        }
    }

    private final class ColumnSettingsCard extends JPanel {
        private final CSVColumn column;
        private String header;
        private JLabel headerLabel;
        private JLabel indexLabel;
        private final List<CSVColumnType> columnTypeList;
        private final String[] formatStyleArray = new String[]{
            "Short",
            "Medium",
            "Long",
            "Full"
        };

        public ColumnSettingsCard(CSVColumn column) {
            this.column = column;
            this.header = column.getOption("name");
            this.columnTypeList = Arrays.stream(CSVColumnType.values()).toList();
            setName(column.id);
            init();
        }

        private void init() {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(4,4,4,4));
            add(createTopTextPanel(), BorderLayout.NORTH);

            JPanel centerPanel = new JPanel(new BorderLayout());

            JPanel settingsPanel = createSettingsPanel();
            settingsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

            String[] readableColumnTypes = columnTypeList
                .stream()
                .map(type -> type.getReadableName())
                .toArray(String[]::new);
            JComboBox<String> columnTypeComboBox = new JComboBox(readableColumnTypes);
            columnTypeComboBox.addActionListener(e -> {
                column.columnType = columnTypeList.get(columnTypeComboBox.getSelectedIndex());
                ((CardLayout)settingsPanel.getLayout()).show(settingsPanel, column.columnType.toString());
            });
            columnTypeComboBox.setSelectedIndex(columnTypeList.indexOf(column.columnType));


            JPanel exportContainerPanel = new JPanel(new VerticalLayout());

            JPanel exportingTypePanel = new JPanel(new HorizontalLayout());
            exportingTypePanel.add(new JLabel("Exporting type:"));
            exportingTypePanel.add(columnTypeComboBox);
            exportContainerPanel.add(exportingTypePanel);


            centerPanel.add(exportContainerPanel, BorderLayout.NORTH);

            centerPanel.add(settingsPanel, BorderLayout.CENTER);
            add(centerPanel, BorderLayout.CENTER);
        }

        private JPanel createTopTextPanel() {

            var topTextPanel = new JPanel(new VerticalLayout());

            var headerPanel = new JPanel(new HorizontalLayout());

            headerLabel = new JLabel(header);
            headerLabel.setBorder(new EmptyBorder(0,0,0,6));
            var font = new Font(FontPreferences.getTitleFont().getFontName(), Font.PLAIN, 16);
            headerLabel.setFont(font);
            headerPanel.add(headerLabel);
            JButton editHeaderButton = new JButton();
            headerPanel.add(editHeaderButton);
            editHeaderButton.setIcon(IconManager.getInstance().getIcon("actions/edit", IconSize.SMALL));
            editHeaderButton.addActionListener(e -> {
                JFrame f = new JFrame();
                String newHeader = JOptionPane.showInputDialog(f, "Rename the column", header);
                if (newHeader == null || newHeader.strip().equals("")) return;
                header = newHeader;
                headerLabel.setText(newHeader);
                column.setOption("name", newHeader);
                selectedColumnTableModel.fireTableRowsUpdated(column.csvColumnIndex, column.csvColumnIndex);
            });

            topTextPanel.add(headerPanel);
            indexLabel = new JLabel("Column: " + (column.csvColumnIndex + 1));
            topTextPanel.add(indexLabel);
            topTextPanel.add(new JLabel("\n"));
            topTextPanel.add(new JLabel("\n"));

            return topTextPanel;
        }

        private JPanel createSettingsPanel() {
            JPanel settingsPanel = new JPanel(new CardLayout());

            for (CSVColumnType columnType : columnTypeList) {
                var settingsCard = new JPanel();

                switch (columnType) {
                    case SESSION_DATE, PARTICIPANT_BIRTHDAY -> settingsCard = dateSettings();
                    case PARTICIPANT_AGE -> settingsCard = ageSettings();
                    case ORTHOGRAPHY -> settingsCard = orthographySettings();
                    case IPA_TARGET, IPA_ACTUAL -> settingsCard = ipaSettings();
                    case SEGMENT -> settingsCard = segmentSettings(true);
                    case SEGMENT_START, SEGMENT_END, SEGMENT_DURATION -> {
                        settingsCard = segmentSettings(false);
                    }
                    case USER_TIER -> settingsCard = userTierSettings();
                    default -> {}
                }

                settingsPanel.add(settingsCard, columnType.toString());
            }

            return settingsPanel;
        }

        private JPanel dateSettings() {
            GridBagConstraints c = new GridBagConstraints();

            JPanel settingsCard = new JPanel(new VerticalLayout());


            JRadioButton defaultFormatButton = new JRadioButton("Default format (ISO)", true);

            JRadioButton localizedFormatButton = new JRadioButton("Localized", false);


            ButtonGroup formatGroup = new ButtonGroup();
            formatGroup.add(defaultFormatButton);
            formatGroup.add(localizedFormatButton);



            settingsCard.add(defaultFormatButton);

            JPanel localizedPanel = new JPanel(new HorizontalLayout());

            final Locale[] selectedLocale = new Locale[]{Locale.getDefault()};
            final String[] selectedFormatStyle = new String[]{"SHORT"};

            LocalDate today = LocalDate.now();
            JLabel exampleLabel = new JLabel("Example: " + DateTimeFormatter.ISO_LOCAL_DATE.format(today));
            exampleLabel.setBorder(new EmptyBorder(0,8,0,0));

            localizedPanel.add(localizedFormatButton);
            List<Locale> locales = Arrays
                .stream(Locale.getAvailableLocales())
                .sorted(Comparator.comparing(Locale::getDisplayName))
                .toList();
            JComboBox<String> localeComboBox = new JComboBox<>(
                locales.stream().map(locale -> locale.getDisplayName()).sorted().toArray(String[]::new)
            );
            localeComboBox.addActionListener(e -> {
                if (localeComboBox.getSelectedIndex() == -1) return;
                column.setOption("locale", locales.get(localeComboBox.getSelectedIndex()).getDisplayName());
                selectedLocale[0] = locales.get(localeComboBox.getSelectedIndex());
                var formatStyle = Arrays
                    .stream(FormatStyle.values())
                    .filter(format -> format.name().toUpperCase().equals(selectedFormatStyle[0]))
                    .findFirst();
                var formatter = DateTimeFormatter.ofLocalizedDate(formatStyle.get()).withLocale(selectedLocale[0]);
                exampleLabel.setText("Example: " + formatter.format(today));
            });
            var localeFromOptions = Arrays
                .stream(Locale.getAvailableLocales())
                .filter(locale -> locale.getDisplayName().equals(column.getOption("locale")))
                .findFirst();
            localeComboBox.setSelectedIndex(locales.indexOf(localeFromOptions.get()));
            localeComboBox.setEnabled(false);
            localizedPanel.add(localeComboBox);
            settingsCard.add(localizedPanel);

            JPanel formatStylePanel = new JPanel(new HorizontalLayout());
            ButtonGroup formatStyleGroup = new ButtonGroup();

            for (String formatStyleString : formatStyleArray) {
                boolean selected = formatStyleString.toUpperCase().equals(selectedFormatStyle[0]);
                JRadioButton radioButton = new JRadioButton(formatStyleString, selected);
                formatStyleGroup.add(radioButton);
                formatStylePanel.add(radioButton);
                radioButton.addItemListener(e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        selectedFormatStyle[0] = radioButton.getText().toUpperCase();
                        column.setOption("dateFormat", selectedFormatStyle[0]);
                        var formatStyle = Arrays
                            .stream(FormatStyle.values())
                            .filter(format -> format.name().toUpperCase().equals(selectedFormatStyle[0]))
                            .findFirst();
                        var formatter = DateTimeFormatter.ofLocalizedDate(formatStyle.get()).withLocale(selectedLocale[0]);
                        exampleLabel.setText("Example: " + formatter.format(today));
                    }
                });
                radioButton.setEnabled(false);
            }

            defaultFormatButton.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    System.out.println("ISO");
                    column.setOption("dateFormat", "ISO");

                    localeComboBox.setEnabled(false);
                    for (Enumeration<AbstractButton> buttons = formatStyleGroup.getElements(); buttons.hasMoreElements();) {
                        JRadioButton radioButton = (JRadioButton) buttons.nextElement();
                        radioButton.setEnabled(false);
                    }

                    exampleLabel.setText("Example: " + DateTimeFormatter.ISO_LOCAL_DATE.format(today));
                }
            });
            localizedFormatButton.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    System.out.println(selectedFormatStyle[0]);
                    column.setOption("dateFormat", selectedFormatStyle[0]);

                    localeComboBox.setEnabled(true);
                    for (Enumeration<AbstractButton> buttons = formatStyleGroup.getElements(); buttons.hasMoreElements();) {
                        JRadioButton radioButton = (JRadioButton) buttons.nextElement();
                        radioButton.setEnabled(true);
                    }

                    var formatStyle = Arrays
                        .stream(FormatStyle.values())
                        .filter(format -> format.toString().toUpperCase().equals(selectedFormatStyle[0]))
                        .findFirst();
                    var formatter = DateTimeFormatter.ofLocalizedDate(formatStyle.get()).withLocale(selectedLocale[0]);
                    exampleLabel.setText("Example: " + formatter.format(today));
                }
            });

            formatStylePanel.setBorder(new EmptyBorder(0,16,0,0));
            settingsCard.add(formatStylePanel);

            settingsCard.add(exampleLabel);

            return settingsCard;
        }

        private JPanel ageSettings() {
            JPanel settingsCard = new JPanel(new VerticalLayout());

            Period period = Period.between(
                LocalDate.of(2000, 1, 1),
                LocalDate.of(2001, 3, 4)
            );

            JLabel exampleLabel = new JLabel("Example: ");
            exampleLabel.setBorder(new EmptyBorder(0,8,0,0));


            ButtonGroup buttonGroup = new ButtonGroup();

            JRadioButton phonFormatButton = new JRadioButton("Y;M.D");
            phonFormatButton.addItemListener(e -> {
                column.setOption("ageFormat", "PHON");
                exampleLabel.setText("Example: " + PeriodFormatter.periodToString(period, PeriodFormatStyle.PHON));
            });
            buttonGroup.add(phonFormatButton);
            settingsCard.add(phonFormatButton);

            JRadioButton isoFormatButton = new JRadioButton("ISO");
            isoFormatButton.addItemListener(e -> {
                column.setOption("ageFormat", "ISO");
                exampleLabel.setText("Example: " + PeriodFormatter.periodToString(period, PeriodFormatStyle.ISO));
            });
            buttonGroup.add(isoFormatButton);
            settingsCard.add(isoFormatButton);

            buttonGroup.setSelected(phonFormatButton.getModel(), true);


            settingsCard.add(exampleLabel);

            return settingsCard;
        }

        private JPanel orthographySettings() {
            JPanel settingsCard = new JPanel(new VerticalLayout());

            JCheckBox wordsOnlyCheckBox = new JCheckBox("Words only (strip coding)");
            settingsCard.add(wordsOnlyCheckBox);
            wordsOnlyCheckBox.addActionListener(e -> {
                column.setOption("wordsOnly", wordsOnlyCheckBox.isSelected() ? "true" : "false");
            });

            return settingsCard;
        }

        private JPanel ipaSettings() {

            JPanel settingsCard = new JPanel(new VerticalLayout());

            JCheckBox includeSyllabificationCheckBox = new JCheckBox("Include syllabification");
            settingsCard.add(includeSyllabificationCheckBox);
            includeSyllabificationCheckBox.addActionListener(e -> {
                column.setOption(
                    "includeSyllabification",
                    includeSyllabificationCheckBox.isSelected() ? "true" : "false"
                );
            });

            JCheckBox wordsOnlyCheckBox = new JCheckBox("Strip diacritics");
            settingsCard.add(wordsOnlyCheckBox);
            wordsOnlyCheckBox.addActionListener(e -> {
                column.setOption("stripDiacritics", wordsOnlyCheckBox.isSelected() ? "true" : "false");
            });

            return settingsCard;
        }

        private JPanel segmentSettings(boolean fullSegment) {
            JPanel settingsCard = new JPanel(new VerticalLayout());


            SessionFactory sessionFactory = SessionFactory.newFactory();
            var segment = sessionFactory.createMediaSegment();
            segment.setStartValue(0);
            segment.setEndValue(83000);
            segment.setUnitType(MediaUnit.Millisecond);


            JLabel exampleLabel = new JLabel("Example: ");
            exampleLabel.setBorder(new EmptyBorder(0,8,0,0));

            ButtonGroup buttonGroup = new ButtonGroup();
            for (MediaTimeFormatStyle format : MediaTimeFormatStyle.values()) {
                JRadioButton radioButton = new JRadioButton(format.name());
                radioButton.addItemListener(e -> {
                    column.setOption("segmentFormat", format.toString());

                    if (fullSegment) {
                        MediaSegmentFormatter formatter = new MediaSegmentFormatter(format);
                        exampleLabel.setText("Example: " + formatter.format(segment));
                    }
                    else {
                        exampleLabel.setText(
                            "Example: " + MediaTimeFormatter.timeToString((long)segment.getEndValue(), format)
                        );
                    }
                });
                buttonGroup.add(radioButton);
                if (format.equals(MediaTimeFormatStyle.MINUTES_AND_SECONDS)) {
                    buttonGroup.setSelected(radioButton.getModel(), true);
                }
                settingsCard.add(radioButton);
            }

            settingsCard.add(exampleLabel);

            return settingsCard;
        }

        private JPanel userTierSettings() {
            GridBagConstraints c = new GridBagConstraints();

            JPanel settingsCard = new JPanel(new VerticalLayout());
            JLabel tierNameLabel = new JLabel();
            tierNameLabel.setText("Exporting user tier: " + column.getOption(CSVImportSettings.USER_TIER_NAME_KEY));
            JPanel tierNamePanel = new JPanel(new HorizontalLayout());
            tierNamePanel.setBorder(new EmptyBorder(0,8,0,8));
            tierNamePanel.add(tierNameLabel, c);

            settingsCard.add(tierNamePanel);

            return settingsCard;
        }

        public void setHeader(String header) {
            this.header = header;
            headerLabel.setText(header);
        }

        public void refreshIndex() {
            indexLabel.setText("Column: " + column.csvColumnIndex);
        }
    }

    private final class SelectedTierTableModel extends AbstractTableModel {

        public SelectedTierTableModel() {
            super();
        }

        @Override
        public int getRowCount() {
            return selectedExportColumns.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int column) {
            return null;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return selectedExportColumns.get(rowIndex).getOption("name");
        }

        @Override
        public void fireTableCellUpdated(int row, int column) {
            super.fireTableCellUpdated(row, column);
            String newVal = getValueAt(row, column).toString();
            var tierColumn = selectedExportColumns.get(row);
            tierColumn.setOption("name", newVal);
            var settingsCard = getColumnSettingsCard(tierColumn.id);
            if (settingsCard.isPresent()) {
                settingsCard.get().setHeader(newVal);
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            selectedExportColumns.get(rowIndex).setOption("name", aValue.toString());
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    private class FileSelectionTransferHandler extends FileTransferHandler {

        private static final long serialVersionUID = 6799990443658389742L;

        @Override
        public boolean importData(JComponent comp, Transferable transferable) {
            File file = null;
            try {
                file = getFile(transferable);
            } catch (IOException e) {
                return false;
            }

            if(file != null) {
                if(file.isFile()) return false;
                onSelectFolder(file.getAbsolutePath());
                return true;
            } else {
                return false;
            }
        }

        @Override
        public File getFile(Transferable transferable) throws IOException {
            File retVal = super.getFile(transferable);
            if(retVal.isFile()) {
                retVal = null;
            }
            return retVal;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return super.createTransferable(c);
        }

    }

    public class ReorderableTableTransferHandler extends TransferHandler {
        private int startRowIndex = -1;

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            JXTable source = (JXTable) c;
            startRowIndex = source.getSelectedRow();
            return new StringSelection("");
        }

        @Override
        protected void exportDone(JComponent c, Transferable data, int action) {
            startRowIndex = -1;
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport info) {
            return info.getComponent() instanceof JXTable;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport info) {
            if (!info.isDrop()) {
                return false;
            }

            JXTable target = (JXTable) info.getComponent();
            SelectedTierTableModel tableModel = (SelectedTierTableModel) target.getModel();
            JXTable.DropLocation dl = (JXTable.DropLocation) info.getDropLocation();
            int endRowIndex = dl.getRow();

            if (endRowIndex < 0 || endRowIndex > tableModel.getRowCount()) {
                return false;
            }

            try {
                if (endRowIndex != startRowIndex && endRowIndex != startRowIndex + 1) {
                    if (endRowIndex >= startRowIndex) endRowIndex--;
                    moveSelectedColumn(startRowIndex, endRowIndex);
                }

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private final class WizardExporterListener implements CSVExporterListener {

        private BufferedWriter bufferedWriter;
        private int warningCount = 0;

        public WizardExporterListener(BufferedWriter bufferedWriter) {
            this.bufferedWriter = bufferedWriter;
        }

        @Override
        public void writingError(IOException e) {
            try {
                bufferedWriter.write(e.toString());
                bufferedWriter.newLine();
            }
            catch (Exception exception) {
                throw new RuntimeException(exception);
            }
            warningCount++;
        }

        public int getWarningCount() {
            return warningCount;
        }
    }
}
