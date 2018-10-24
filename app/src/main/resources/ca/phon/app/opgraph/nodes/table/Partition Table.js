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
 
 /**
  * Partition Table.js
  * 
  * Script for use with ca.phon.app.opgraph.nodes.table.TableScriptNode
  * 
  * Partition table using a standard query filter and column name.
  */
var PatternFilter = require("lib/PatternFilter").PatternFilter;

var columnParamInfo = {
	"id": "columnName",
	"title": "Column Name",
	"def": "",
	"prompt": "Enter column name"
}

var filters = {
    "column": new PatternFilter("filters.column")
}

function setup_params(params) {
	var columnParam = new StringScriptParam(
		columnParamInfo.id,
		columnParamInfo.title,
		columnParamInfo.def);
	columnParam.setPrompt(columnParamInfo.prompt);
	params.add(columnParam);
	
	filters.column.param_setup(params);
}

// add custom inputs/outputs here
function init(inputs, outputs) {
	outputs.add("trueTable", "Table of rows from input table which match filter", false, DefaultTableDataSource);
	outputs.add("falseTable", "Table of rows from output table which do not match the filter", false, DefaultTableDataSource);
}

// run operation on table
function tableOp(context, table) {
	// create output tables
	trueTable = new DefaultTableDataSource();
	falseTable = new DefaultTableDataSource();
	
	context.put("trueTable", trueTable);
	context.put("falseTable", falseTable);
	
	if(table == null) return;
	
	for(c = 0; c < table.columnCount; c++) {
	    var colTitle = table.getColumnTitle(c);
	    trueTable.setColumnTitle(c, colTitle);
	    falseTable.setColumnTitle(c, colTitle);
	}

	
	// find table column index
	col = table.getColumnIndex(columnName);
	if(col < 0) {
		context.put("falseTable", table);	
		return; // column not found
	}
		
	for(row = 0; row < table.rowCount; row++) {
		rowData = table.getRow(row);
		rowMatches = filterRow(table, row, col);
		
		if(rowMatches == true) {
			trueTable.addRow(rowData);
		} else {
			falseTable.addRow(rowData);
		}
	}
	
}

function filterRow(table, row, col) {
	var value = table.getValueAt(row, col);
	if(value == null) return false;
	
	return filters.column.check_filter(value);
}
