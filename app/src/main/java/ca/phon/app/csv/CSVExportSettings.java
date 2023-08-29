package ca.phon.app.csv;

import ca.phon.csv.CSVQuoteType;

import java.util.*;

public class CSVExportSettings {
    private final Map<String, Integer> userTiers;
    public final static String USER_TIER_NAME_KEY = "tierName";
    private boolean useFirstRowAsHeader = true;
    private char separator;
    private CSVQuoteType quoteType;
    private boolean trimSpaces;
    private String encoding;

    private List<CSVColumn> exportColumnList = new ArrayList<>();

    public CSVExportSettings(char separator, CSVQuoteType quoteType, String encoding, boolean useFirstRowAsHeader) {
        this.separator = separator;
        this.quoteType = quoteType;
        this.encoding = encoding;
        this.useFirstRowAsHeader = useFirstRowAsHeader;
        this.userTiers = new HashMap<>();
    }

    public void addTier(CSVColumnType type, int columnIndex) {
        var exportColumn = new CSVColumn();
        exportColumn.columnType = type;
        exportColumn.csvColumnIndex = columnIndex;
        exportColumnList.add(exportColumn);
    }

    public Map<String, Integer> getUserTiers() {
        return userTiers;
    }

    public void addUserTier(String tierName, int tierTierIndex) {
        var exportColumn = new CSVColumn();
        exportColumn.csvColumnIndex = tierTierIndex;
        exportColumn.columnType = CSVColumnType.USER_TIER;
        exportColumn.options.put(USER_TIER_NAME_KEY, tierName);
        exportColumnList.add(exportColumn);
    }

    public boolean isUseFirstRowAsHeader() {
        return useFirstRowAsHeader;
    }

    public void setUseFirstRowAsHeader(boolean useFirstRowAsHeader) {
        this.useFirstRowAsHeader = useFirstRowAsHeader;
    }

    public List<CSVColumn> getExportColumnList() {
        return Collections.unmodifiableList(exportColumnList);
    }

    public void setExportColumnList(List<CSVColumn> columnList) {
        exportColumnList = columnList;
    }
}
