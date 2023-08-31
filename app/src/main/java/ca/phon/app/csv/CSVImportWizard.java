package ca.phon.app.csv;

import ca.hedlund.desktopicons.MacOSStockIcon;
import ca.hedlund.desktopicons.WindowsStockIcon;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.LogUtil;
import ca.phon.app.welcome.WelcomeWindow;
import ca.phon.csv.CSVReader;
import ca.phon.csv.CSVWriter;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.ui.MultiActionButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.decorations.TitledPanel;
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
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.List;

public class CSVImportWizard extends BreadcrumbWizardFrame {

    private final static String WINDOW_TITLE = "CSV Import";

    private final WizardStep execStep;
    private final WizardStep settingsStep;
    private final WizardStep tiersStep;
    private PhonTask currentTask;
    /**
     * Buffer panel used for displaying console messages to the user.  Data may be formatted in csv
     * and then displayed as a table.
     */
    private BufferPanel bufferPanel;

    private String selectedFile;
    private List<String> selectedFiles = new ArrayList<>();
    private MultiActionButton selectFileBtn;
    private PhonUIAction<Void> selectFileAct;
    private JList tierStepHeaderList;
    private int selectedHeaderColumn = -1;
    private JPanel tierStepRightPanel;
    private JCheckBox firstRowHeaderCheckBox;
    private CSVImportSettings settings = null;
    private JXBusyLabel busyLabel;
    private JLabel currentFileLabel;
    private boolean filesChanged = false;
    private String selectedCorpus;
    private List<String> csvHeaders;

    private CSVSettingsPanel csvSettingsPanel;

    public CSVImportWizard(Project project, String selectedCorpus) {
        super(WINDOW_TITLE + " : " + project.getName());

        this.settingsStep = createStep1();
        this.settingsStep.setNextStep(1);
        this.tiersStep = createStep2();
        this.tiersStep.setNextStep(2);
        this.tiersStep.setPrevStep(0);
        this.execStep = createExecStep();
        this.execStep.setNextStep(-1);
        this.execStep.setPrevStep(1);

        addWizardStep(this.settingsStep);
        addWizardStep(this.tiersStep);
        addWizardStep(this.execStep);

        this.selectedCorpus = selectedCorpus;
        // some window menu and management functions rely on this extension
        putExtension(Project.class, project);
    }

    public Project getProject() {
        return getExtension(Project.class);
    }

    private WizardStep createStep1() {
        final WizardStep step = new WizardStep();
        step.setTitle("Settings");
        final DialogHeader header = new DialogHeader(WINDOW_TITLE, "Select files for import");
        step.setLayout(new BorderLayout());
        step.add(header, BorderLayout.NORTH);


        JPanel contentPanel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel(new VerticalLayout());
        contentPanel.add(buttonPanel, BorderLayout.NORTH);

        JPanel settingsPanel = new JPanel(new BorderLayout());
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        csvSettingsPanel = new CSVSettingsPanel(true);
        settingsPanel.add(csvSettingsPanel);
        TitledPanel settingsPanelWrapper = new TitledPanel("CSV Settings", settingsPanel);
        contentPanel.add(settingsPanelWrapper);

        step.add(contentPanel, BorderLayout.CENTER);

        buttonPanel.add(createSelectFilesButton());



        setupOtherOptionsPanel();

        return step;
    }

    private MultiActionButton createSelectFilesButton() {
        selectFileAct = PhonUIAction.runnable(this::selectFiles);
        selectFileAct.putValue(PhonUIAction.NAME, "Select files and directories for import");

        var smallFolderIcn = IconManager.getInstance().getSystemStockIcon(OSInfo.isMacOs() ?
                MacOSStockIcon.GenericFolderIcon : WindowsStockIcon.FOLDER, IconSize.SMALL);
        var medFolderIcn = IconManager.getInstance().getSystemStockIcon(OSInfo.isMacOs() ?
                MacOSStockIcon.GenericFolderIcon : WindowsStockIcon.FOLDER, IconSize.MEDIUM);
        var lrgFolderIcn = IconManager.getInstance().getSystemStockIcon(OSInfo.isMacOs() ?
                MacOSStockIcon.GenericFolderIcon : WindowsStockIcon.FOLDER, IconSize.LARGE);
        selectFileAct.putValue(PhonUIAction.SMALL_ICON, smallFolderIcn);
        selectFileAct.putValue(PhonUIAction.LARGE_ICON_KEY, lrgFolderIcn);
        selectFileAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select files and directories for import");

        selectFileBtn = new MultiActionButton();
        selectFileBtn.setDefaultAction(selectFileAct);
        selectFileBtn.setBackground(Color.white);
        var selectFileTopTitle = selectFileBtn.getTopLabel();
        selectFileTopTitle.setFont(FontPreferences.getTitleFont());
        selectFileTopTitle.setText("Select files and directories for import");
        selectFileTopTitle.setIcon(medFolderIcn);
        selectFileTopTitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        selectFileBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        var selectFileBottomTitle = selectFileBtn.getBottomLabel();
        selectFileBottomTitle.setForeground(Color.gray);
        selectFileBottomTitle.setText("<html></html>");

        WelcomeWindow.BtnBgPainter btnBgPainter = new WelcomeWindow.BtnBgPainter();
        selectFileBtn.addMouseListener(btnBgPainter);
        selectFileBtn.setBackgroundPainter(btnBgPainter);

        return selectFileBtn;
    }

    private WizardStep createStep2() {
        final WizardStep step = new WizardStep();
        step.setTitle("Tiers");
        final DialogHeader header = new DialogHeader(
            WINDOW_TITLE,
            "Map the CSV columns to the appropriate Tiers"
        );
        step.setLayout(new BorderLayout());
        step.add(header, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel(new VerticalLayout());
        contentPanel.add(buttonPanel, BorderLayout.NORTH);

        step.add(contentPanel, BorderLayout.CENTER);


        tierStepHeaderList = new JList<String>();
        tierStepHeaderList.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            loadCSVColumnMapper(tierStepHeaderList.getSelectedIndex());
        });


        tierStepHeaderList.setCellRenderer(new CSVColumnListRenderer());
        JScrollPane headerListScrollPane = new JScrollPane(tierStepHeaderList);
        TitledPanel leftWrapperPanel = new TitledPanel("CSV Columns", headerListScrollPane);
        leftWrapperPanel.setPreferredSize(new Dimension(210, leftWrapperPanel.getPreferredSize().height));
        contentPanel.add(leftWrapperPanel, BorderLayout.WEST);


        tierStepRightPanel = new JPanel(new CardLayout());
        TitledPanel rightWrapperPanel = new TitledPanel("Import As", tierStepRightPanel);
        contentPanel.add(rightWrapperPanel, BorderLayout.CENTER);

        return step;
    }

    private void selectFiles() {
        OpenDialogProperties props = new OpenDialogProperties();
        props.setRunAsync(true);
        props.setCanChooseDirectories(true);
        props.setCanChooseFiles(true);
        props.setAllowMultipleSelection(true);
        props.setCanCreateDirectories(false);
        props.setTitle("Select Files and Directories");
        props.setFileFilter(FileFilter.csvFilter);
        props.setListener(nativeDialogEvent -> {
            if (nativeDialogEvent.getDialogResult() == NativeDialogEvent.OK_OPTION) {
                final String[] selectedFiles = (String[])nativeDialogEvent.getDialogData();
                SwingUtilities.invokeLater(() -> {
                    try {
                        onSelectFiles(selectedFiles);
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
        NativeDialogs.showOpenDialog(props);
    }

    private String[] getCSVFilesRecursively(File rootDir) {
        List<File> files = List.of(rootDir.listFiles());
        List<String> output = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    var filesFromSubdir = getCSVFilesRecursively(file);
                    Arrays.stream(filesFromSubdir).forEach(subdirFile -> output.add(subdirFile));
                } else {
                    var filePath = file.getAbsolutePath();
                    if (filePath.substring(filePath.length()-3, filePath.length()).toUpperCase().equals("CSV")) {
                        output.add(file.getAbsolutePath());
                    }
                }
            }
        }

        return output.toArray(String[]::new);
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
    private boolean validateCSVFiles(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) return false;

        int columnCount = -1;

        for (String filePath : filePaths) {
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(
                    new FileInputStream(filePath),
                    csvSettingsPanel.getEncoding()
                );
                CSVReader csvReader = csvSettingsPanel.createCSVReaderWithSettings(inputStreamReader);
                if (columnCount == -1) {
                    columnCount = csvReader.readNext().length;
                }
                else if (csvReader.readNext().length != columnCount) {
                    return false;
                }
            }
            catch (Exception e) {
                return false;
            }
        }

        return true;
    }
    private boolean validateCSVHeaders(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) return false;

        String[] headers = null;

        for (String filePath : filePaths) {
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(
                    new FileInputStream(filePath),
                    csvSettingsPanel.getEncoding()
                );
                CSVReader csvReader = csvSettingsPanel.createCSVReaderWithSettings(inputStreamReader);
                if (headers == null) {
                    headers = csvReader.readNext();
                }
                else {
                    var currentFileHeaders = csvReader.readNext();
                    if (currentFileHeaders.length != headers.length) return false;
                    for (int i = 0; i < headers.length; i++) {
                        if (!headers[i].equals(currentFileHeaders[i])) return false;
                    }
                }
            }
            catch (Exception e) {
                return false;
            }
        }

        return true;
    }
    private void onSelectFiles(String[] paths) throws IOException {
        // Clear out any existing data
        selectedFile = null;
        selectedFiles = new ArrayList<>();

        // Get new data
        for (String path : paths) {
            File file = new File(path);
            // For any directories selected
            if (file.isDirectory()) {
                // Get all CSV files in them recursively
                var filesInDir = getCSVFilesRecursively(file);
                // Add them all to the list
                for (String fileInDir: filesInDir) {
                    selectedFiles.add(fileInDir);
                }
            }
            else {
                // Add any selected CSV files to the list
                selectedFiles.add(path);
            }
        }

        if (!validateCSVFiles(selectedFiles)) {
            showMessage("Error", "Selected files did not all have the same number of columns.");
            return;
        }

        if (!validateCSVHeaders(selectedFiles)) {
            showMessage("Warning", "Selected files did not all have the same column headers.");
            firstRowHeaderCheckBox.setSelected(false);
            csvSettingsPanel.setFirstRowHeader(false);
        }

        filesChanged = true;

        selectedFile = selectedFiles.get(0);

        boolean hitCharLimit = false;

        StringBuilder bottomLabelStringBuilder = new StringBuilder();
        for (int i = 0; i < selectedFiles.size(); i++) {
            var fileName = selectedFiles.get(i);
            var splitFileName = fileName.split("/");
            bottomLabelStringBuilder.append(splitFileName[splitFileName.length-1]);
            var bottomLabel = StringUtils.abbreviate(bottomLabelStringBuilder.toString(), 50);
            if (bottomLabel.substring(bottomLabel.length()-3, bottomLabel.length()).equals("...")) {
                int remainingFileNameCount = selectedFiles.size() - i - 1;
                if (remainingFileNameCount > 0) {
                    bottomLabel += " + " + remainingFileNameCount + " more";
                }
                selectFileBtn.setBottomLabelText(bottomLabel);
                hitCharLimit = true;
                break;
            }
            bottomLabelStringBuilder.append(", ");
        }

        if (!hitCharLimit) {
            var bottomLabel = bottomLabelStringBuilder.toString();
            selectFileBtn.setBottomLabelText(bottomLabel.substring(0, bottomLabel.length()-2));
        }

        try {
            csvSettingsPanel.loadPreviewTableData(selectedFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadCSVColumnMapper(int index) {
        if (index == -1) return;

        var column = settings.getColumnByIndex(index);

        if (Arrays.stream(tierStepRightPanel.getComponents()).noneMatch(c -> c.getName().equals(column.id))) {
            System.out.println("Adding card for: " + csvHeaders.get(index));
            tierStepRightPanel.add(new ColumnSettingsCard(column, csvHeaders.get(index)), Integer.toString(index));
        }

        ((CardLayout)tierStepRightPanel.getLayout()).show(tierStepRightPanel, Integer.toString(index));

    }

    private void setupOtherOptionsPanel() {
        GridBagConstraints c = new GridBagConstraints();
        var otherOptions = csvSettingsPanel.getOtherOptionsContentPanel();
        otherOptions.setLayout(new HorizontalLayout());

        firstRowHeaderCheckBox = new JCheckBox("Use First Row as Header");
        firstRowHeaderCheckBox.addActionListener((e) -> {
            csvSettingsPanel.setFirstRowHeader(((AbstractButton) e.getSource()).isSelected());
        });
        firstRowHeaderCheckBox.setSelected(true);
        csvSettingsPanel.setFirstRowHeader(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        otherOptions.add(firstRowHeaderCheckBox, c);

        JCheckBox trimSpaces = new JCheckBox("Trim Spaces");
        trimSpaces.addActionListener((e) -> {
            try {
                csvSettingsPanel.setTrimSpaces(((AbstractButton) e.getSource()).isSelected());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 1;
        c.gridy = 0;
        otherOptions.add(trimSpaces, c);
    }

    private boolean isImported(Optional<CSVColumn> column) {
        return column.map(csvColumn -> csvColumn.importThisColumn).orElse(false);
    }
    private List<String> getImportConflicts() {
        List<String> retVal = new ArrayList<>();

        boolean noColumnsImported =  settings
            .getImportColumnList()
            .stream()
            .filter(column -> column.importThisColumn)
            .findFirst()
            .isEmpty();

        if (noColumnsImported) {
            retVal.add("No columns selected for import");
            return retVal;
        }

        var sessionPath = settings.getColumnByType(CSVColumnType.SESSION_PATH);
        var corpusName = settings.getColumnByType(CSVColumnType.CORPUS_NAME);
        var sessionName = settings.getColumnByType(CSVColumnType.SESSION_NAME);
        if (isImported(sessionPath)) {
            if (isImported(corpusName) || isImported(sessionName)) {
                retVal.add("Cannot import corpus name or session name if session path is being imported");
            }
        }
        else {
            if (!isImported(corpusName) || !isImported(sessionName)) {
                retVal.add("If session path is not being imported, corpus name and session name must be imported");
            }
        }

        var segment = settings.getColumnByType(CSVColumnType.SEGMENT);
        var segmentStart = settings.getColumnByType(CSVColumnType.SEGMENT_START);
        var segmentEnd = settings.getColumnByType(CSVColumnType.SEGMENT_END);
        var segmentDuration = settings.getColumnByType(CSVColumnType.SEGMENT_DURATION);

        if (isImported(segment)) {
            if (isImported(segmentStart) || isImported(segmentEnd) || isImported(segmentDuration)) {
                retVal.add("Cannot import segment start, segment end or segment duration if segment is being imported");
            }
        }
        else {
            if (!isImported(segmentStart) || (isImported(segmentEnd) == isImported(segmentDuration))) {
                retVal.add("If segment is not being imported, segment start must be imported as well as either segment duration or segment end");
            }
        }

        List<String> userTierNameList = new ArrayList<>();
        Set<String> duplicateNames = new HashSet<>();
        for (CSVColumn column : settings.getImportColumnList()) {
            if (column.columnType != CSVColumnType.USER_TIER || !column.importThisColumn) continue;

            var userTierName = column.getOption(CSVImportSettings.USER_TIER_NAME_KEY);
            if (userTierNameList.indexOf(userTierName) != -1) {
                duplicateNames.add(userTierName);
            }
            else {
                userTierNameList.add(userTierName);
            }
        }
        for (String userTierName : duplicateNames) {
            retVal.add("Cannot import multiple user tiers with the name \"" + userTierName + "\"");
        }

        return retVal;
    }

    private WizardStep createExecStep() {
        final WizardStep step = new WizardStep();
        step.setTitle("Import files");
        step.setLayout(new BorderLayout());
        final DialogHeader header = new DialogHeader(WINDOW_TITLE, "Importing files...");
        step.add(header, BorderLayout.NORTH);
        bufferPanel = new BufferPanel("Log");
        TitledPanel bufferPanelWrapper = new TitledPanel("Importing files", bufferPanel);
        busyLabel = new JXBusyLabel(new Dimension(16, 16));
        currentFileLabel = new JLabel();
        bufferPanelWrapper.setLeftDecoration(busyLabel);
        bufferPanelWrapper.setRightDecoration(currentFileLabel);
        step.add(bufferPanelWrapper, BorderLayout.CENTER);
        return step;
    }

    private void startImport(OutputStream out) {
        try (CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)))) {
            String[] headerTitles = new String[]{
                "File name",
                "CSV record index",
                "Field index",
                "Character position in field",
                "Column type",
                "Session name",
                "Record",
                "Error"
            };

            writer.writeNext(headerTitles);

            int beforeSessionCount = 0;

            var project = getProject();

            for (String corpus : project.getCorpora()) {
                beforeSessionCount += project.getCorpusSessions(corpus).size();
            }

            CSVImporter csvImporter = new CSVImporter(project, selectedCorpus);

            WizardImporterListener importerListener = new WizardImporterListener(writer);
            csvImporter.addListener(importerListener);

            for (String filePath : selectedFiles) {
                var splitFilePath = filePath.split("/");
                var fileName = splitFilePath[splitFilePath.length - 1];
                SwingUtilities.invokeLater(() -> currentFileLabel.setText(fileName));
                csvImporter.importCSV(filePath, settings);
            }

            int afterSessionCount = 0;

            for (String corpus : project.getCorpora()) {
                afterSessionCount += project.getCorpusSessions(corpus).size();
            }

            int newSessionsCount = afterSessionCount - beforeSessionCount;
            String sessionCountString = newSessionsCount + " session";
            if (newSessionsCount != 1) sessionCountString += "s";

            String fileCountString = selectedFiles.size() + " file";
            if (selectedFiles.size() != 1) fileCountString += "s";

            String warningCountString = importerListener.getWarningCount() + " warning";
            if (importerListener.getWarningCount() != 1) warningCountString += "s";

            int response = showMessage(
                "Complete",
                "Imported " + sessionCountString +
                        " from " + fileCountString +
                        " with " + warningCountString,
                new String[]{"Ok", "Close"}
            );

            if (response == 1) close();
        } catch (IOException e) {
            LogUtil.warning(e);
        }
    }



    @Override
    public void gotoStep(int stepIndex) {
        if(getWizardStep(stepIndex) == execStep) {

            var importConflictList = getImportConflicts();
            if (!importConflictList.isEmpty()) {
                StringJoiner stringJoiner = new StringJoiner("\n\n");
                for (String conflictMessage : importConflictList) {
                    stringJoiner.add(conflictMessage);
                }
                showMessage("Error", stringJoiner.toString());
                return;
            }

            if (currentTask == null || currentTask.getStatus() != PhonTask.TaskStatus.RUNNING) {
                busyLabel.setBusy(true);
                // start export thread
                currentTask = PhonWorker.invokeOnNewWorker(() -> startImport(bufferPanel.getLogBuffer().getStdOutStream()));
                currentTask.addTaskListener(new PhonTaskListener() {
                    @Override
                    public void statusChanged(PhonTask phonTask, PhonTask.TaskStatus oldStatus, PhonTask.TaskStatus newStatus) {
                        if (newStatus != PhonTask.TaskStatus.RUNNING) {
                            SwingUtilities.invokeLater(bufferPanel::showTable);
                            SwingUtilities.invokeLater(() -> busyLabel.setBusy(false));
                        }
                    }

                    @Override
                    public void propertyChanged(PhonTask phonTask, String s, Object o, Object o1) {

                    }
                });
            }
        }
        else if (getWizardStep(stepIndex) == tiersStep) {
            if (this.selectedFiles.isEmpty()) {
                showMessage("Error", "No files have been selected for import");
                return;
            }
            if (this.csvSettingsPanel.getSeparators().isEmpty()) {
                showMessage("Error", "No separators have been selected");
            }

            if (filesChanged) {
                settings = csvSettingsPanel.getImportSettings();
                csvHeaders = List.of(csvSettingsPanel.getTableHeaders());
                tierStepHeaderList.setModel(new HeaderListModel(csvHeaders));

                for (int i = 0; i < csvHeaders.size(); i++) {

                    String headerName = csvHeaders.get(i);
                    var matchedType = Arrays
                        .stream(CSVColumnType.values())
                        .filter(type -> type.getReadableName().equals(headerName))
                        .findFirst();

                    if (matchedType.isPresent()) {
                        settings.addTier(matchedType.get(), i);
                    }
                    else {
                        settings.addUserTier(headerName, i);
                    }
                }

                tierStepHeaderList.setSelectedIndex(0);

                loadCSVColumnMapper(0);
            }

            filesChanged = false;
        }

        super.gotoStep(stepIndex);
    }

    private static final class HeaderListModel extends AbstractListModel {

        private final List<String> list;

        public HeaderListModel(List<String> list) {
            this.list = list;
        }

        @Override
        public int getSize() {
            return list.size();
        }

        @Override
        public Object getElementAt(int index) {
            return list.get(index);
        }
    }

    private final class ColumnSettingsCard extends JPanel {
        private final CSVColumn column;
        private final String header;
        private final List<CSVColumnType> columnTypeList;
        private final String[] formatStyleArray = new String[]{
            "Short",
            "Medium",
            "Long",
            "Full"
        };

        public ColumnSettingsCard(CSVColumn column, String header) {
            setName(column.id);
            this.column = column;
            this.header = header;
            var exportOnlySet = new HashSet<>(Arrays.asList(
                CSVColumnType.PHONE_ALIGNMENT,
                CSVColumnType.RECORD_ID,
                CSVColumnType.PARTICIPANT_ID,
                CSVColumnType.RECORD_NUMBER
            ));
            this.columnTypeList = Arrays
                .stream(CSVColumnType.values())
                .filter(type -> !exportOnlySet.contains(type))
                .toList();
            initDefaultSettings();
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
                if (columnTypeComboBox.getSelectedIndex() == -1) return;
                column.columnType = columnTypeList.get(columnTypeComboBox.getSelectedIndex());
                ((CardLayout)settingsPanel.getLayout()).show(settingsPanel, column.columnType.toString());
            });
            columnTypeComboBox.setSelectedIndex(columnTypeList.indexOf(column.columnType));


            JPanel importContainerPanel = new JPanel(new VerticalLayout());

            JPanel importCheckBoxPanel = new JPanel(new HorizontalLayout());

            importCheckBoxPanel.add(new JLabel("Import column:"));
            JCheckBox importCheckBox = new JCheckBox();
            importCheckBox.setToolTipText("Import this column: " + (column.importThisColumn ? "Yes" : "No"));
            importCheckBox.setSelected(column.importThisColumn);
            importCheckBox.addActionListener(e -> {
                column.importThisColumn = importCheckBox.isSelected();
                importCheckBox.setToolTipText("Import this column: " + (column.importThisColumn ? "Yes" : "No"));
            });
            importCheckBoxPanel.add(importCheckBox);

            importContainerPanel.add(importCheckBoxPanel);

            JPanel importAsPanel = new JPanel(new HorizontalLayout());

            importAsPanel.add(new JLabel("Import as:"));
            importAsPanel.add(columnTypeComboBox);
            importContainerPanel.add(importAsPanel);

            centerPanel.add(importContainerPanel, BorderLayout.NORTH);

            centerPanel.add(settingsPanel, BorderLayout.CENTER);
            add(centerPanel, BorderLayout.CENTER);
        }

        private JPanel createTopTextPanel() {

            var topTextPanel = new JPanel(new VerticalLayout());

            var headerPanel = new JPanel(new HorizontalLayout());

            var columnTitleLabel = new JLabel(header);
            var font = new Font(FontPreferences.getTitleFont().getFontName(), Font.PLAIN, 16);
            columnTitleLabel.setFont(font);
            headerPanel.add(columnTitleLabel);

            topTextPanel.add(headerPanel);
            topTextPanel.add(new JLabel("Column: " + (column.csvColumnIndex + 1)));
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
                    case IPA_TARGET, IPA_ACTUAL -> settingsCard = ipaSettings();
                    case USER_TIER -> settingsCard = userTierSettings();
                    default -> {}
                }

                settingsPanel.add(settingsCard, columnType.toString());
            }

            return settingsPanel;
        }

        private void initDefaultSettings() {
            column.setOption("dateFormat", "ISO");
            column.setOption("locale", Locale.getDefault().getDisplayName());
            column.setOption(
                "syllabifierLanguage",
                SyllabifierLibrary.getInstance().defaultSyllabifierLanguage().toString()
            );
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

        private JPanel ipaSettings() {

            JPanel settingsCard = new JPanel(new VerticalLayout());

            List<String> languages = SyllabifierLibrary
                .getInstance()
                .availableSyllabifierLanguages()
                .stream()
                .map(language -> language.toString())
                .toList();
            JComboBox<String> languageComboBox = new JComboBox<>(languages.toArray(String[]::new));
            languageComboBox.addActionListener(e -> {
                column.setOption("syllabifierLanguage", languages.get(languageComboBox.getSelectedIndex()));
            });
            languageComboBox.setSelectedIndex(languages.indexOf(column.getOption("syllabifierLanguage")));

            var syllabifierPanel = new JPanel(new HorizontalLayout());
            syllabifierPanel.setBorder(new EmptyBorder(0,8,0,0));

            syllabifierPanel.add(new JLabel("Syllabifer Language:"));

            syllabifierPanel.add(languageComboBox);

            settingsCard.add(syllabifierPanel);

            return settingsCard;
        }

        private JPanel userTierSettings() {
            GridBagConstraints c = new GridBagConstraints();

            JPanel settingsCard = new JPanel(new VerticalLayout());

            JTextField tierNameField = new JTextField();
            tierNameField.setText(column.getOption(CSVImportSettings.USER_TIER_NAME_KEY));
            tierNameField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    textChanged();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    textChanged();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {

                }

                private void textChanged() {
                    column.setOption(CSVImportSettings.USER_TIER_NAME_KEY, tierNameField.getText());
                }
            });

            JPanel tierNamePanel = new JPanel(new GridBagLayout());

            tierNamePanel.setBorder(new EmptyBorder(0,8,0,8));

            c.fill = GridBagConstraints.BOTH;
            c.weightx = 0;
            c.weighty = 0;
            c.gridx = 0;
            c.gridy = 0;
            tierNamePanel.add(new JLabel("Tier Name:"), c);
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1;
            c.weighty = 0;
            c.gridx = 1;
            c.gridy = 0;
            tierNamePanel.add(tierNameField, c);

            settingsCard.add(tierNamePanel);

            return settingsCard;
        }
    }

    private final class CSVColumnListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
        ) {
            final JLabel retVal = (JLabel) super.getListCellRendererComponent(
                list,
                value,
                index,
                isSelected,
                cellHasFocus
            );

            if (settings.getImportColumnList().size() > index) {
                if (!isSelected && !settings.getColumnByIndex(index).importThisColumn) {
                    retVal.setForeground(Color.gray);
                }
            }

            return retVal;
        }
    }

    private final class WizardImporterListener implements CSVImporterListener {

        private CSVWriter csvWriter;

        public WizardImporterListener(CSVWriter csvWriter) {
            this.csvWriter = csvWriter;
        }

        private int warningCount = 0;

        @Override
        public void parsingError(
            String fileName,
            int csvRecordIndex,
            int fieldIndex,
            int charPositionInField,
            CSVColumnType csvColumnType,
            Session session,
            int recordIndexInSession,
            Exception e
        ) {
            String[] row = new String[]{
                fileName,
                String.valueOf(csvRecordIndex),
                String.valueOf(fieldIndex),
                String.valueOf(charPositionInField),
                csvColumnType.getReadableName(),
                new SessionPath(session.getCorpus(), session.getName()).toString(),
                String.valueOf(recordIndexInSession),
                e.getMessage()
            };
            try {
                csvWriter.writeNext(row);
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
