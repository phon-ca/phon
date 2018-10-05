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
package ca.phon.app.opgraph.report.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
