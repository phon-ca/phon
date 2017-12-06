package ca.phon.app.log;

import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import ca.phon.query.report.datasource.DefaultTableDataSource;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;

public class HTMLToWorkbookWriter {

	private MultiBufferPanel multiBufferPanel;
	
	public HTMLToWorkbookWriter(MultiBufferPanel bufferPanel) {
		super();
		this.multiBufferPanel = bufferPanel;
	}
	
	public void writeToWorkbook(WritableWorkbook workbook, String html) throws RowsExceededException, WriteException {
		final Document soupDoc = Jsoup.parse(html); 
		
		// find all tables
		final List<Element> tableList = soupDoc.getElementsByTag("table");
		for(Element tableEle:tableList) {
			// caption is the title of the table
			final Element captionEle = tableEle.getElementsByTag("caption").first();
			String title = captionEle.text();
			
			if(title.trim().length() == 0) {
				title = "Sheet " + (workbook.getNumberOfSheets()+1);
			}
			
			final WritableSheet sheet = workbook.createSheet(title, workbook.getNumberOfSheets()+1);
			writeTableToSheet(sheet, 0, tableEle);
		}
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
		final BufferPanel bufferPanel = this.multiBufferPanel.getBuffer(tableId);
		if(bufferPanel != null && bufferPanel.getUserObject() instanceof DefaultTableDataSource) {
			return WorkbookUtils.addTableToSheet(sheet, startRow, 
					(DefaultTableDataSource)bufferPanel.getUserObject(), columns);
		} else {
			// TODO print table data from HTML
			return startRow;
		}
	}
	
}
