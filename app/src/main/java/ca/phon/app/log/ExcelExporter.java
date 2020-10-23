package ca.phon.app.log;

import jxl.write.*;

/**
 * Interface for exporting some context to Excel.
 *
 */
public interface ExcelExporter {
	
	/**
	 * Add content to the given workbook
	 * 
	 * @param wb
	 */
	public void addToWorkbook(WritableWorkbook wb) throws WriteException;

}
