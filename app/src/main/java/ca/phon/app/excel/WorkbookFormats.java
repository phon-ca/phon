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

import jxl.write.*;

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
