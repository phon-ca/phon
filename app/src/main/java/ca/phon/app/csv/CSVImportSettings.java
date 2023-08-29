package ca.phon.app.csv;

import ca.phon.csv.CSVQuoteType;

import java.util.*;

public class CSVImportSettings {
    public final static String USER_TIER_NAME_KEY = "tierName";
    private boolean useFirstRowAsHeader = true;

    private char[] separators;
    private CSVQuoteType quoteType;
    private boolean trimSpaces;
    private String encoding;

    private List<CSVColumn> importColumnList = new ArrayList<>();
    private List<Boolean> importColumnChecklist = new ArrayList<>();


    public CSVImportSettings(char[] separators, CSVQuoteType quoteType, boolean trimSpaces, String encoding) {
        this.separators = separators;
        this.quoteType = quoteType;
        this.trimSpaces = trimSpaces;
        this.encoding = encoding;
    }

    public CSVColumn addTier(CSVColumnType type, int columnIndex) {
        var importColumn = new CSVColumn();
        importColumn.columnType = type;
        importColumn.csvColumnIndex = columnIndex;
        importColumnList.add(importColumn);
        importColumnChecklist.add(true);
        return importColumn;
    }

    public CSVColumn addUserTier(String tierName, int tierTierIndex) {
        var importColumn = new CSVColumn();
        importColumn.csvColumnIndex = tierTierIndex;
        importColumn.columnType = CSVColumnType.USER_TIER;
        importColumn.options.put(USER_TIER_NAME_KEY, tierName);
        importColumnList.add(importColumn);
        return importColumn;
    }

    public boolean isUseFirstRowAsHeader() {
        return useFirstRowAsHeader;
    }

    public void setUseFirstRowAsHeader(boolean useFirstRowAsHeader) {
        this.useFirstRowAsHeader = useFirstRowAsHeader;
    }

    public List<CSVColumn> getImportColumnList() {
        return Collections.unmodifiableList(importColumnList);
    }

    public Optional<CSVColumn> findColumnForImport(CSVColumnType type) {
        return importColumnList.stream().filter(importColumn -> importColumn.columnType == type).findFirst();
    }

    public Optional<CSVColumn> getColumnByType(CSVColumnType csvColumnType) {
        return importColumnList
            .stream()
            .filter(importColumn -> importColumn.columnType == csvColumnType)
            .findFirst();
    }

    public CSVColumn getColumnByIndex(int index) {
        return importColumnList.get(index);
    }

    public char[] getSeparators() {
        return separators;
    }

    public void setSeparators(char[] separators) {
        this.separators = separators;
    }

    public CSVQuoteType getQuoteType() {
        return quoteType;
    }

    public void setQuoteType(CSVQuoteType quoteType) {
        this.quoteType = quoteType;
    }

    public boolean getTrimSpaces() {
        return trimSpaces;
    }

    public void setTrimSpaces(boolean trimSpaces) {
        this.trimSpaces = trimSpaces;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
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
        props.setProperty(prefix + "quoteChar", quoteType.name());
        props.setProperty(prefix + "firstRowHeader", Boolean.toString(useFirstRowAsHeader));
        return props;
    }

    public void readProperties(String prefix, Properties props) {

        prefix = prefix == null ? "" : prefix + ".";

        separators = props.getProperty(prefix + "separators").toCharArray();
        encoding = props.getProperty(prefix + "encoding");
        trimSpaces = Boolean.valueOf(props.getProperty(prefix + "trimSpaces"));
        String finalPrefix = prefix;
        Optional<CSVQuoteType> potentialQuoteChar = Arrays
            .stream(CSVQuoteType.values())
            .filter(quoteChar -> quoteChar.name().equals(props.getProperty(finalPrefix + "quoteChar")))
            .findFirst();
        if (potentialQuoteChar.isPresent()) {
            quoteType = potentialQuoteChar.get();
        }
        useFirstRowAsHeader = Boolean.valueOf(props.getProperty(prefix + "firstRowHeader"));
    }

    public String[] getUserTierNames() {
        List<CSVColumn> userTiers = new ArrayList<>();

        for (int i = 0; i < importColumnChecklist.size(); i++) {
            if (!importColumnChecklist.get(i)) continue;
            userTiers.add(importColumnList.get(i));
        }

        return userTiers.stream().map(tier -> tier.getOption(USER_TIER_NAME_KEY)).toArray(String[]::new);
    }
}
