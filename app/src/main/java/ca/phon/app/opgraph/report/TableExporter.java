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

public class TableExporter {

    public static void writeTableToFile(DefaultTableDataSource table, File file, TableExportType type, String encoding, boolean useIntegerForBoolean) throws IOException {
        switch(type) {
            case CSV:
                writeTableToCSVFile(table, file, encoding, useIntegerForBoolean);
                break;

            case EXCEL:
                writeTableToExcelWorkbook(table, file, encoding, useIntegerForBoolean);
                break;

            default:
                throw new IOException(new IllegalArgumentException("exportType"));
        }
    }

    public static void writeTableToExcelWorkbook(DefaultTableDataSource table, File file, String encoding, boolean useIntegerForBoolean) throws IOException {
        final WritableWorkbook workbook = Workbook.createWorkbook(file);
        final WritableSheet sheet = workbook.createSheet("Sheet 1", 1);

        try {
            WorkbookUtils.addTableToSheet(sheet, 0, table, useIntegerForBoolean);
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

    public static void writeTableToCSVFile(DefaultTableDataSource table, File file, String encoding, boolean useIntegerForBoolean) throws IOException {
        final CSVWriter writer =
                new CSVWriter(new PrintWriter(file, encoding), ',', '\"',
                        (OSInfo.isWindows() ? "\r\n" : "\n"));

        // write column header
        final String[] colnames = new String[table.getColumnCount()];
        for(int i = 0; i < table.getColumnCount(); i++) {
            colnames[i] = table.getColumnTitle(i);
        }
        writer.writeNext(colnames);

        final String[] currentRow = new String[table.getColumnCount()];
        for(int row = 0; row < table.getRowCount(); row++) {
            for(int col = 0; col < table.getColumnCount(); col++) {
                Object cellVal = table.getValueAt(row, col);
                if(cellVal instanceof Boolean && useIntegerForBoolean) {
                    cellVal = (cellVal == Boolean.TRUE ? 1 : 0);
                }
                currentRow[col] = (cellVal == null ? "" : cellVal.toString());
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
