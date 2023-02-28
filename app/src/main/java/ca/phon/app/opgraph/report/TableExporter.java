package ca.phon.app.opgraph.report;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.app.excel.WorkbookUtils;
import ca.phon.app.log.LogUtil;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.util.OSInfo;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class TableExporter {

    public static void writeTableToFile(DefaultTableDataSource table, List<String> columns, File file, TableExportType type, String encoding, boolean useIntegerForBoolean) throws IOException {
        switch(type) {
            case CSV:
                writeTableToCSVFile(table, columns, file, encoding, useIntegerForBoolean);
                break;

            case EXCEL:
                writeTableToExcelWorkbook(table, columns, file, encoding, useIntegerForBoolean);
                break;

            default:
                throw new IOException(new IllegalArgumentException("exportType"));
        }
    }

    public static void writeTableToExcelWorkbook(DefaultTableDataSource table, List<String> columns, File file, String encoding, boolean useIntegerForBoolean) throws IOException {
        final WritableWorkbook workbook = Workbook.createWorkbook(file);
        final WritableSheet sheet = workbook.createSheet("Sheet 1", 1);

        try {
            WorkbookUtils.addTableToSheet(sheet, 0, table, columns, useIntegerForBoolean);
            workbook.write();
        } catch (WriteException e) {
            throw new IOException(e);
        } finally {
            try {
                workbook.close();
            } catch (WriteException e) {
                LogUtil.severe(e);
            }
        }
    }

    public static void writeTableToCSVFile(DefaultTableDataSource table, List<String> columns, File file, String encoding, boolean useIntegerForBoolean) throws IOException {
        final CSVWriter writer =
                new CSVWriter(new PrintWriter(file, encoding), ',', '\"',
                        (OSInfo.isWindows() ? "\r\n" : "\n"));

        // write column header
        final String[] colnames = columns.toArray(new String[columns.size()]);
        writer.writeNext(colnames);

        final String[] currentRow = new String[colnames.length];
        for(int row = 0; row < table.getRowCount(); row++) {
            int col = 0;
            for(String colname:colnames) {
                Object cellVal = table.getValueAt(row, colname);
                if(cellVal instanceof Boolean && useIntegerForBoolean) {
                    cellVal = (cellVal == Boolean.TRUE ? 1 : 0);
                }
                currentRow[col++] = (cellVal == null ? "" : cellVal.toString());
            }
            writer.writeNext(currentRow);
        }
        writer.flush();
        writer.close();
    }

    public enum TableExportType {
        CSV,
        EXCEL
    }

}
