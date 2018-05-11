package ca.phon.app.log;

import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import ca.phon.query.report.datasource.DefaultTableDataSource;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;

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
