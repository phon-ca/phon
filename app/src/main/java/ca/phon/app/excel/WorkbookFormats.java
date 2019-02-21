package ca.phon.app.excel;

import jxl.write.DateFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;

/**
 * Factory for creating common workbook formats.
 * 
 */
public class WorkbookFormats {
	
	public static WritableCellFormat getDefaultFormat() {
		WritableCellFormat defaultFormat = new WritableCellFormat();
		WritableFont font2 = new WritableFont(WritableFont.ARIAL, WritableFont.DEFAULT_POINT_SIZE, WritableFont.NO_BOLD);
		defaultFormat.setFont(font2);
		return defaultFormat;
	}
	
	public static WritableCellFormat getColumnHeaderFormat() {
		WritableCellFormat columnHeaderFormat = new WritableCellFormat();
        WritableFont font = new WritableFont(WritableFont.ARIAL, WritableFont.DEFAULT_POINT_SIZE, WritableFont.BOLD);
        columnHeaderFormat.setFont(font);
		return columnHeaderFormat;
	}

	public static WritableCellFormat getDateCellFormat() {
		 DateFormat dateFormat = new DateFormat("YYYY-mm-DD");
		 WritableCellFormat dateCellFormat = new WritableCellFormat(dateFormat);
		 return dateCellFormat;
	}
	
}
