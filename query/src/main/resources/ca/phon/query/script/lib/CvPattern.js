/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
 * CV Pattern library functions for query scripts.
 */

exports.CvPatternOptions = function (id) {
	
	var includeParamInfo = {
		"id": id + ".include",
		"title": "",
		"desc": "Include CGV pattern (CGV)",
		"def": false
	};
	var includeParam;
	this.include = includeParamInfo.def;
	
	var separateParamInfo = {
		"id": id + ".separate",
		"title": "",
		"desc": "Use separate metadata fields for IPA Target (CGV-T) and IPA Actual (CGV-A)",
		"def": false
	};
	var separateParam;
	this.separate = separateParamInfo.def;
	
	this.param_setup = function (params) {
		includeParam = new BooleanScriptParam(
		includeParamInfo.id,
		includeParamInfo.desc,
		includeParamInfo.title,
		includeParamInfo.def);
		
		separateParam = new BooleanScriptParam(
		separateParamInfo.id,
		separateParamInfo.desc,
		separateParamInfo.title,
		separateParamInfo.def);
		
		params.add(includeParam);
		params.add(separateParam);
	};
};