package ca.phon.app.log;

import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

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
