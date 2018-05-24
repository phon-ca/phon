/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 201, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
