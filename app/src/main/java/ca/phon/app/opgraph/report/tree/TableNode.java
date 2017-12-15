package ca.phon.app.opgraph.report.tree;

import java.util.*;
import java.util.stream.Collectors;

public class TableNode extends ReportTreeNode {
	
	private String tableName;
	
	private boolean includeColumns = true;
	
	private List<String> columnList;
	
	public TableNode(String title, String tableName) {
		this(title, tableName, new ArrayList<>());
	}
	
	public TableNode(String title, String tableName, List<String> columns) {
		this(title, tableName, true, columns);
	}

	public TableNode(String title, String tableName, boolean includeColumns, List<String> columns) {
		super(title);
		this.tableName = tableName;
		this.includeColumns = includeColumns;
		this.columnList = columns;
	}
	
	private boolean includeAllColumns() {
		return (this.includeColumns && this.columnList.size() == 0);
	}
	
	public boolean isIncludeColumns() {
		return this.includeColumns;
	}
	
	public void setIncludeColumns(boolean includeCols) {
		this.includeColumns = includeCols;
	}
	
	public String getTableName() {
		return this.tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public void setColumns(List<String> columns) {
		this.columnList = columns;
	}
	
	public List<String> getColumns() {
		return Collections.unmodifiableList(this.columnList);
	}

	@Override
	public String getReportTemplateBlock() {
		StringBuffer buffer = new StringBuffer();
		
		// setup table caption
		buffer.append("#set($caption = \"<h")
			.append(getLevel()).append(">")
			.append(getTitle())
			.append("</h").append(getLevel()).append(">\")\n");
		
		buffer.append("#set($tableName = \"").append(getTableName()).append("\")\n");
		
		buffer.append("#set($table = $tables.get($tableName))\n");
		
		if(includeAllColumns()) {
			buffer.append("#printTableWithIdAndCaption($table $tableName $caption [])\n");
		} else {
			final String columnTxt = columnList.stream()
					.map( (col) -> "\"" + col + "\"" ).collect(Collectors.joining(","));
			if(isIncludeColumns()) {
				buffer.append("#printTableWithIdAndCaption($table $tableName $caption [")
					.append(columnTxt).append("])\n");
			} else {
				buffer.append("#printTableWithIdAndCaptionExcludingColumns($table $tableName $caption [")
					.append(columnTxt).append("])\n");
			}
		}
		
		return buffer.toString();
	}

}
