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

exports.PMLUOptions = function(id) {

	var ignoreTruncatedParamInfo = {
		"id": id + ".ignoreTruncated",
		"desc": "Ignore truncated words",
		"title": "",
		"def": true
	};
	var ignoreTruncatedParam;
	this.ignoreTruncated = ignoreTruncatedParamInfo.def;
	
	var includePMLUParamInfo = {
		"id": id + ".includePMLU",
		"desc": "Include Phonological Mean Length of Utterance (Ingram 2002)",
		"title": "PMLU",
		"def": true
	};
	var includePMLUParam;
	this.includePMLU = includePMLUParamInfo.def;
	
	var includeEPMLUParamInfo = {
		"id": id + ".includeEPMLU",
		"desc": "Include Expanded Phonological Mean Length of Utterance (Arias & Lleó 2013)",
		"title": "ePMLU",
		"def": true
	};
	var includeEPMLUParam;
	this.includeEPMLU = includeEPMLUParamInfo.def;
	
	var ePMLUClosedSyllBonusParamInfo = {
		"id": id + ".closedSyllBonus",
		"desc": "Closed syllable bonus",
		"title": "",
		"prompt": "Enter a number",
		"def": "1.0"
	};
	var ePMLUClosedSyllBonusParam;
	this.closedSyllBonus = ePMLUClosedSyllBonusParamInfo.def;
	
	this.param_setup = function( params ) {
		ignoreTruncatedParam = new BooleanScriptParam(
			ignoreTruncatedParamInfo.id,
			ignoreTruncatedParamInfo.desc,
			ignoreTruncatedParamInfo.title,
			ignoreTruncatedParamInfo.def);
		params.add(ignoreTruncatedParam);
	
		includePMLUParam = new BooleanScriptParam(
			includePMLUParamInfo.id,
			includePMLUParamInfo.desc,
			includePMLUParamInfo.title,
			includePMLUParamInfo.def);
		params.add(includePMLUParam);
	
		includeEPMLUParam = new BooleanScriptParam(
			includeEPMLUParamInfo.id,
			includeEPMLUParamInfo.desc,
			includeEPMLUParamInfo.title,
			includeEPMLUParamInfo.def);
		params.add(includeEPMLUParam);
	
		ePMLUClosedSyllBonusParam = new StringScriptParam(
			ePMLUClosedSyllBonusParamInfo.id,
			ePMLUClosedSyllBonusParamInfo.desc,
			ePMLUClosedSyllBonusParamInfo.def);
		ePMLUClosedSyllBonusParam.setPrompt(ePMLUClosedSyllBonusParamInfo.prompt);
		params.add(ePMLUClosedSyllBonusParam);
	};

};
