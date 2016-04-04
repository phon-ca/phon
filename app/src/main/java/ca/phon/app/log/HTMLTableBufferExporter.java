package ca.phon.app.log;

import java.io.StringReader;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Convert buffer table to an HTML table.
 * 
 */
public class HTMLTableBufferExporter implements BufferExporter<String> {
	
	private boolean useFirstRowAsColumnHeader = true;
	
	public HTMLTableBufferExporter() {
		super();
	}
	
	public HTMLTableBufferExporter(boolean useFirstRowAsColumnHeader) {
		super();
		this.useFirstRowAsColumnHeader = useFirstRowAsColumnHeader;
	}

	@Override
	public String exportBuffer(LogBuffer logBuffer) throws BufferExportException {
		final CSVReader reader = new CSVReader(new StringReader(logBuffer.getText()));
		final CSVTableModel tableModel = new CSVTableModel(reader);
		tableModel.setUseFirstRowAsHeader(this.useFirstRowAsColumnHeader);
		
		// TODO Add styles
		
		final StringBuffer buffer = new StringBuffer();
		buffer.append("<table style='report-table'>\n");
		buffer.append("\t<tr style='report-table-header-row'>\n");
		for(int col = 0; col < tableModel.getColumnCount(); col++) {
			String colName = tableModel.getColumnName(col);
			buffer.append("\t\t<th style='report-table-header'>").append(colName).append("</th>\n");
		}
		buffer.append("\t</tr>\n");
		
		for(int row = 0; row < tableModel.getRowCount(); row++) {
			buffer.append("\t<tr style='report-table-row").append(row % 2).append("'>\n");
			
			for(int col = 0; col < tableModel.getColumnCount(); col++) {
				buffer.append("\t\t<td style='report-table-cell'>");
				buffer.append(tableModel.getValueAt(row, col));
				buffer.append("</td>\n");
			}
			
			buffer.append("\t</tr>\n");
		}
		buffer.append("\t<tr style='report-table-header-row'>\n");
		
		return buffer.toString();
	}

}
