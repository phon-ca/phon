package ca.phon.app.opgraph.report.tree;

import java.util.*;
import java.util.stream.Collectors;

import ca.phon.query.report.datasource.TableDataSource;

public class TableNode extends ReportTreeNode {
	
	private TableDataSource table;
	
	private boolean includeColumns = true;
	
	private List<String> columnList;
	
	public TableNode(String title, TableDataSource table) {
		this(title, table, new ArrayList<>());
	}
	
	public TableNode(String title, TableDataSource table, List<String> columns) {
		this(title, table, true, columns);
	}

	public TableNode(String title, TableDataSource table, boolean includeColumns, List<String> columns) {
		super(title);
		this.table = table;
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
	
	public TableDataSource getTable() {
		return this.table;
	}
	
	public void setTable(TableDataSource table) {
		this.table = table;
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
				
		buffer.append("#set($tablePath = \"").append(getPath()).append("\")\n");
		
		buffer.append("#set($table = $tableMap.get($tablePath))\n");
		
		if(includeAllColumns()) {
			buffer.append("#printTableWithIdAndCaption($table $tablePath $caption [])\n");
		} else {
			final String columnTxt = columnList.stream()
					.map( (col) -> "\"" + col + "\"" ).collect(Collectors.joining(","));
			if(isIncludeColumns()) {
				buffer.append("#printTableWithIdAndCaption($table $tablePath $caption [")
					.append(columnTxt).append("])\n");
			} else {
				buffer.append("#printTableWithIdAndCaptionExcludingColumns($table $tablePath $caption [")
					.append(columnTxt).append("])\n");
			}
		}
		
		return buffer.toString();
	}

}
