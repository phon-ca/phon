/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.excel;

import ca.phon.formatter.FormatterUtil;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;
import org.apache.commons.lang.StringUtils;

import javax.swing.table.TableModel;
import java.lang.Boolean;
import java.lang.Number;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
					// don't write infinity values to excel
					final Number number = (Number)val;
					if(number.doubleValue() == Double.POSITIVE_INFINITY || number.doubleValue() == Double.POSITIVE_INFINITY) {
						final Label cell = new Label(col, startRow+row+1, "");
						sheet.addCell(cell);
					} else {
						final jxl.write.Number cell = 
								new jxl.write.Number(col, startRow+row+1, ((Number)val).doubleValue());
						sheet.addCell(cell);
					}
				} else if (val instanceof LocalDate) {
					final DateTime cell = new DateTime(col, startRow+row+1, 
							Date.from(((LocalDate)val).atStartOfDay(ZoneId.systemDefault()).toInstant()));
					sheet.addCell(cell);
				} else if(val instanceof LocalDateTime) {
					final DateTime cell = new DateTime(col, startRow + row + 1,
							Date.from(((LocalDateTime) val).atZone(ZoneId.systemDefault()).toInstant()));
					sheet.addCell(cell);
				} else if(val instanceof Boolean) {
					final jxl.write.Boolean cell = new jxl.write.Boolean(col, startRow + row + 1, (boolean) val);
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
					final Number number = (Number)val;
					if(number.doubleValue() == Double.POSITIVE_INFINITY || number.doubleValue() == Double.POSITIVE_INFINITY) {
						final Label cell = new Label(col, startRow+row+1, "");
						sheet.addCell(cell);
					} else {
						final jxl.write.Number cell = 
								new jxl.write.Number(col, startRow+row+1, ((Number)val).doubleValue());
						sheet.addCell(cell);
					}
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
	
	/**
	 * Sanitize name of excel tab.
	 * 
	 * @param name
	 * @return version of name which complies to excel specification
	 */
	public static String sanitizeTabName(String name) {
		// reduce string length, keep end
		final String illegalCharRegex = "[\\\\/\\[\\]*?:]";
		String retVal = name.trim();
		retVal = retVal.replaceAll(illegalCharRegex, "_");
		retVal.replaceAll(illegalCharRegex, "_");
		
		if(retVal.length() > 31) {
			retVal = StringUtils.abbreviate(retVal, retVal.length()-31+3, 31);
		}
						
		return retVal;
	}
	
}
