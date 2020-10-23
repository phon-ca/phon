/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.log;

import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import ca.phon.app.excel.*;
import ca.phon.query.report.datasource.*;
import jxl.write.*;
import jxl.write.biff.*;

public class HTMLToWorkbookWriter {

	private Map<String, DefaultTableDataSource> tableMap;
	
	public HTMLToWorkbookWriter() {
		this(new HashMap<>());
	}
	
	public HTMLToWorkbookWriter(Map<String, DefaultTableDataSource> tableMap) {
		super();
		this.tableMap = tableMap;
	}

	public void writeToWorkbook(WritableWorkbook workbook, String html) throws RowsExceededException, WriteException {
		final Document soupDoc = Jsoup.parse(html);

		// find all tables
		final List<Element> tableList = soupDoc.getElementsByTag("table");
		for(Element tableEle:tableList) {
			final String tableId = tableEle.attr("id");

			final WritableSheet sheet = createSheet(workbook, tableId);
			writeTableToSheet(sheet, 0, tableEle);
		}
	}

	public WritableSheet createSheet(WritableWorkbook workbook, String sheetName) {
		sheetName = WorkbookUtils.sanitizeTabName(sheetName);
		
		final List<String> currentSheetNames = Arrays.asList(workbook.getSheetNames());

		if(sheetName.trim().length() == 0)
			sheetName = "Sheet";

		String name = sheetName;
		int nameIdx = 0;
		while(currentSheetNames.contains(name)) {
			name = String.format("%s (%d)", sheetName, (++nameIdx));
		}

		return workbook.createSheet(name, workbook.getNumberOfSheets()+1);
	}

	public int writeTableToSheet(WritableSheet sheet, int startRow, Element tableEle) throws RowsExceededException, WriteException {
		final Elements ths = tableEle.getElementsByTag("th");
		List<String> columns = new ArrayList<>();
		for(Element thEle:ths) {
			columns.add(thEle.text());
		}
		return writeTableToSheet(sheet, startRow, tableEle, columns);
	}

	public int writeTableToSheet(WritableSheet sheet, int startRow, Element tableEle, List<String> columns) throws RowsExceededException, WriteException {
		// id of table is the table/buffer name
		final String tableId = tableEle.attr("id");
		final DefaultTableDataSource table = tableMap.get(tableId);
		if(table != null) {
			return WorkbookUtils.addTableToSheet(sheet, startRow, table, columns);
		} else {
			// TODO print table data from HTML
			return startRow;
		}
	}

}
