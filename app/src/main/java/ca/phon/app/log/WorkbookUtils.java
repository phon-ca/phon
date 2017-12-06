package ca.phon.app.log;

import java.lang.Number;
import java.time.*;
import java.util.*;

import javax.swing.table.TableModel;

import ca.phon.formatter.FormatterUtil;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;

/**
 * Utility methods for working with Excel workbook files.
 *
 */
public class WorkbookUtils {

	
	public static int addTableToSheet(WritableSheet sheet, int startRow, DefaultTableDataSource table)
		throws RowsExceededException, WriteException {
		final ArrayList<String> columns = new ArrayList<>();
		for(int col = 0; col < table.getColumnCount(); col++) columns.add(table.getColumnTitle(col));
		return addTableToSheet(sheet, startRow, table, columns);
	}
	
	/**
	 * Add table to the given sheet starting at row.
	 * Only print given columns.
	 * 
	 * @param sheet
	 * @param row
	 * @param table
	 * @param columns
	 * @throws WriteException 
	 * @throws RowsExceededException 
	 */
	public static int addTableToSheet(WritableSheet sheet, int startRow, DefaultTableDataSource table, List<String> columns) 
			throws RowsExceededException, WriteException {
		// write header
		WritableCellFormat cFormat = new WritableCellFormat();
        WritableFont font = new WritableFont(WritableFont.ARIAL, WritableFont.DEFAULT_POINT_SIZE, WritableFont.BOLD);
        cFormat.setFont(font);
        int col = 0;
		for(String column:columns) {
			final Label label = new Label(col++, 0, column, cFormat);
			sheet.addCell(label);
		}
		
		for(int row = 0; row < table.getRowCount(); row++) {
			final Object rowData[] = table.getRow(row);
			col = 0;
			for(String column:columns) {
				int tableCol = table.getColumnIndex(column);
				Object val = rowData[tableCol];
				
				if(val != null && val instanceof Number) {
					final jxl.write.Number cell = 
							new jxl.write.Number(col, startRow+row+1, ((Number)val).doubleValue());
					sheet.addCell(cell);
				} else if (val instanceof LocalDate) {
					final DateTime cell = new DateTime(col, startRow+row+1, 
							Date.from(((LocalDate)val).atStartOfDay(ZoneId.systemDefault()).toInstant()));
					sheet.addCell(cell);
				} else if(val instanceof LocalDateTime) {
					final DateTime cell = new DateTime(col, startRow+row+1,
							Date.from(((LocalDateTime)val).atZone(ZoneId.systemDefault()).toInstant()));
					sheet.addCell(cell);
				} else {
					final Label cell = new Label(col, startRow+row+1, FormatterUtil.format(val));
					sheet.addCell(cell);
				}
				++col;
			}
		}
		
		return (startRow + table.getRowCount());
	}
	
	public static int addTableToSheet(WritableSheet sheet, int startRow, TableModel tableModel) 
		throws RowsExceededException, WriteException {
		final ArrayList<String> columns = new ArrayList<>();
		for(int col = 0; col < tableModel.getColumnCount(); col++) columns.add(tableModel.getColumnName(col));
		return addTableToSheet(sheet, startRow, tableModel, columns);
	}
	
	/**
	 * Add csv table model to workbook sheet.
	 * 
	 * @param sheet
	 * @param startRow
	 * @param tableModel
	 * @param columns
	 */
	public static int addTableToSheet(WritableSheet sheet, int startRow, TableModel tableModel, List<String> columns)
		throws RowsExceededException, WriteException {
		// write header
		WritableCellFormat cFormat = new WritableCellFormat();
        WritableFont font = new WritableFont(WritableFont.ARIAL, WritableFont.DEFAULT_POINT_SIZE, WritableFont.BOLD);
        cFormat.setFont(font);
        int col = 0;
		for(String column:columns) {
			final Label label = new Label(col++, 0, column, cFormat);
			sheet.addCell(label);
		}
		
		for(int row = 0; row < tableModel.getRowCount(); row++) {
			col = 0;
			for(String column:columns) {
				int tableCol = getColumnIndex(tableModel, column);
				Object val = tableModel.getValueAt(row, tableCol);
				
				if(val != null && val instanceof Number) {
					final jxl.write.Number cell = 
							new jxl.write.Number(col, startRow+row+1, ((Number)val).doubleValue());
					sheet.addCell(cell);
				} else if (val instanceof LocalDate) {
					final DateTime cell = new DateTime(col, startRow+row+1, 
							Date.from(((LocalDate)val).atStartOfDay(ZoneId.systemDefault()).toInstant()));
					sheet.addCell(cell);
				} else if(val instanceof LocalDateTime) {
					final DateTime cell = new DateTime(col, startRow+row+1,
							Date.from(((LocalDateTime)val).atZone(ZoneId.systemDefault()).toInstant()));
					sheet.addCell(cell);
				} else {
					final Label cell = new Label(col, startRow+row+1, FormatterUtil.format(val));
					sheet.addCell(cell);
				}
				++col;
			}
		}
		return (startRow + tableModel.getRowCount());
	}
	
	private static int getColumnIndex(TableModel tableModel, String column) {
		int retVal = -1;
		for(int i = 0; i < tableModel.getColumnCount(); i++) {
			if(tableModel.getColumnName(i).equals(column)) {
				retVal = i;
				break;
			}
		}
		return retVal;
	}
	
}
