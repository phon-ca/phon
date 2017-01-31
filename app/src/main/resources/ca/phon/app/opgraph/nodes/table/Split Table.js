/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
  * Split Table.js
  * 
  * Script for use with ca.phon.app.opgraph.nodes.table.TableScriptNode
  * 
  * Split table based on column name.  Two outputs
  * are produced, a set of keys from the column used in the filter  and a 
  * map of key -> table values.
  */

var columnParamInfo = {
	"id": "columnName",
	"title": "Column Name",
	"def": "",
	"prompt": "Enter column name"
}

function setup_params(params) {
	var columnParam = new StringScriptParam(
		columnParamInfo.id,
		columnParamInfo.title,
		columnParamInfo.def);
	columnParam.setPrompt(columnParamInfo.prompt);
	params.add(columnParam);
}

// add custom inputs/outputs here
function init(inputs, outputs) {
	outputs.add("keySet", "Unique values from given column name", false, java.util.Set);
	outputs.add("tableMap", "Map of key -> table values", false, java.util.Map);
}

/* 
 * Create a new table with the same schema as the inputTable
 */
function setupTable(inputTable) {
    var table = new DefaultTableDataSource();
    for(c = 0; c < inputTable.columnCount; c++) {
	    var colTitle = inputTable.getColumnTitle(c);
	    table.setColumnTitle(c, colTitle);
	}
	return table;
}

// run operation on table
function tableOp(context, table) {
	// find table column index
	col = table.getColumnIndex(columnName);
	
	if(col < 0) 
		return; // column not found
	
	var tableMap = new java.util.LinkedHashMap();
	
	for(row = 0; row < table.rowCount; row++) {
	    // use string value as row key
		var rowKey = Packages.ca.phon.formatter.FormatterUtil.format(table.getValueAt(row, col));
		
		var keyTable = tableMap.get(rowKey);
		if(keyTable == null) {
		    keyTable = setupTable(table);
		    tableMap.put(rowKey, keyTable);
		}
		keyTable.addRow(table.getRow(row));
	}
	
	context.put("keySet", tableMap.keySet);
	context.put("tableMap", tableMap);
}
