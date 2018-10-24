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
var columnParam;

var splitOptionsParamInfo = {
	"ids": ["ignoreDiacritics", "caseSensitive"],
	"title": ["Ignore diacritics", "Case sensitive"],
	"def": [false, true],
	"cols": 0
}
var splitOptionsParam;

function setup_params(params) {
	columnParam = new StringScriptParam(
		columnParamInfo.id,
		columnParamInfo.title,
		columnParamInfo.def);
	columnParam.setPrompt(columnParamInfo.prompt);
	params.add(columnParam);
	
	splitOptionsParam = new MultiboolScriptParam(
		splitOptionsParamInfo.ids,
		splitOptionsParamInfo.def,
		splitOptionsParamInfo.title,
		"",
		splitOptionsParamInfo.cols);
	params.add(splitOptionsParam);
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
	var tableMap = new java.util.LinkedHashMap();
	context.put("tableMap", tableMap);
	context.put("keySet", new java.util.LinkedHashSet());
	
	if(table == null) return;
	
	// find table column index
	col = table.getColumnIndex(columnName);
	
	if(col < 0) 
		return; // column not found
	
	
	for(row = 0; row < table.rowCount; row++) {
		var rowVal = table.getValueAt(row, col);
		if(ignoreDiacritics == true && rowVal instanceof IPATranscript) {
			rowVal = rowVal.stripDiacritics();
		}
		
	    // use string value as row key
		var rowKey = Packages.ca.phon.formatter.FormatterUtil.format(rowVal);
		if(caseSensitive == false) {
			rowKey = rowKey.toLowerCase();
		}
		var keyTable = tableMap.get(rowKey);
		if(keyTable == null) {
		    keyTable = setupTable(table);
		    tableMap.put(rowKey, keyTable);
		}
		keyTable.addRow(table.getRow(row));
	}
	
	context.put("keySet", tableMap.keySet());
}
