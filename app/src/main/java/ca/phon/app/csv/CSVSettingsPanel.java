package ca.phon.app.csv;

import ca.phon.csv.CSVQuoteType;
import ca.phon.csv.CSVReader;
import ca.phon.csv.CSVWriter;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

// https://datatracker.ietf.org/doc/html/rfc4180
public class CSVSettingsPanel extends JPanel {

    private String fileName;
    private JXTable previewTable;
    private final HashSet<Character> separators = new HashSet<>();
    private Character otherSeparator;
    //private JCheckBox otherSeparatorCheckbox;
    private boolean trimSpaces = false;
    private String encoding;
    private CSVQuoteType quoteChar = CSVQuoteType.DOUBLE_QUOTE;
    private JPanel otherOptionsPanel;
    private JPanel otherOptionsContentPanel;
    private boolean firstRowHeader = false;
    private PreviewTableModel previewTableModel;
    private JLabel previewLabel;
    private final boolean multipleSeparators;
    private final boolean hidePreview;

    public CSVSettingsPanel(boolean multipleSeparators, boolean hidePreview)  {
        this.hidePreview = hidePreview;
        this.multipleSeparators = multipleSeparators;
        init();
    }

    public CSVSettingsPanel(boolean multipleSeparators)  {
        this(multipleSeparators, false);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Title");
        frame.setLayout(new BorderLayout());
        CSVSettingsPanel csvSettingsPanel = new CSVSettingsPanel(true);
        try {
            csvSettingsPanel.loadPreviewTableData("src/test/resources/ca/phon/csv/test-tab.csv");
        }
        catch (IOException e) {
            System.out.println(e);
        }
        csvSettingsPanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        frame.add(csvSettingsPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private ArrayList<String[]> generate2DArrayFromCSV(String fileName) throws IOException {
        char[] separators = this.separators.stream()
            .map(ch -> ch.toString())
            .collect(Collectors.joining())
            .toCharArray();
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(fileName), encoding);
        CSVReader csvReader = new CSVReader(inputStreamReader, separators, this.quoteChar, this.trimSpaces);

        ArrayList<String[]> outputList = new ArrayList<>();

        String[] line = csvReader.readNext();

        while (line != null) {
            outputList.add(line);
            line = csvReader.readNext();
        }

        return outputList;
    }

    private void init() {
        GridBagConstraints c = new GridBagConstraints();
        setLayout(new GridBagLayout());

        Font boldFont = new Font("LucidaGrande", Font.BOLD,13);


        //region Encoding Panel

        JPanel encodingPanel = new JPanel();
        encodingPanel.setLayout(new GridBagLayout());

        JLabel encodingLabel = new JLabel("Encoding");
        encodingLabel.setFont(boldFont);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        encodingPanel.add(encodingLabel, c);


        //region Encoding Selector Panel

        JPanel encodingSelectorPanel = new JPanel();
        encodingSelectorPanel.setLayout(new GridBagLayout());

        JLabel charSetLabel = new JLabel("Character Set: ");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 0;
        encodingSelectorPanel.add(charSetLabel, c);

        String[] encodings = Charset
            .availableCharsets()
            .keySet()
            .stream()
            .map(key -> key.toString())
            .toArray(String[]::new);

        JComboBox<String> encodingComboBox = new JComboBox<>(encodings);
        int utf8Index = List.of(encodings).indexOf("UTF-8");
        encodingComboBox.setSelectedIndex(utf8Index);
        this.encoding = encodings[utf8Index];
        encodingComboBox.addActionListener((e)->{
            this.encoding = encodingComboBox.getSelectedItem().toString();
            System.out.println(this.encoding);
            loadPreviewTableData();
        });

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 0;
        encodingSelectorPanel.add(encodingComboBox, c);

        JPanel whiteSpacePanel = new JPanel();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 0;
        encodingSelectorPanel.add(whiteSpacePanel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(8,8,8,0);
        encodingPanel.add(encodingSelectorPanel, c);

        //endregion

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0,0,0,0);
        add(encodingPanel, c);

        //endregion


        //region Separator Options Panel

        JPanel separatorOptionsPanel = new JPanel();
        separatorOptionsPanel.setLayout(new GridBagLayout());

        JLabel separatorOptionsLabel = new JLabel("Separator Options");
        separatorOptionsLabel.setFont(boldFont);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0,0,0,0);
        separatorOptionsPanel.add(separatorOptionsLabel, c);

        //region Separator Checkboxes

        JPanel separatorCheckboxPanel = new JPanel(new GridBagLayout());

        if (this.multipleSeparators) {
            setupSeparatorCheckboxes(separatorCheckboxPanel);
        }
        else {
            setupSeparatorRadioButtons(separatorCheckboxPanel);
        }


        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 6;
        c.gridy = 0;
        separatorCheckboxPanel.add(new JPanel(), c);


        JLabel stringDelimiterLabel = new JLabel("String Delimiter: ");
        c.fill = GridBagConstraints.REMAINDER;
        c.weightx = 0;
        c.gridx = 7;
        c.gridy = 0;
        separatorCheckboxPanel.add(stringDelimiterLabel, c);

        String[] stringDelimiterComboBoxOptions = {"\"", "'"};
        CSVQuoteType[] stringDelimiterComboBoxValues = {CSVQuoteType.DOUBLE_QUOTE, CSVQuoteType.SINGLE_QUOTE};
        JComboBox<String> stringDelimiterComboBox = new JComboBox<>(stringDelimiterComboBoxOptions);
        stringDelimiterComboBox.addActionListener((e)->{
            int selectedIndex = stringDelimiterComboBox.getSelectedIndex();
            this.quoteChar = stringDelimiterComboBoxValues[selectedIndex];
            loadPreviewTableData();
        });
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.gridx = 8;
        c.gridy = 0;
        c.insets = new Insets(4,0,0,0);
        separatorCheckboxPanel.add(stringDelimiterComboBox, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(8,0,0,0);
        separatorOptionsPanel.add(separatorCheckboxPanel, c);

        //endregion

        //region More Separator Options

        JPanel moreSeparatorOptionsPanel = new JPanel();
        moreSeparatorOptionsPanel.setLayout(new HorizontalLayout());
        moreSeparatorOptionsPanel.setBorder(new EmptyBorder(0,10,0,0));



        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 5;
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0, 0,8,0);
        separatorOptionsPanel.add(moreSeparatorOptionsPanel, c);

        //endregion

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0,0,0,0);
        add(separatorOptionsPanel, c);

        //endregion

        //region Other Options Panel

        otherOptionsPanel = new JPanel();
        otherOptionsPanel.setLayout(new GridBagLayout());

        JLabel otherOptionsLabel = new JLabel("Other Options");
        otherOptionsLabel.setFont(boldFont);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        otherOptionsPanel.add(otherOptionsLabel, c);

        otherOptionsContentPanel = new JPanel();
        otherOptionsContentPanel.setLayout(new GridBagLayout());

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(8,0,8,0);
        otherOptionsPanel.add(otherOptionsContentPanel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0,0,0,0);
        add(otherOptionsPanel, c);

        //endregion

        if (hidePreview) return;

        //region Preview Panel

        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new GridBagLayout());

        previewLabel = new JLabel("Preview");
        previewLabel.setFont(boldFont);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0,0,0,0);
        previewPanel.add(previewLabel, c);

        this.previewTable = new JXTable(new PreviewTableModel(new ArrayList<>()));
        this.previewTable.setVisibleRowCount(5);
        JScrollPane previewTableScrollPane = new JScrollPane(this.previewTable);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(8,8,0,0);
        previewPanel.add(previewTableScrollPane, c);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 3;
        c.insets = new Insets(0,0,0,0);
        add(previewPanel, c);

        //endregion
    }

    private String getFileNameFromPath(String filePath) {
        String[] splitFilePath = filePath.split("/");
        return splitFilePath[splitFilePath.length - 1];
    }

    public void loadPreviewTableData() {
        if (fileName == null) return;
        try {
            // Get the data from the CSV with updated settings
            ArrayList<String[]> previewTableData = generate2DArrayFromCSV(fileName);
            // Update the model on the preview table
            this.previewTableModel = new PreviewTableModel(previewTableData);
            this.previewTableModel.setFirstRowHeader(this.firstRowHeader);
            this.previewTable.setModel(this.previewTableModel);
            this.previewLabel.setText("Preview (" + getFileNameFromPath(fileName) + ")");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadPreviewTableData(String fileName) throws IOException {
        this.fileName = fileName;
        // Get the data from the CSV with updated settings
        ArrayList<String[]> previewTableData = generate2DArrayFromCSV(fileName);
        // Update the model on the preview table
        this.previewTableModel = new PreviewTableModel(previewTableData);
        this.previewTableModel.setFirstRowHeader(this.firstRowHeader);
        this.previewTable.setModel(previewTableModel);
        this.previewLabel.setText("Preview (" + getFileNameFromPath(fileName) + ")");
    }

    public JPanel getOtherOptionsPanel() {
        return otherOptionsPanel;
    }

    public JPanel getOtherOptionsContentPanel() {
        return otherOptionsContentPanel;
    }

    public boolean isFirstRowHeader() {
        return firstRowHeader;
    }

    public void setFirstRowHeader(boolean firstRowHeader) {
        this.firstRowHeader = firstRowHeader;
        loadPreviewTableData();
    }

    public boolean getTrimSpaces() {
        return trimSpaces;
    }

    public void setTrimSpaces(boolean trimSpaces) throws IOException {
        this.trimSpaces = trimSpaces;
        loadPreviewTableData();
    }

    public String[] getTableHeaders() {
        return this.previewTableModel.getColumnNames();
    }

    public CSVReader createCSVReaderWithSettings(Reader reader) {
        char[] separatorsArray = this.separators.stream()
                .map(ch -> ch.toString())
                .collect(Collectors.joining())
                .toCharArray();
        return new CSVReader(reader, separatorsArray, this.quoteChar, this.trimSpaces);
    }

    public CSVImportSettings getImportSettings() {
        char[] separatorsArray = this.separators.stream()
            .map(ch -> ch.toString())
            .collect(Collectors.joining())
            .toCharArray();
        return new CSVImportSettings(
            separatorsArray,
            this.quoteChar,
            this.trimSpaces,
            this.encoding
        );
    }

    public CSVExportSettings getExportSettings() {
        char separator = this.separators.stream().toArray(Character[]::new)[0];
        return new CSVExportSettings(
            separator,
            this.quoteChar,
            this.encoding,
            this.firstRowHeader
        );
    }

    private void setupSeparatorCheckboxes(JPanel panel) {
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(new GridBagLayout());

        String[] checkBoxOptionNames = {"Tab", "Comma", "Semicolon", "Space"};
        char[] checkBoxOptionValues = {'\t', ',', ';', ' '};

        JCheckBox[] separatorCheckboxes = new JCheckBox[5];
        for (int i = 0; i < checkBoxOptionNames.length; i++) {
            separatorCheckboxes[i] = new JCheckBox(checkBoxOptionNames[i]);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0;
            c.gridx = i;
            c.gridy = 0;
            int finalI = i;
            separatorCheckboxes[i].addActionListener((e) -> {
                if (((AbstractButton) e.getSource()).isSelected()) {
                    this.separators.add(checkBoxOptionValues[finalI]);
                }
                else {
                    this.separators.remove(checkBoxOptionValues[finalI]);
                }
                System.out.println(this.separators);
                loadPreviewTableData();
            });
            panel.add(separatorCheckboxes[i], c);
        }
        separatorCheckboxes[1].setSelected(true);
        this.separators.add(',');

        var otherSeparatorCheckbox = new JCheckBox("Other");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.gridx = checkBoxOptionNames.length;
        c.gridy = 0;
        otherSeparatorCheckbox.addActionListener((e)->{
            if (((AbstractButton) e.getSource()).isSelected()) {
                if (this.otherSeparator != null) {
                    this.separators.add(this.otherSeparator);
                }
            }
            else {
                if (this.otherSeparator != null) {
                    this.separators.remove(this.otherSeparator);
                }
            }
            System.out.println(this.separators.toString());
            loadPreviewTableData();
        });
        panel.add(otherSeparatorCheckbox, c);

        JTextField otherTextField = new JTextField(2);
        otherTextField.setDocument(new LengthRestrictedDocument(1));
        otherTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateOtherSeparator();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateOtherSeparator();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateOtherSeparator();
            }

            public void updateOtherSeparator() {
                String text = otherTextField.getText();
                if (text.length() > 0) {
                    separators.remove(otherSeparator);
                    otherSeparator = text.charAt(0);
                    separators.add(otherSeparator);
                    otherSeparatorCheckbox.setSelected(true);
                }
                else {
                    separators.remove(otherSeparator);
                    otherSeparator = null;
                    otherSeparatorCheckbox.setSelected(false);
                }
                System.out.println(separators);
                loadPreviewTableData();
            }
        });
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.gridx = checkBoxOptionNames.length + 1;
        c.gridy = 0;
        panel.add(otherTextField, c);
    }

    private void setupSeparatorRadioButtons(JPanel panel) {
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(new GridBagLayout());

        String[] checkBoxOptionNames = {"Tab", "Comma", "Semicolon", "Space"};
        char[] checkBoxOptionValues = {'\t', ',', ';', ' '};

        ButtonGroup buttonGroup = new ButtonGroup();

        JRadioButton[] separatorButtons = new JRadioButton[5];
        for (int i = 0; i < checkBoxOptionNames.length; i++) {
            separatorButtons[i] = new JRadioButton(checkBoxOptionNames[i]);
            buttonGroup.add(separatorButtons[i]);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0;
            c.gridx = i;
            c.gridy = 0;
            int finalI = i;
            separatorButtons[i].addActionListener((e) -> {
                if (((AbstractButton) e.getSource()).isSelected()) {
                    this.separators.clear();
                    this.separators.add(checkBoxOptionValues[finalI]);
                }
                System.out.println(this.separators);
                loadPreviewTableData();
            });
            panel.add(separatorButtons[i], c);
        }
        separatorButtons[1].setSelected(true);
        this.separators.add(',');

        var otherSeparatorRadioButton = new JRadioButton("Other");
        buttonGroup.add(otherSeparatorRadioButton);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.gridx = checkBoxOptionNames.length;
        c.gridy = 0;
        otherSeparatorRadioButton.addActionListener((e)->{
            if (((AbstractButton) e.getSource()).isSelected()) {
                this.separators.clear();
                if (this.otherSeparator != null) {
                    this.separators.add(this.otherSeparator);
                }
            }
            System.out.println(this.separators.toString());
            loadPreviewTableData();
        });
        panel.add(otherSeparatorRadioButton, c);

        JTextField otherTextField = new JTextField(2);
        otherTextField.setDocument(new LengthRestrictedDocument(1));
        otherTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateOtherSeparator();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateOtherSeparator();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateOtherSeparator();
            }

            public void updateOtherSeparator() {
                String text = otherTextField.getText();
                if (text.length() > 0) {
                    separators.clear();
                    otherSeparator = text.charAt(0);
                    separators.add(otherSeparator);
                    otherSeparatorRadioButton.setSelected(true);
                }
                else {
                    separators.remove(otherSeparator);
                    otherSeparator = null;
                    otherSeparatorRadioButton.setSelected(false);
                }
                System.out.println(separators);
                loadPreviewTableData();
            }
        });
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.gridx = checkBoxOptionNames.length + 1;
        c.gridy = 0;
        panel.add(otherTextField, c);
    }

    public String getEncoding() {
        return encoding;
    }

    public Set<Character> getSeparators() {
        return this.separators;
    }

    public Properties writeProperties(String prefix, Properties props) {

        prefix = prefix == null ? "" : prefix + ".";

        StringBuilder stringBuilder = new StringBuilder();
        for (char separator : separators) {
            stringBuilder.append(separator);
        }
        props.setProperty(prefix + "separators", stringBuilder.toString());
        props.setProperty(prefix + "encoding", encoding);
        props.setProperty(prefix + "trimSpaces", Boolean.toString(trimSpaces));
        props.setProperty(prefix + "quoteChar", quoteChar.name());
        return props;
    }

    public void readProperties(String prefix, Properties props) {

        prefix = prefix == null ? "" : prefix + ".";

        separators.removeAll(separators);
        char[] separatorArray = props.getProperty(prefix + "separators").toCharArray();
        for (char separator : separatorArray) {
            separators.add(separator);
        }
        encoding = props.getProperty(prefix + "encoding");
        trimSpaces = Boolean.valueOf(props.getProperty(prefix + "trimSpaces"));
        String finalPrefix = prefix;
        Optional<CSVQuoteType> potentialQuoteChar = Arrays
            .stream(CSVQuoteType.values())
            .filter(quoteChar -> quoteChar.name().equals(props.getProperty(finalPrefix + "quoteChar")))
            .findFirst();
        if (potentialQuoteChar.isPresent()) {
            quoteChar = potentialQuoteChar.get();
        }
    }

    private static final class PreviewTableModel extends AbstractTableModel {

        private final ArrayList<String[]> data;
        private final int maxRowLength;
        private boolean firstRowHeader = false;

        public PreviewTableModel(ArrayList<String[]> data) {
            this.data = data;
            // Get the max length of any one row in the table data
            int maxRowLength = 0;
            for (String[] row : data) {
                if (maxRowLength < row.length) {
                    maxRowLength = row.length;
                }
            }
            this.maxRowLength = maxRowLength;
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return this.maxRowLength;
        }

        @Override
        public String getColumnName(int column) {
            return this.firstRowHeader ? this.data.get(0)[column] : super.getColumnName(column);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (data.get(rowIndex).length <= columnIndex) {
                return "";
            }
            else {
                int startingRow = this.firstRowHeader ? 1 : 0;
                return data.subList(startingRow, data.size()).get(rowIndex)[columnIndex];
            }
        }

        public boolean isFirstRowHeader() {
            return firstRowHeader;
        }

        public void setFirstRowHeader(boolean firstRowHeader) {
            this.firstRowHeader = firstRowHeader;
        }

        public String[] getColumnNames() {
            var nameList = new ArrayList<String>();
            for (int i = 0; i < maxRowLength; i++) {
                nameList.add(getColumnName(i));
            }
            return nameList.toArray(String[]::new);
        }
    }

    private static final class LengthRestrictedDocument extends PlainDocument {

        private final int limit;

        public LengthRestrictedDocument(int limit) {
            this.limit = limit;
        }

        @Override
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            if (str == null)
                return;
            if ((getLength() + str.length()) <= limit) {
                super.insertString(offs, str, a);
            }
        }
    }
}



